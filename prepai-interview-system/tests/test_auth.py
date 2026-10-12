import os
import sys
import unittest

# Add parent directory to sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app import create_app
from models import get_user_by_email, init_db

class AuthTestCase(unittest.TestCase):
    def setUp(self):
        # Configure test environment
        os.environ["DATABASE_PATH"] = ":memory:"
        self.app = create_app()
        self.app.config["TESTING"] = True
        self.app.config["WTF_CSRF_ENABLED"] = False
        self.client = self.app.test_client()

    def test_login_page_renders(self):
        response = self.client.get("/login")
        self.assertEqual(response.status_code, 200)
        self.assertIn(b"Walk in already having done this before.", response.data)
        self.assertIn(b"LIVE READINESS SIGNAL", response.data)

    def test_register_and_login_flow(self):
        # Register new account
        with self.client.session_transaction() as sess:
            sess["csrf_token"] = "test-token"

        reg_resp = self.client.post("/register", data={
            "name": "Vinoth Test",
            "email": "vinoth.test@example.com",
            "password": "securepassword123",
            "confirm_password": "securepassword123",
            "csrf_token": "test-token"
        }, follow_redirects=True)
        self.assertEqual(reg_resp.status_code, 200)
        self.assertIn(b"Vinoth Test", reg_resp.data)

        # Logout
        logout_resp = self.client.get("/logout", follow_redirects=True)
        self.assertEqual(logout_resp.status_code, 200)

        # Login again
        with self.client.session_transaction() as sess:
            sess["csrf_token"] = "test-token"

        login_resp = self.client.post("/login", data={
            "email": "vinoth.test@example.com",
            "password": "securepassword123",
            "csrf_token": "test-token"
        }, follow_redirects=True)
        self.assertEqual(login_resp.status_code, 200)
        self.assertIn(b"Good to see you, Vinoth Test.", login_resp.data)

    def test_invalid_login_rejection(self):
        with self.client.session_transaction() as sess:
            sess["csrf_token"] = "test-token"

        resp = self.client.post("/login", data={
            "email": "nonexistent@example.com",
            "password": "wrongpassword",
            "csrf_token": "test-token"
        })
        self.assertEqual(resp.status_code, 200)
        self.assertIn(b"Invalid email or password", resp.data)

if __name__ == "__main__":
    unittest.main()
