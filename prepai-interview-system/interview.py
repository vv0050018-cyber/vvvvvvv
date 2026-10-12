import time
import json
from flask import Blueprint, render_template, request, redirect, url_for, jsonify, g, flash
from auth import login_required
from models import (
    get_all_questions, get_question_by_id, get_daily_question, get_user_bookmarks,
    create_practice_session, get_practice_session, save_answer, complete_practice_session,
    get_session_answers, get_user_practice_sessions, get_user_readiness_metrics, filter_questions, get_db
)
from ai_evaluator import evaluate_interview_answer

interview_bp = Blueprint("interview", __name__)

# --- PRACTICE PAGE ---
@interview_bp.route("/practice")
@login_required
def practice():
    user_id = g.user["id"]
    daily_q = get_daily_question()
    bookmarks = get_user_bookmarks(user_id)
    recent_sessions = get_user_practice_sessions(user_id, limit=5)
    
    question_id = request.args.get("question_id", type=int)
    current_question = get_question_by_id(question_id) if question_id else daily_q

    categories = [
        "HR and Behavioral Questions",
        "Python Programming",
        "Data Structures and Algorithms",
        "SQL and Databases",
        "Machine Learning",
        "Full-Stack Development"
    ]

    return render_template(
        "practice.html",
        daily_question=daily_q,
        current_question=current_question,
        bookmarks=bookmarks,
        recent_sessions=recent_sessions,
        categories=categories,
        active_page="practice"
    )

@interview_bp.route("/api/practice/evaluate-quick", methods=["POST"])
@login_required
def practice_quick_evaluate():
    data = request.get_json() or {}
    question_id = data.get("question_id")
    user_answer = data.get("answer", "")

    question = get_question_by_id(question_id)
    if not question:
        return jsonify({"error": "Question not found"}), 404

    feedback = evaluate_interview_answer(
        question_text=question["question_text"],
        category=question["category"],
        difficulty=question["difficulty"],
        sample_answer=question["sample_answer"] or "",
        evaluation_criteria=question["evaluation_criteria"] or "",
        user_answer=user_answer
    )

    # Record single practice session in database
    session_id = create_practice_session(
        user_id=g.user["id"],
        category=question["category"],
        difficulty=question["difficulty"],
        total_questions=1
    )
    save_answer(session_id, question_id, user_answer, feedback["overall_score"], feedback)
    complete_practice_session(session_id, duration_seconds=data.get("duration", 60))

    return jsonify({
        "success": True,
        "feedback": feedback,
        "session_id": session_id
    })

# --- MOCK INTERVIEW SYSTEM ---
@interview_bp.route("/mock-interview/setup")
@login_required
def mock_setup():
    categories = [
        "All Categories (Mixed)",
        "HR and Behavioral Questions",
        "Python Programming",
        "Data Structures and Algorithms",
        "SQL and Databases",
        "Machine Learning",
        "Full-Stack Development"
    ]
    return render_template("mock_setup.html", categories=categories, active_page="practice")

@interview_bp.route("/mock-interview/start", methods=["POST"])
@login_required
def mock_start():
    category = request.form.get("category", "All Categories (Mixed)")
    difficulty = request.form.get("difficulty", "Medium")
    question_count = int(request.form.get("question_count", 3))
    duration_per_question = int(request.form.get("duration_minutes", 3)) * 60

    # Query matching questions
    cat_param = None if "All" in category else category
    diff_param = None if "All" in difficulty else difficulty
    
    questions = filter_questions(category=cat_param, difficulty=diff_param)
    if not questions:
        questions = get_all_questions()

    selected_questions = questions[:question_count]
    if not selected_questions:
        flash("No questions available for this selection.", "warning")
        return redirect(url_for("interview.mock_setup"))

    session_id = create_practice_session(
        user_id=g.user["id"],
        category=category,
        difficulty=difficulty,
        total_questions=len(selected_questions)
    )

    # Store question sequence in session
    q_ids = [q["id"] for q in selected_questions]
    return redirect(url_for("interview.mock_room", session_id=session_id, q_idx=0, q_ids=",".join(map(str, q_ids)), time_limit=duration_per_question))

@interview_bp.route("/mock-interview/<int:session_id>/room")
@login_required
def mock_room(session_id):
    practice_session = get_practice_session(session_id)
    if not practice_session:
        flash("Session not found.", "danger")
        return redirect(url_for("dashboard.index"))

    q_ids_raw = request.args.get("q_ids", "")
    q_idx = int(request.args.get("q_idx", 0))
    time_limit = int(request.args.get("time_limit", 180))

    q_id_list = [int(x) for x in q_ids_raw.split(",") if x.isdigit()]
    if not q_id_list or q_idx >= len(q_id_list):
        return redirect(url_for("interview.mock_results", session_id=session_id))

    current_q_id = q_id_list[q_idx]
    current_question = get_question_by_id(current_q_id)

    return render_template(
        "mock_interview.html",
        session=practice_session,
        question=current_question,
        q_idx=q_idx,
        total_questions=len(q_id_list),
        q_ids=q_ids_raw,
        time_limit=time_limit,
        active_page="practice"
    )

@interview_bp.route("/api/mock-interview/submit-answer", methods=["POST"])
@login_required
def mock_submit_answer():
    data = request.get_json() or {}
    session_id = data.get("session_id")
    question_id = data.get("question_id")
    answer_text = data.get("answer", "")
    duration_spent = data.get("duration_spent", 0)

    question = get_question_by_id(question_id)
    if not question:
        return jsonify({"error": "Question not found"}), 404

    feedback = evaluate_interview_answer(
        question_text=question["question_text"],
        category=question["category"],
        difficulty=question["difficulty"],
        sample_answer=question["sample_answer"] or "",
        evaluation_criteria=question["evaluation_criteria"] or "",
        user_answer=answer_text
    )

    save_answer(session_id, question_id, answer_text, feedback["overall_score"], feedback)

    return jsonify({
        "success": True,
        "score": feedback["overall_score"],
        "feedback": feedback
    })

@interview_bp.route("/mock-interview/<int:session_id>/finish", methods=["POST", "GET"])
@login_required
def mock_finish(session_id):
    total_time = int(request.args.get("total_time", 0))
    complete_practice_session(session_id, duration_seconds=total_time)
    return redirect(url_for("interview.mock_results", session_id=session_id))

@interview_bp.route("/mock-interview/<int:session_id>/results")
@login_required
def mock_results(session_id):
    practice_session = get_practice_session(session_id)
    if not practice_session:
        flash("Session not found.", "danger")
        return redirect(url_for("dashboard.index"))

    answers_raw = get_session_answers(session_id)
    answers = []
    for a in answers_raw:
        item = dict(a)
        try:
            item["feedback"] = json.loads(item["feedback_json"])
        except Exception:
            item["feedback"] = {}
        answers.append(item)

    # Calculate overall metrics
    overall_score = practice_session["overall_score"]
    
    # Calculate category strengths
    cat_scores = {}
    for a in answers:
        cat = a["category"]
        cat_scores.setdefault(cat, []).append(a["score"])
        
    strongest_area = max(cat_scores.items(), key=lambda i: sum(i[1])/len(i[1]))[0] if cat_scores else "Communication Structure"
    weakest_area = min(cat_scores.items(), key=lambda i: sum(i[1])/len(i[1]))[0] if cat_scores else "Technical Terminology"

    return render_template(
        "interview_results.html",
        session=practice_session,
        answers=answers,
        overall_score=overall_score,
        strongest_area=strongest_area,
        weakest_area=weakest_area,
        active_page="practice"
    )

# --- PROGRESS ANALYTICS ---
@interview_bp.route("/progress")
@login_required
def progress():
    user_id = g.user["id"]
    metrics = get_user_readiness_metrics(user_id)
    sessions = get_user_practice_sessions(user_id, limit=30)
    
    # Build timeline series
    timeline_labels = []
    timeline_scores = []
    for s in reversed(sessions):
        timeline_labels.append(s["created_at"][:10])
        timeline_scores.append(s["overall_score"])

    if not timeline_scores:
        timeline_labels = ["Day 1", "Day 2", "Day 3", "Day 4", "Day 5"]
        timeline_scores = [60, 65, 72, 70, 75]

    # Category performance
    with get_db() as conn:
        cat_stats = conn.execute(
            """SELECT q.category, AVG(a.score) as avg_score, COUNT(a.id) as total_answers
               FROM answers a
               JOIN questions q ON a.question_id = q.id
               JOIN practice_sessions s ON a.session_id = s.id
               WHERE s.user_id = ?
               GROUP BY q.category""",
            (user_id,)
        ).fetchall()

    return render_template(
        "progress.html",
        metrics=metrics,
        sessions=sessions,
        timeline_labels=json.dumps(timeline_labels),
        timeline_scores=json.dumps(timeline_scores),
        cat_stats=cat_stats,
        active_page="progress"
    )
