import os
import sys
import unittest

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app import create_app
from models import (
    create_user, get_all_questions, create_practice_session,
    save_answer, complete_practice_session, get_user_readiness_metrics
)

class DashboardTestCase(unittest.TestCase):
    def setUp(self):
        os.environ["DATABASE_PATH"] = ":memory:"
        self.app = create_app()
        self.app.config["TESTING"] = True
        self.client = self.app.test_client()

    def test_question_bank_seeded(self):
        questions = get_all_questions()
        self.assertGreaterEqual(len(questions), 4)
        
        # Verify specific requested seed questions exist
        q_texts = [q["question_text"] for q in questions]
        self.assertTrue(any("Tell me about a time you disagreed with a teammate" in q for q in q_texts))
        self.assertTrue(any("Describe a project you're most proud of" in q for q in q_texts))

    def test_readiness_calculation(self):
        user_id = create_user("Metric Tester", "metric@test.com", "pass123")
        
        # Initial empty state
        metrics_init = get_user_readiness_metrics(user_id)
        self.assertFalse(metrics_init["has_data"])
        self.assertEqual(metrics_init["readiness_score"], 72) # baseline display

        # Simulate 2 completed sessions
        s1 = create_practice_session(user_id, "Python Programming", "Medium", 1)
        save_answer(s1, 1, "test answer", 80, {"overall_score": 80})
        complete_practice_session(s1, 60)

        s2 = create_practice_session(user_id, "Data Structures and Algorithms", "Medium", 1)
        save_answer(s2, 2, "test answer 2", 90, {"overall_score": 90})
        complete_practice_session(s2, 90)

        metrics_after = get_user_readiness_metrics(user_id)
        self.assertTrue(metrics_after["has_data"])
        self.assertEqual(metrics_after["sessions_completed"], 2)
        self.assertEqual(metrics_after["readiness_score"], 85) # (80 + 90) / 2

if __name__ == "__main__":
    unittest.main()
