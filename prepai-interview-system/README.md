# PREPAI – AI-Powered Interview Preparation System
### Final-Year BSc Computer Science Capstone Project

PREPAI is a full-stack AI-assisted interview preparation and readiness intelligence system built with **Python Flask, SQLite, HTML5/CSS3/JavaScript, and Chart.js**. It features a design system modeled after modern high-performance SaaS tools, offering live voice and text mock interviews, automated STAR-framework evaluation, question bank filtering, and session readiness analytics.

---

## 1. System Architecture & Features

- **Frontend:** Responsive HTML5, CSS3 Custom Properties, Vanilla JavaScript, Chart.js, Google Fonts (*Playfair Display* serif & *Inter* sans-serif).
- **Backend:** Python Flask modular blueprint architecture (`auth`, `dashboard`, `question_bank`, `interview`).
- **Database:** SQLite with relational tables, foreign key constraints, and automatic migration seeding.
- **Security:** Password hashing with PBKDF2/Werkzeug, CSRF tokens on forms, session cookies (`HttpOnly`, `SameSite=Lax`), and parameterized queries.
- **AI Assessment Engine:** Hybrid evaluation pipeline supporting live **Google Gemini** or **OpenAI** API evaluation with a deterministic local rule-based fallback analyzer that grades Relevance, Technical Depth, Structure, Clarity, and Completeness.
- **Audio Dictation:** In-browser Web Speech API transcription with real-time text synchronization.

---

## 2. Quick Setup & Run in Visual Studio Code (Windows PowerShell)

Open **Visual Studio Code**, navigate to the project directory, open the integrated terminal (`Ctrl + ~`), and run the following exact PowerShell commands:

### Step 1: Open Terminal in Project Root
```powershell
cd prepai-interview-system
```

### Step 2: Create Python Virtual Environment
```powershell
python -m venv venv
```

### Step 3: Activate the Virtual Environment
```powershell
.\venv\Scripts\Activate.ps1
```
*(Note: If PowerShell shows an ExecutionPolicy error, run `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass` once).*

### Step 4: Install Dependencies
```powershell
pip install -r requirements.txt
```

### Step 5: Configure Environment Variables
Copy `.env.example` to `.env`:
```powershell
Copy-Item .env.example .env
```
*(Optional: Open `.env` in VS Code and set `GEMINI_API_KEY=your_key` if you wish to use live Google Gemini AI. Otherwise, leave it blank to automatically use PREPAI's intelligent rule-based local evaluator).*

### Step 6: Start the Flask Development Server
```powershell
python app.py
```

### Step 7: Access the Application
Open your web browser and navigate to:
```
http://127.0.0.1:5000
```

---

## 3. Running Automated Tests

To run the automated test suite with `unittest` or `pytest`:

```powershell
python -m unittest discover tests
```
or
```powershell
pytest
```

---

## 4. End-to-End User Journey (Demonstration Workflow)

1. **Login & Registration (Screenshot 2 Match):**
   - Access `http://127.0.0.1:5000/login`.
   - Experience the 52% promotional pane with the headline *"Walk in already having done this before."* and animated *LIVE READINESS SIGNAL*.
   - Click the **Create account** tab on the segmented control, enter your details, and submit.
   - Alternatively, test **Continue as guest** to enter immediately with local session tracking.
2. **Dashboard (Screenshot 1 Match):**
   - View your personal greeting: *"Good to see you, [Your Name]."* under the **GREEN ROOM** tag.
   - Observe the **Readiness Score card** with recent practice performance bars and score 72.
   - Review the four KPI metrics: *Sessions completed*, *Average score*, *Day streak*, and *Focus area suggested*.
   - Review *Pick up where you left off* featuring the 4 benchmark interview questions with category and difficulty badges.
3. **Question Bank:**
   - Search across questions, filter by category or difficulty, star questions to bookmark them, and view benchmark model answers.
4. **Practice Lab & Live Mock Interview:**
   - Launch a quick practice round or configure a timed multi-question interview.
   - Use either keyboard typing or the microphone for speech-to-text dictation.
   - Click **Analyze & Score Answer** to receive multi-dimensional feedback: score breakdown, strengths, areas for improvement, and benchmark model answers.
5. **Progress Analytics:**
   - View your longitudinal score trajectory over sessions on the interactive Chart.js line chart.
   - Inspect the category proficiency breakdown table to identify areas requiring further rehearsal.
