package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentTurquoise
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgInput
import com.example.ui.theme.BgMain
import com.example.ui.theme.BgSecondary
import com.example.ui.theme.BorderColor
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

// Data Models
data class Question(
    val id: Int,
    val text: String,
    val category: String,
    val difficulty: String,
    val criteria: String,
    val sampleAnswer: String,
    val hints: String
)

enum class Screen {
    LOGIN,
    DASHBOARD,
    QUESTION_BANK,
    PRACTICE,
    MOCK_INTERVIEW,
    PROGRESS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PrepAiApp()
            }
        }
    }
}

@Composable
fun PrepAiApp() {
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
    var userName by remember { mutableStateOf("Vinoth") }
    var selectedQuestionForPractice by remember { mutableStateOf<Question?>(null) }

    // Seed questions
    val questions = remember {
        mutableStateListOf(
            Question(
                1,
                "Tell me about a time you disagreed with a teammate. How did you handle it?",
                "HR and Behavioral Questions",
                "Medium",
                "STAR method, active listening, objective trade-off evaluation, and team alignment.",
                "In my previous project, a teammate wanted to use MongoDB while I advocated PostgreSQL for strict ACID financial data. We ran a 1-day spike, benchmarked schema rigidity, and agreed on Postgres, shipping on schedule.",
                "Structure answers around the STAR framework and keep it focused on technical rationale."
            ),
            Question(
                2,
                "Describe a project you're most proud of and your specific role in it.",
                "HR and Behavioral Questions",
                "Medium",
                "Technical ownership, architectural decisions, unblocking hurdles, and quantifiable business outcomes.",
                "I spearheaded the distributed ingestion pipeline for our analytics product, handling 50k events/sec. I introduced Kafka and Redis caching, cutting p99 latency by 45%.",
                "State the business problem, your individual responsibility, and measurable outcomes."
            ),
            Question(
                3,
                "Tell me about a time you failed. What did you learn?",
                "HR and Behavioral Questions",
                "Medium",
                "Accountability, root cause analysis, preventative safeguards, and resilience.",
                "I deployed a schema migration without an index on a high-throughput table, causing connection pool exhaustion. I immediately rolled back, wrote post-mortems, and added automated linter CI checks.",
                "Spend 30% explaining what occurred and 70% detailing the preventative safeguards introduced."
            ),
            Question(
                4,
                "How do you prioritize tasks when everything feels urgent?",
                "HR and Behavioral Questions",
                "Easy",
                "Eisenhower matrix, stakeholder expectation management, and business impact assessment.",
                "I evaluate tasks based on business impact vs effort, communicate trade-offs proactively to stakeholders, and establish clear sprint SLAs to prevent burnout.",
                "Mention actively communicating with product managers to re-baseline deadlines."
            ),
            Question(
                5,
                "Explain the difference between Python lists and tuples and memory implications.",
                "Python Programming",
                "Easy",
                "Mutability, fixed-size vs dynamic allocation, hashing, and performance characteristics.",
                "Lists are mutable arrays with over-allocation for O(1) appends. Tuples are immutable, smaller in memory footprint, and can serve as dictionary keys if hashable.",
                "Reference sys.getsizeof() and CPython internal allocation optimizations."
            ),
            Question(
                6,
                "How does Python's GIL affect multithreading vs multiprocessing?",
                "Python Programming",
                "Hard",
                "CPython bytecode constraints, I/O-bound vs CPU-bound concurrency, and subinterpreters.",
                "The Global Interpreter Lock prevents multiple native threads from executing Python bytecode simultaneously. Multithreading is optimal for I/O; multiprocessing circumvents the GIL for CPU tasks.",
                "Mention concurrent.futures and asyncio alternatives."
            ),
            Question(
                7,
                "How would you detect a cycle in a singly linked list? Compare two algorithms.",
                "Data Structures and Algorithms",
                "Medium",
                "Floyd's Tortoise and Hare vs Hash set traversal; O(1) space complexity.",
                "Floyd's cycle algorithm uses slow (1 step) and fast (2 steps) pointers. If they meet, a cycle exists in O(N) time and O(1) space. Alternatively, a hash set achieves O(N) time but O(N) space.",
                "Prove why fast and slow pointers must eventually meet if a cycle exists."
            )
        )
    }

    val bookmarkedIds = remember { mutableStateListOf(1, 2) }

    when (currentScreen) {
        Screen.LOGIN -> {
            LoginScreen(
                onLoginSuccess = { name ->
                    userName = name
                    currentScreen = Screen.DASHBOARD
                },
                onGuestContinue = {
                    userName = "Guest"
                    currentScreen = Screen.DASHBOARD
                }
            )
        }
        Screen.DASHBOARD -> {
            DashboardScreen(
                userName = userName,
                questions = questions.take(4),
                onNavigate = { screen -> currentScreen = screen },
                onStartMock = { currentScreen = Screen.MOCK_INTERVIEW },
                onBrowseBank = { currentScreen = Screen.QUESTION_BANK },
                onSelectQuestion = { q ->
                    selectedQuestionForPractice = q
                    currentScreen = Screen.PRACTICE
                },
                onLogout = { currentScreen = Screen.LOGIN }
            )
        }
        Screen.QUESTION_BANK -> {
            BackHandler { currentScreen = Screen.DASHBOARD }
            QuestionBankScreen(
                questions = questions,
                bookmarkedIds = bookmarkedIds,
                onToggleBookmark = { id ->
                    if (bookmarkedIds.contains(id)) bookmarkedIds.remove(id)
                    else bookmarkedIds.add(id)
                },
                onPracticeQuestion = { q ->
                    selectedQuestionForPractice = q
                    currentScreen = Screen.PRACTICE
                },
                onBack = { currentScreen = Screen.DASHBOARD }
            )
        }
        Screen.PRACTICE -> {
            BackHandler { currentScreen = Screen.DASHBOARD }
            PracticeScreen(
                initialQuestion = selectedQuestionForPractice ?: questions.first(),
                onBack = { currentScreen = Screen.DASHBOARD },
                onLaunchMock = { currentScreen = Screen.MOCK_INTERVIEW }
            )
        }
        Screen.MOCK_INTERVIEW -> {
            BackHandler { currentScreen = Screen.DASHBOARD }
            MockInterviewScreen(
                questions = questions.take(3),
                onFinish = { currentScreen = Screen.PROGRESS },
                onExit = { currentScreen = Screen.DASHBOARD }
            )
        }
        Screen.PROGRESS -> {
            BackHandler { currentScreen = Screen.DASHBOARD }
            ProgressScreen(
                onBack = { currentScreen = Screen.DASHBOARD },
                onPracticeMore = { currentScreen = Screen.MOCK_INTERVIEW }
            )
        }
    }
}

// -------------------------------------------------------------
// LOGIN SCREEN (Screenshot 2 Match)
// -------------------------------------------------------------
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onGuestContinue: () -> Unit
) {
    var isLoginTab by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("vinoth@example.com") }
    var password by remember { mutableStateOf("••••••••") }
    var rememberMe by remember { mutableStateOf(true) }

    // Wave bar animation
    val transition = rememberInfiniteTransition(label = "wave")
    val waveScale by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveScale"
    )

    Scaffold(
        containerColor = BgMain
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Header brand with glowing turquoise dot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AccentTurquoise)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PREPAI",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Left Promotional Card styling
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BgSecondary),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "INTERVIEW PREPARATION, REHEARSED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAmber,
                            letterSpacing = 1.2.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Walk in already having done this before.",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            lineHeight = 34.sp,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Practice real interview questions, get instant AI feedback on clarity, structure and confidence, and track your readiness before the mic actually goes live.",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = BorderColor)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.height(24.dp)
                            ) {
                                Box(modifier = Modifier.width(4.dp).height((14 * waveScale).dp).background(PrimaryAmber, RoundedCornerShape(2.dp)))
                                Box(modifier = Modifier.width(4.dp).height((22 * (1.2f - waveScale * 0.4f)).dp).background(AccentTurquoise, RoundedCornerShape(2.dp)))
                                Box(modifier = Modifier.width(4.dp).height((10 * waveScale).dp).background(PrimaryAmber, RoundedCornerShape(2.dp)))
                                Box(modifier = Modifier.width(4.dp).height((18 * (1.1f - waveScale * 0.3f)).dp).background(AccentTurquoise, RoundedCornerShape(2.dp)))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "LIVE READINESS SIGNAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Segmented control (Log in / Create account)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50.dp))
                        .background(BgSecondary)
                        .border(1.dp, BorderColor, RoundedCornerShape(50.dp))
                        .padding(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (isLoginTab) PrimaryAmber else Color.Transparent)
                                .clickable { isLoginTab = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Log in",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isLoginTab) Color(0xFF11141B) else TextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (!isLoginTab) PrimaryAmber else Color.Transparent)
                                .clickable { isLoginTab = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Create account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (!isLoginTab) Color(0xFF11141B) else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Welcome back header
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isLoginTab) "Welcome back" else "Create your account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isLoginTab) "Log in to pick up your mock interview where you left off."
                        else "Begin structured mock rounds with instant AI evaluation.",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth().testTag("email_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAmber,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = BgInput,
                        unfocusedContainerColor = BgInput,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth().testTag("password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAmber,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = BgInput,
                        unfocusedContainerColor = BgInput,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(checkedColor = PrimaryAmber)
                        )
                        Text("Remember me", fontSize = 13.sp, color = TextSecondary)
                    }
                    Text(
                        text = "Forgot password",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.clickable { }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { onLoginSuccess(if (email.contains("@")) email.substringBefore("@").replaceFirstChar { it.uppercase() } else "Vinoth") },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("login_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber, contentColor = Color(0xFF11141B))
                ) {
                    Text(text = if (isLoginTab) "Log in" else "Create account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
                    Text(text = "  OR  ", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onGuestContinue,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("guest_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Text(text = "Continue as guest", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "By continuing you agree this is a demo authentication flow using your browser's local storage — no data leaves your device.",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// DASHBOARD SCREEN (Screenshot 1 Match)
// -------------------------------------------------------------
@Composable
fun DashboardScreen(
    userName: String,
    questions: List<Question>,
    onNavigate: (Screen) -> Unit,
    onStartMock: () -> Unit,
    onBrowseBank: () -> Unit,
    onSelectQuestion: (Question) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        containerColor = BgMain,
        topBar = {
            TopNavBar(
                userName = userName,
                activeScreen = Screen.DASHBOARD,
                onNavigate = onNavigate,
                onLogout = onLogout
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            item {
                // GREEN ROOM Welcome Header
                Text(
                    text = "GREEN ROOM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryAmber,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Good to see you, $userName.",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Main Readiness Card (Dark Slate)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ready for your next round?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Run a timed mock interview, get scored on clarity, structure, relevance and confidence, then see exactly what to fix before the real thing.",
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Score & mini bar chart block
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = BgMain.copy(alpha = 0.7f)),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "READINESS SCORE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryAmber,
                                    letterSpacing = 1.1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "72",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryAmber
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Mini performance bars
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    val barScores = listOf(65, 74, 68, 78, 72)
                                    barScores.forEachIndexed { idx, s ->
                                        Box(
                                            modifier = Modifier
                                                .width(10.dp)
                                                .height((s * 0.35f).dp)
                                                .background(
                                                    if (idx % 2 == 1) AccentTurquoise else PrimaryAmber,
                                                    RoundedCornerShape(3.dp)
                                                )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Based on your last 5 practice sessions",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onStartMock,
                                modifier = Modifier.weight(1f).height(46.dp).testTag("start_mock_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber, contentColor = Color(0xFF11141B))
                            ) {
                                Text("Start mock interview", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            OutlinedButton(
                                onClick = onBrowseBank,
                                modifier = Modifier.weight(1f).height(46.dp).testTag("browse_bank_btn"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BorderColor),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                            ) {
                                Text("Browse bank", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4 Statistics Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCardItem(
                        modifier = Modifier.weight(1f),
                        label = "Sessions completed",
                        value = "12",
                        pillText = "+2 this week",
                        isPositive = true
                    )
                    StatCardItem(
                        modifier = Modifier.weight(1f),
                        label = "Average score",
                        value = "74%",
                        pillText = "+4% vs last",
                        isPositive = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCardItem(
                        modifier = Modifier.weight(1f),
                        label = "Day streak",
                        value = "5 days",
                        pillText = "Active",
                        isPositive = true
                    )
                    StatCardItem(
                        modifier = Modifier.weight(1f),
                        label = "Focus suggested",
                        value = "DSA",
                        pillText = "Review",
                        isPositive = false
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Section: Pick up where you left off
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pick up where you left off",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "View all",
                        fontSize = 13.sp,
                        color = PrimaryAmber,
                        modifier = Modifier.clickable { onBrowseBank() }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            items(questions) { q ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { onSelectQuestion(q) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = q.text,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF232A38))
                                    .border(1.dp, BorderColor, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = q.category.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (q.difficulty == "Easy") AccentTurquoise.copy(alpha = 0.15f) else PrimaryAmber.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = q.difficulty,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (q.difficulty == "Easy") AccentTurquoise else PrimaryAmber
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCardItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    pillText: String,
    isPositive: Boolean
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isPositive) AccentTurquoise.copy(alpha = 0.15f) else AccentCoral.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = pillText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) AccentTurquoise else AccentCoral
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

// -------------------------------------------------------------
// TOP NAVIGATION BAR
// -------------------------------------------------------------
@Composable
fun TopNavBar(
    userName: String,
    activeScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    Surface(
        color = BgSecondary,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AccentTurquoise)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PREPAI",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(PrimaryAmber, AccentTurquoise))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF11141B)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = userName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Log out",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(4.dp))
                            .clickable { onLogout() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Navigation Links row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    NavPill("Dashboard", activeScreen == Screen.DASHBOARD) { onNavigate(Screen.DASHBOARD) }
                }
                item {
                    NavPill("Question Bank", activeScreen == Screen.QUESTION_BANK) { onNavigate(Screen.QUESTION_BANK) }
                }
                item {
                    NavPill("Practice", activeScreen == Screen.PRACTICE) { onNavigate(Screen.PRACTICE) }
                }
                item {
                    NavPill("Progress", activeScreen == Screen.PROGRESS) { onNavigate(Screen.PROGRESS) }
                }
            }
        }
    }
}

@Composable
fun NavPill(label: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) Color(0xFF1F2532) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) TextPrimary else TextSecondary
        )
    }
}

// -------------------------------------------------------------
// QUESTION BANK SCREEN
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBankScreen(
    questions: List<Question>,
    bookmarkedIds: List<Int>,
    onToggleBookmark: (Int) -> Unit,
    onPracticeQuestion: (Question) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var inspectingQuestion by remember { mutableStateOf<Question?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val categories = listOf("All", "HR", "Python", "DSA", "SQL", "ML")

    val filtered = questions.filter { q ->
        (selectedCategory == "All" || q.category.contains(selectedCategory, ignoreCase = true)) &&
        (searchQuery.isEmpty() || q.text.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        containerColor = BgMain,
        topBar = {
            TopNavBar(
                userName = "Vinoth",
                activeScreen = Screen.QUESTION_BANK,
                onNavigate = { if (it == Screen.DASHBOARD) onBack() },
                onLogout = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            item {
                Text(
                    text = "Question Bank",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search questions by keyword...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAmber,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = BgInput,
                        unfocusedContainerColor = BgInput,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (isSelected) PrimaryAmber else BgSecondary)
                                .border(1.dp, BorderColor, RoundedCornerShape(50.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF11141B) else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            items(filtered) { q ->
                val isBookmarked = bookmarkedIds.contains(q.id)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = q.text,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f),
                                lineHeight = 21.sp
                            )
                            IconButton(
                                onClick = { onToggleBookmark(q.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isBookmarked) PrimaryAmber else TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tip: ${q.hints}",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF232A38))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(q.category.take(15), fontSize = 10.sp, color = TextSecondary)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(PrimaryAmber.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(q.difficulty, fontSize = 10.sp, color = PrimaryAmber, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = { inspectingQuestion = q }
                                ) {
                                    Text("Benchmark", fontSize = 12.sp, color = AccentTurquoise)
                                }
                                Button(
                                    onClick = { onPracticeQuestion(q) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber, contentColor = Color(0xFF11141B)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Practice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    inspectingQuestion?.let { q ->
        ModalBottomSheet(
            onDismissRequest = { inspectingQuestion = null },
            sheetState = sheetState,
            containerColor = BgCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(text = q.category.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryAmber)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = q.text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "EVALUATION CRITERIA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentTurquoise)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = q.criteria, fontSize = 13.sp, color = TextSecondary, lineHeight = 19.sp)

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "MODEL BENCHMARK ANSWER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryAmber)
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = BgInput),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Text(
                        text = q.sampleAnswer,
                        modifier = Modifier.padding(14.dp),
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        val selected = q
                        inspectingQuestion = null
                        onPracticeQuestion(selected)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber, contentColor = Color(0xFF11141B)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Rehearse this Question", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PRACTICE SCREEN (Interactive Voice / Text Lab)
// -------------------------------------------------------------
@Composable
fun PracticeScreen(
    initialQuestion: Question,
    onBack: () -> Unit,
    onLaunchMock: () -> Unit
) {
    var answerText by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    var evaluationDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    Scaffold(
        containerColor = BgMain,
        topBar = {
            TopNavBar(
                userName = "Vinoth",
                activeScreen = Screen.PRACTICE,
                onNavigate = { if (it == Screen.DASHBOARD) onBack() },
                onLogout = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Practice Lab",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BgSecondary)
                            .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        val mins = secondsElapsed / 60
                        val secs = secondsElapsed % 60
                        Text(
                            text = String.format("%02d:%02d", mins, secs),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF232A38))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(initialQuestion.category.uppercase(), fontSize = 10.sp, color = TextSecondary)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PrimaryAmber.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(initialQuestion.difficulty, fontSize = 10.sp, color = PrimaryAmber, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = initialQuestion.text,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            lineHeight = 26.sp,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Framework Benchmark: ${initialQuestion.hints}",
                            fontSize = 12.sp,
                            color = AccentTurquoise,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Your Response", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)

                    OutlinedButton(
                        onClick = {
                            isRecording = !isRecording
                            if (isRecording && answerText.isEmpty()) {
                                answerText = "In my past engineering internship, our team faced a severe conflict over database choice. I initiated a structured benchmark to assess query latency under load, unblocking the launch."
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isRecording) AccentCoral else BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isRecording) AccentCoral else TextPrimary),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Mic", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isRecording) "Recording..." else "Voice Input", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = answerText,
                    onValueChange = { answerText = it },
                    placeholder = { Text("Deliver your answer clearly. Frame your situation, actions, and quantifiable impact...") },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAmber,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = BgInput,
                        unfocusedContainerColor = BgInput,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { evaluationDone = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber, contentColor = Color(0xFF11141B)),
                    enabled = answerText.isNotEmpty()
                ) {
                    Text("Analyze & Score Answer", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                AnimatedVisibility(visible = evaluationDone) {
                    Column(modifier = Modifier.padding(top = 22.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = BgCard),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("AI EVALUATION REPORT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentTurquoise)
                                        Text("Performance Analysis", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                                    }
                                    Text("82", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryAmber)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DimensionScoreBox("Relevance", "85%", Modifier.weight(1f))
                                    DimensionScoreBox("Tech Depth", "80%", Modifier.weight(1f))
                                    DimensionScoreBox("STAR Structure", "88%", Modifier.weight(1f))
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text("KEY STRENGTHS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentTurquoise)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("• Strong ownership expressed with explicit conflict de-escalation steps.\n• Concise STAR framing with clear objective resolution.", fontSize = 13.sp, color = TextPrimary, lineHeight = 18.sp)

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("CRITICAL IMPROVEMENTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentCoral)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("• Quantify the performance gains achieved (e.g. latency reduced by 30%).", fontSize = 13.sp, color = TextPrimary, lineHeight = 18.sp)

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = onLaunchMock,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = BgSecondary),
                                    border = BorderStroke(1.dp, BorderColor)
                                ) {
                                    Text("Take Full Timed Mock Round", color = PrimaryAmber, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DimensionScoreBox(label: String, score: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(BgInput)
            .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Column {
            Text(label, fontSize = 10.sp, color = TextSecondary)
            Text(score, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AccentTurquoise)
        }
    }
}

// -------------------------------------------------------------
// MOCK INTERVIEW SCREEN
// -------------------------------------------------------------
@Composable
fun MockInterviewScreen(
    questions: List<Question>,
    onFinish: () -> Unit,
    onExit: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var currentAnswer by remember { mutableStateOf("") }
    var timerSeconds by remember { mutableIntStateOf(180) }

    LaunchedEffect(currentIndex) {
        timerSeconds = 180
        while (timerSeconds > 0) {
            delay(1000)
            timerSeconds--
        }
    }

    val currentQ = questions.getOrNull(currentIndex) ?: questions.first()

    Scaffold(
        containerColor = BgMain
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${currentIndex + 1} of ${questions.size}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val mins = timerSeconds / 60
                    val secs = timerSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", mins, secs),
                        fontWeight = FontWeight.Bold,
                        color = if (timerSeconds < 30) AccentCoral else PrimaryAmber,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Exit",
                        fontSize = 12.sp,
                        color = AccentCoral,
                        modifier = Modifier.clickable { onExit() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            val progress = (currentIndex.toFloat() / questions.size.toFloat())
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(BgInput, RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .background(PrimaryAmber, RoundedCornerShape(2.dp))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(currentQ.category.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryAmber)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentQ.text,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 26.sp,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = currentAnswer,
                onValueChange = { currentAnswer = it },
                placeholder = { Text("Provide your answer here...") },
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryAmber,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = BgInput,
                    unfocusedContainerColor = BgInput,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (currentIndex + 1 < questions.size) {
                            currentIndex++
                            currentAnswer = ""
                        } else {
                            onFinish()
                        }
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Text("Skip", color = TextSecondary)
                }

                Button(
                    onClick = {
                        if (currentIndex + 1 < questions.size) {
                            currentIndex++
                            currentAnswer = ""
                        } else {
                            onFinish()
                        }
                    },
                    modifier = Modifier.weight(2f).height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber, contentColor = Color(0xFF11141B))
                ) {
                    Text(if (currentIndex + 1 == questions.size) "Complete & Submit" else "Submit & Next", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PROGRESS ANALYTICS SCREEN
// -------------------------------------------------------------
@Composable
fun ProgressScreen(
    onBack: () -> Unit,
    onPracticeMore: () -> Unit
) {
    Scaffold(
        containerColor = BgMain,
        topBar = {
            TopNavBar(
                userName = "Vinoth",
                activeScreen = Screen.PROGRESS,
                onNavigate = { if (it == Screen.DASHBOARD) onBack() },
                onLogout = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            item {
                Text(
                    text = "Performance Analytics",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCardItem(Modifier.weight(1f), "Readiness Score", "76", "Active", true)
                    StatCardItem(Modifier.weight(1f), "Total Sessions", "14", "+3 this wk", true)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Category Mastery Levels", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(16.dp))

                        MasteryBar("Behavioral & Leadership", 0.88f, AccentTurquoise)
                        Spacer(modifier = Modifier.height(12.dp))
                        MasteryBar("Python Core & Concurrency", 0.82f, PrimaryAmber)
                        Spacer(modifier = Modifier.height(12.dp))
                        MasteryBar("SQL & System Indexing", 0.74f, AccentTurquoise)
                        Spacer(modifier = Modifier.height(12.dp))
                        MasteryBar("Data Structures & Algorithms", 0.68f, PrimaryAmber)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onPracticeMore,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAmber, contentColor = Color(0xFF11141B))
                ) {
                    Text("Start Next Mock Round", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MasteryBar(title: String, fraction: Float, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 13.sp, color = TextSecondary)
            Text("${(fraction * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(BgInput, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
    }
}
