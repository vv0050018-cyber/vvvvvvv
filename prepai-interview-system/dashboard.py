from flask import Blueprint, render_template, g, session, redirect, url_for
from auth import login_required
from models import get_user_readiness_metrics, get_seed_recent_questions, get_user_practice_sessions

dashboard_bp = Blueprint("dashboard", __name__)

@dashboard_bp.route("/")
@dashboard_bp.route("/dashboard")
@login_required
def index():
    user = g.user
    user_id = user["id"]
    
    # Calculate real readiness and statistics from SQLite database
    metrics = get_user_readiness_metrics(user_id)
    
    # Retrieve recent/recommended interview questions matching reference
    recent_questions = get_seed_recent_questions(limit=4)
    
    # Retrieve user's past sessions for activity tracking
    recent_sessions = get_user_practice_sessions(user_id, limit=5)
    
    return render_template(
        "dashboard.html",
        user=user,
        metrics=metrics,
        recent_questions=recent_questions,
        recent_sessions=recent_sessions,
        active_page="dashboard"
    )
