import json
import re
import requests
from config import Config

def evaluate_interview_answer(question_text, category, difficulty, sample_answer, evaluation_criteria, user_answer):
    """
    Evaluates a candidate's interview answer using Gemini/OpenAI API if configured,
    or falls back to an intelligent, deterministic local rule-based evaluation engine.
    
    Returns a validated dictionary with keys:
    - overall_score (0-100)
    - relevance (0-100)
    - technical_correctness (0-100)
    - clarity (0-100)
    - structure (0-100)
    - completeness (0-100)
    - communication_quality (0-100)
    - strengths (list of strings)
    - weaknesses (list of strings)
    - suggested_improved_answer (string)
    - recommendations (list of strings)
    - next_steps (list of strings)
    - is_ai_fallback (boolean)
    - evaluator_mode (string)
    """
    cleaned_answer = (user_answer or "").strip()
    if not cleaned_answer:
        return {
            "overall_score": 0,
            "relevance": 0,
            "technical_correctness": 0,
            "clarity": 0,
            "structure": 0,
            "completeness": 0,
            "communication_quality": 0,
            "strengths": ["None - no answer submitted."],
            "weaknesses": ["No answer provided for this question."],
            "suggested_improved_answer": sample_answer,
            "recommendations": ["Always attempt to provide at least an initial structure or outline, even when uncertain."],
            "next_steps": ["Review foundational principles for this topic before attempting another mock round."],
            "is_ai_fallback": True,
            "evaluator_mode": "Empty Answer Detection"
        }

    # Attempt Gemini API if key is available
    if Config.GEMINI_API_KEY:
        try:
            return _call_gemini_api(question_text, category, difficulty, sample_answer, evaluation_criteria, cleaned_answer)
        except Exception as e:
            print(f"[PREPAI Evaluator] Gemini API error: {e}. Falling back to Rule-Based Evaluator.")

    # Attempt OpenAI API if key is available
    if Config.OPENAI_API_KEY:
        try:
            return _call_openai_api(question_text, category, difficulty, sample_answer, evaluation_criteria, cleaned_answer)
        except Exception as e:
            print(f"[PREPAI Evaluator] OpenAI API error: {e}. Falling back to Rule-Based Evaluator.")

    # Local intelligent rule-based evaluator
    return _evaluate_rule_based(question_text, category, difficulty, sample_answer, evaluation_criteria, cleaned_answer)


def _call_gemini_api(question_text, category, difficulty, sample_answer, evaluation_criteria, user_answer):
    prompt = f"""You are PREPAI's Senior AI Interview Assessor.
Evaluate the following interview response with rigor and constructive precision.

QUESTION: {question_text}
CATEGORY: {category}
DIFFICULTY: {difficulty}
BENCHMARK SAMPLE ANSWER: {sample_answer}
EVALUATION CRITERIA: {evaluation_criteria}

CANDIDATE ANSWER:
"{user_answer}"

Respond ONLY with valid JSON in this exact structure:
{{
  "overall_score": <number between 0 and 100>,
  "relevance": <number between 0 and 100>,
  "technical_correctness": <number between 0 and 100>,
  "clarity": <number between 0 and 100>,
  "structure": <number between 0 and 100>,
  "completeness": <number between 0 and 100>,
  "communication_quality": <number between 0 and 100>,
  "strengths": ["<strength 1>", "<strength 2>"],
  "weaknesses": ["<weakness 1>", "<weakness 2>"],
  "suggested_improved_answer": "<ideal response demonstrating professional STAR/technical structure>",
  "recommendations": ["<recommendation 1>", "<recommendation 2>"],
  "next_steps": ["<actionable step 1>", "<actionable step 2>"]
}}"""

    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={Config.GEMINI_API_KEY}"
    payload = {
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {"temperature": 0.2, "response_mime_type": "application/json"}
    }
    resp = requests.post(url, json=payload, timeout=12)
    resp.raise_for_status()
    data = resp.json()
    raw_text = data["candidates"][0]["content"]["parts"][0]["text"]
    result = json.loads(raw_text)
    result["is_ai_fallback"] = False
    result["evaluator_mode"] = "Gemini Live AI Service"
    return _validate_evaluation_result(result, sample_answer)


def _call_openai_api(question_text, category, difficulty, sample_answer, evaluation_criteria, user_answer):
    headers = {
        "Authorization": f"Bearer {Config.OPENAI_API_KEY}",
        "Content-Type": "application/json"
    }
    system_prompt = "You are PREPAI's Senior Interview Assessor. Return only JSON matching the requested schema."
    user_prompt = f"""Evaluate candidate interview response:
Question: {question_text}
Category: {category}
Difficulty: {difficulty}
Benchmark: {sample_answer}
Criteria: {evaluation_criteria}

Candidate Answer:
{user_answer}

JSON Schema:
{{
  "overall_score": <0-100>,
  "relevance": <0-100>,
  "technical_correctness": <0-100>,
  "clarity": <0-100>,
  "structure": <0-100>,
  "completeness": <0-100>,
  "communication_quality": <0-100>,
  "strengths": ["..."],
  "weaknesses": ["..."],
  "suggested_improved_answer": "...",
  "recommendations": ["..."],
  "next_steps": ["..."]
}}"""
    payload = {
        "model": "gpt-4o-mini",
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt}
        ],
        "response_format": {"type": "json_object"},
        "temperature": 0.2
    }
    resp = requests.post("https://api.openai.com/v1/chat/completions", headers=headers, json=payload, timeout=12)
    resp.raise_for_status()
    raw_text = resp.json()["choices"][0]["message"]["content"]
    result = json.loads(raw_text)
    result["is_ai_fallback"] = False
    result["evaluator_mode"] = "OpenAI Live AI Service"
    return _validate_evaluation_result(result, sample_answer)


def _evaluate_rule_based(question_text, category, difficulty, sample_answer, evaluation_criteria, answer):
    """
    Sophisticated local rule-based evaluation fallback.
    Analyzes word count, STAR methodology structure, technical keyword overlap,
    sentence count, and clarity markers.
    """
    words = answer.split()
    word_count = len(words)
    lower_answer = answer.lower()
    
    # Keyword overlap with sample answer and evaluation criteria
    sample_keywords = set(re.findall(r"\b[a-zA-Z]{4,}\b", (sample_answer + " " + evaluation_criteria).lower()))
    answer_keywords = set(re.findall(r"\b[a-zA-Z]{4,}\b", lower_answer))
    
    common_keywords = sample_keywords.intersection(answer_keywords)
    overlap_ratio = len(common_keywords) / max(len(sample_keywords), 1)

    # Check for structural cues (STAR method, transition words, sequential indicators)
    star_cues = ["situation", "task", "action", "result", "initially", "because", "therefore", "firstly", "secondly", "finally", "outcome", "learned", "metric", "impact"]
    matched_cues = [cue for cue in star_cues if cue in lower_answer]

    # Metrics calculation
    # Length scoring (aiming for concise yet comprehensive 50 to 250 words)
    if word_count < 25:
        length_score = 45
    elif word_count < 60:
        length_score = 65
    elif word_count <= 220:
        length_score = 88
    else:
        length_score = 75  # slightly penalized for rambling

    # Technical correctness & relevance
    relevance = min(95, max(40, int(50 + overlap_ratio * 70)))
    tech_score = min(92, max(45, int(48 + overlap_ratio * 75)))
    
    # Structure score
    structure_score = min(94, max(42, int(45 + len(matched_cues) * 10)))
    
    # Clarity and communication
    sentences = re.split(r"[.!?]+", answer)
    avg_sentence_len = word_count / max(len([s for s in sentences if s.strip()]), 1)
    if 10 <= avg_sentence_len <= 25:
        clarity_score = 85
    else:
        clarity_score = 68
        
    completeness = min(90, max(40, int((length_score * 0.5) + (overlap_ratio * 50))))
    comm_score = int((clarity_score + structure_score) / 2)
    
    # Weighted overall score
    overall_score = round(
        (relevance * 0.25) +
        (tech_score * 0.25) +
        (structure_score * 0.15) +
        (clarity_score * 0.15) +
        (completeness * 0.10) +
        (comm_score * 0.10)
    )

    strengths = []
    weaknesses = []
    recommendations = []
    next_steps = []

    if word_count >= 50:
        strengths.append(f"Substantial response depth with {word_count} words covering essential points.")
    else:
        weaknesses.append(f"Answer was brief ({word_count} words); interviewers expect more elaboration.")
        recommendations.append("Expand on the specific steps you took and the concrete outcomes achieved.")

    if len(common_keywords) >= 4:
        strengths.append(f"Successfully integrated key domain terminology: {', '.join(list(common_keywords)[:3])}.")
    else:
        weaknesses.append("Lacked key technical terminology and benchmarks expected for this topic.")
        recommendations.append(f"Incorporate domain-specific concepts outlined in: {evaluation_criteria}")

    if matched_cues:
        strengths.append(f"Utilized structured framing cues ({', '.join(matched_cues[:2])}) that aid interviewer comprehension.")
    else:
        weaknesses.append("Lacked explicit framing transitions (e.g. STAR: Situation, Task, Action, Result).")
        recommendations.append("Structure answers using the STAR method to clearly convey technical ownership and impact.")

    next_steps.append("Re-record or re-write your answer using the suggested benchmark structure below.")
    next_steps.append("Practice timing your delivery to stay between 90 and 120 seconds.")

    return {
        "overall_score": overall_score,
        "relevance": relevance,
        "technical_correctness": tech_score,
        "clarity": clarity_score,
        "structure": structure_score,
        "completeness": completeness,
        "communication_quality": comm_score,
        "strengths": strengths or ["Attempted the question with clear intent."],
        "weaknesses": weaknesses or ["Could provide more quantified metrics and architectural trade-offs."],
        "suggested_improved_answer": sample_answer,
        "recommendations": recommendations or ["Practice articulating technical trade-offs with confidence."],
        "next_steps": next_steps,
        "is_ai_fallback": True,
        "evaluator_mode": "Intelligent Rule-Based Analyzer (Local Fallback)"
    }


def _validate_evaluation_result(result, fallback_sample_answer):
    """Ensure all required fields exist and conform to specifications."""
    def _clamp(val, default=70):
        try:
            return max(0, min(100, int(val)))
        except (ValueError, TypeError):
            return default

    result["overall_score"] = _clamp(result.get("overall_score"), 72)
    result["relevance"] = _clamp(result.get("relevance"), 70)
    result["technical_correctness"] = _clamp(result.get("technical_correctness"), 70)
    result["clarity"] = _clamp(result.get("clarity"), 70)
    result["structure"] = _clamp(result.get("structure"), 70)
    result["completeness"] = _clamp(result.get("completeness"), 70)
    result["communication_quality"] = _clamp(result.get("communication_quality"), 70)

    if not isinstance(result.get("strengths"), list):
        result["strengths"] = [str(result.get("strengths", "Good core explanation."))]
    if not isinstance(result.get("weaknesses"), list):
        result["weaknesses"] = [str(result.get("weaknesses", "Could expand with specific examples."))]
    if not isinstance(result.get("recommendations"), list):
        result["recommendations"] = [str(result.get("recommendations", "Review the STAR framework."))]
    if not isinstance(result.get("next_steps"), list):
        result["next_steps"] = [str(result.get("next_steps", "Conduct another timed round."))]

    if not result.get("suggested_improved_answer"):
        result["suggested_improved_answer"] = fallback_sample_answer

    return result
