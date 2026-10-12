import functools
import secrets
from flask import Blueprint, render_template, request, redirect, url_for, session, flash, g
from models import (
    create_user, get_user_by_email, get_user_by_id, verify_user,
    create_reset_token, verify_reset_token, mark_token_used, update_user_password
)

auth_bp = Blueprint("auth", __name__)

def generate_csrf_token():
    if "csrf_token" not in session:
        session["csrf_token"] = secrets.token_hex(16)
    return session["csrf_token"]

def validate_csrf():
    token = request.form.get("csrf_token")
    if not token or token != session.get("csrf_token"):
        return False
    return True

def login_required(view):
    """Decorator to require login or guest session for protected routes."""
    @functools.wraps(view)
    def wrapped_view(**kwargs):
        if not session.get("user_id") and not session.get("is_guest"):
            flash("Please log in or continue as guest to access this page.", "warning")
            return redirect(url_for("auth.login"))
        return view(**kwargs)
    return wrapped_view

@auth_bp.before_app_request
def load_logged_in_user():
    user_id = session.get("user_id")
    if user_id is None:
        if session.get("is_guest"):
            g.user = {
                "id": session.get("guest_id", 9999),
                "name": "Guest",
                "email": "guest@prepai.local",
                "is_guest": True
            }
        else:
            g.user = None
    else:
        g.user = get_user_by_id(user_id)
        if g.user:
            g.user = dict(g.user)
            g.user["is_guest"] = False

@auth_bp.route("/login", methods=["GET", "POST"])
def login():
    if g.user:
        return redirect(url_for("dashboard.index"))

    csrf_token = generate_csrf_token()
    error = None

    if request.method == "POST":
        if not validate_csrf():
            error = "Invalid session token. Please try submitting again."
            return render_template("login.html", error=error, csrf_token=csrf_token)

        email = request.form.get("email", "").strip()
        password = request.form.get("password", "")
        remember = bool(request.form.get("remember"))

        if not email or not password:
            error = "Please enter both email address and password."
        else:
            user = verify_user(email, password)
            if user:
                session.clear()
                session["user_id"] = user["id"]
                session["user_name"] = user["name"]
                session["is_guest"] = False
                session.permanent = remember
                flash(f"Welcome back, {user['name']}!", "success")
                return redirect(url_for("dashboard.index"))
            else:
                error = "Invalid email or password. Please verify your credentials."

    return render_template("login.html", error=error, csrf_token=csrf_token, active_tab="login")

@auth_bp.route("/register", methods=["GET", "POST"])
def register():
    if g.user:
        return redirect(url_for("dashboard.index"))

    csrf_token = generate_csrf_token()
    error = None

    if request.method == "POST":
        if not validate_csrf():
            error = "Invalid session token. Please try submitting again."
            return render_template("register.html", error=error, csrf_token=csrf_token)

        name = request.form.get("name", "").strip()
        email = request.form.get("email", "").strip()
        password = request.form.get("password", "")
        confirm_password = request.form.get("confirm_password", "")

        if not name or not email or not password:
            error = "All fields are required."
        elif len(password) < 6:
            error = "Password must be at least 6 characters long."
        elif password != confirm_password:
            error = "Passwords do not match."
        elif get_user_by_email(email):
            error = "An account with this email address already exists."
        else:
            try:
                user_id = create_user(name, email, password)
                session.clear()
                session["user_id"] = user_id
                session["user_name"] = name
                session["is_guest"] = False
                flash("Account created successfully! Welcome to PREPAI.", "success")
                return redirect(url_for("dashboard.index"))
            except Exception as e:
                error = f"Registration failed: {str(e)}"

    return render_template("register.html", error=error, csrf_token=csrf_token, active_tab="register")

@auth_bp.route("/guest-login", methods=["POST"])
def guest_login():
    if not validate_csrf():
        flash("Invalid session token. Please try again.", "danger")
        return redirect(url_for("auth.login"))

    session.clear()
    session["is_guest"] = True
    session["user_name"] = "Guest"
    # Using a deterministic guest user ID so practice sessions can link locally
    guest_user = get_user_by_email("guest@prepai.local")
    if not guest_user:
        try:
            guest_id = create_user("Guest", "guest@prepai.local", secrets.token_hex(16))
        except Exception:
            guest_user = get_user_by_email("guest@prepai.local")
            guest_id = guest_user["id"] if guest_user else 1
    else:
        guest_id = guest_user["id"]
        
    session["user_id"] = guest_id
    flash("Continuing in Guest Mode. Data is retained during your session.", "info")
    return redirect(url_for("dashboard.index"))

@auth_bp.route("/logout")
def logout():
    session.clear()
    flash("You have been logged out safely.", "info")
    return redirect(url_for("auth.login"))

@auth_bp.route("/forgot-password", methods=["GET", "POST"])
def forgot_password():
    csrf_token = generate_csrf_token()
    message = None
    error = None

    if request.method == "POST":
        if not validate_csrf():
            error = "Invalid session token."
            return render_template("forgot_password.html", error=error, csrf_token=csrf_token)

        email = request.form.get("email", "").strip()
        user = get_user_by_email(email)
        if user:
            token = secrets.token_urlsafe(24)
            create_reset_token(user["id"], token)
            # In a development environment, display the simulated secure link directly for demo testing
            reset_url = url_for("auth.reset_password", token=token, _external=True)
            message = f"Password reset link generated: <a href='{reset_url}' class='text-amber'>Click here to reset your password</a> (Valid for 30 minutes)."
        else:
            # Prevent email enumeration while providing safe feedback
            message = "If an account exists with that email, a password reset link has been dispatched."

    return render_template("forgot_password.html", message=message, error=error, csrf_token=csrf_token)

@auth_bp.route("/reset-password/<token>", methods=["GET", "POST"])
def reset_password(token):
    token_row = verify_reset_token(token)
    if not token_row:
        flash("The password reset link is invalid or has expired.", "danger")
        return redirect(url_for("auth.forgot_password"))

    csrf_token = generate_csrf_token()
    error = None

    if request.method == "POST":
        if not validate_csrf():
            error = "Invalid session token."
            return render_template("forgot_password.html", error=error, csrf_token=csrf_token)

        new_password = request.form.get("password", "")
        confirm = request.form.get("confirm_password", "")

        if len(new_password) < 6:
            error = "Password must be at least 6 characters long."
        elif new_password != confirm:
            error = "Passwords do not match."
        else:
            update_user_password(token_row["user_id"], new_password)
            mark_token_used(token_row["id"])
            flash("Your password has been successfully updated. Please log in.", "success")
            return redirect(url_for("auth.login"))

    return render_template("reset_password.html", token=token, error=error, csrf_token=csrf_token)
