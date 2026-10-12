from flask import Blueprint, render_template, request, jsonify, g
from auth import login_required
from models import filter_questions, get_question_by_id, toggle_bookmark

question_bank_bp = Blueprint("question_bank", __name__)

@question_bank_bp.route("/question-bank")
@login_required
def index():
    category = request.args.get("category", "All")
    difficulty = request.args.get("difficulty", "All")
    technology = request.args.get("technology", "All")
    search = request.args.get("search", "")

    user_id = g.user["id"]
    questions = filter_questions(
        category=category,
        difficulty=difficulty,
        technology=technology,
        search=search,
        user_id=user_id
    )

    categories = [
        "All",
        "HR and Behavioral Questions",
        "Python Programming",
        "Data Structures and Algorithms",
        "SQL and Databases",
        "Machine Learning",
        "Full-Stack Development",
        "Object-Oriented Programming"
    ]

    difficulties = ["All", "Easy", "Medium", "Hard"]

    return render_template(
        "question_bank.html",
        questions=questions,
        categories=categories,
        difficulties=difficulties,
        selected_category=category,
        selected_difficulty=difficulty,
        selected_technology=technology,
        search_query=search,
        active_page="question_bank"
    )

@question_bank_bp.route("/api/bookmark/<int:question_id>", methods=["POST"])
@login_required
def bookmark_toggle(question_id):
    user_id = g.user["id"]
    bookmarked = toggle_bookmark(user_id, question_id)
    return jsonify({
        "success": True,
        "question_id": question_id,
        "bookmarked": bookmarked
    })

@question_bank_bp.route("/api/question/<int:question_id>")
@login_required
def get_question_details(question_id):
    question = get_question_by_id(question_id)
    if not question:
        return jsonify({"error": "Question not found"}), 404
    return jsonify({
        "id": question["id"],
        "question_text": question["question_text"],
        "category": question["category"],
        "difficulty": question["difficulty"],
        "sample_answer": question["sample_answer"],
        "evaluation_criteria": question["evaluation_criteria"],
        "hints": question["hints"]
    })
