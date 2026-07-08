package com.example

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.ChatMessageEntity
import com.example.data.db.ChatSession
import com.example.data.db.SavedSnippet
import com.example.ui.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current.applicationContext as Application
                    val viewModel: AssistantViewModel = viewModel(
                        factory = AssistantViewModelFactory(context)
                    )
                    DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: AssistantViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val apiKey by viewModel.apiKey.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "IDE Logo",
                            tint = NeonCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DEEPSEEK CODE",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                },
                actions = {
                    val context = LocalContext.current
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (apiKey.isNotEmpty()) TerminalGreen.copy(alpha = 0.15f) else ElectricAmber.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (apiKey.isNotEmpty()) TerminalGreen else ElectricAmber),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (apiKey.isNotEmpty()) TerminalGreen else ElectricAmber, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (apiKey.isNotEmpty()) "KEY_ONLINE" else "NO_KEY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (apiKey.isNotEmpty()) TerminalGreen else ElectricAmber
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CarbonDark,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CarbonDark,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == AppTab.CHAT,
                    onClick = { viewModel.selectTab(AppTab.CHAT) },
                    icon = { Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Chat") },
                    label = { Text("Chat") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CarbonDark,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_chat")
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.DEBUGGER,
                    onClick = { viewModel.selectTab(AppTab.DEBUGGER) },
                    icon = { Icon(Icons.Outlined.BugReport, contentDescription = "Debugger") },
                    label = { Text("Debugger") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CarbonDark,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_debugger")
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.SNIPPETS,
                    onClick = { viewModel.selectTab(AppTab.SNIPPETS) },
                    icon = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Snippets") },
                    label = { Text("Snippets") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CarbonDark,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_snippets")
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.SETTINGS,
                    onClick = { viewModel.selectTab(AppTab.SETTINGS) },
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CarbonDark,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CarbonDark)
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppTab.CHAT -> ChatScreen(viewModel = viewModel)
                AppTab.DEBUGGER -> DebuggerScreen(viewModel = viewModel)
                AppTab.SNIPPETS -> SnippetsScreen(viewModel = viewModel)
                AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// CHAT TAB
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: AssistantViewModel) {
    val chatSessions by viewModel.chatSessions.collectAsStateWithLifecycle()
    val messages by viewModel.messagesState.collectAsStateWithLifecycle()
    val activeSessionId by viewModel.activeSessionId.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    var inputMessage by remember { mutableStateOf("") }
    var showSessionMenu by remember { mutableStateOf(false) }

    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto scroll to bottom when a new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Session selector / action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Selected Session title indicator
            val currentSession = chatSessions.find { it.id == activeSessionId }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showSessionMenu = true }
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Sessions history",
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = currentSession?.title ?: "Select/Create Session",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ),
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Drop Down",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action: New Session Button
            IconButton(
                onClick = { viewModel.createNewSession() },
                modifier = Modifier
                    .background(SlateSurface, RoundedCornerShape(8.dp))
                    .size(36.dp)
                    .testTag("new_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create New Session",
                    tint = NeonCyan
                )
            }

            if (currentSession != null) {
                Spacer(modifier = Modifier.width(8.dp))
                // Action: Delete Session Button
                IconButton(
                    onClick = { viewModel.deleteSession(currentSession.id) },
                    modifier = Modifier
                        .background(SlateSurface, RoundedCornerShape(8.dp))
                        .size(36.dp)
                        .testTag("delete_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Session",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Sessions List Modal / Dropdown Menu
        if (showSessionMenu) {
            AlertDialog(
                onDismissRequest = { showSessionMenu = false },
                title = { Text("Conversations History") },
                text = {
                    Box(modifier = Modifier.heightIn(max = 300.dp)) {
                        if (chatSessions.isEmpty()) {
                            Text("No active sessions. Create a new one!")
                        } else {
                            LazyColumn {
                                items(chatSessions) { session ->
                                    val isSelected = session.id == activeSessionId
                                    ListItem(
                                        headlineContent = {
                                            Text(
                                                text = session.title,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) NeonCyan else TextPrimary
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectSession(session.id)
                                                showSessionMenu = false
                                            }
                                            .background(if (isSelected) SlateSurface else Color.Transparent)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSessionMenu = false }) {
                        Text("Close")
                    }
                },
                containerColor = SlateSurface,
                titleContentColor = TextPrimary,
                textContentColor = TextPrimary
            )
        }

        Divider(color = CodeBorder)

        // Messages list
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                // Onboarding empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Coding Helper",
                        tint = NeonCyan.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "DeepSeek Coding Terminal",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter any coding prompt, ask for architecture, algorithms, or code blocks in Kotlin, Python, JS and beyond. Your history is stored safely locally.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                    items(messages) { message ->
                        ChatMessageItem(
                            message = message,
                            onSaveSnippet = { title, desc, code, lang ->
                                viewModel.saveCodeSnippet(title, desc, code, lang)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    if (isGenerating) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = NeonCyan,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Generating response...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
            }
        }

        // Send Input Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = {
                        Text(
                            "Type coding prompt...",
                            color = TextSecondary
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CodeBorder,
                        focusedContainerColor = CharcoalVariant,
                        unfocusedContainerColor = CharcoalVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputMessage.trim().isNotEmpty()) {
                            viewModel.sendMessage(inputMessage)
                            inputMessage = ""
                        }
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (inputMessage.trim().isEmpty() || isGenerating) CharcoalVariant else NeonCyan)
                        .size(48.dp)
                        .testTag("send_button"),
                    enabled = inputMessage.trim().isNotEmpty() && !isGenerating
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputMessage.trim().isEmpty() || isGenerating) TextSecondary else CarbonDark
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// CHAT MESSAGE ITEM (W/ CODE BLOCKS)
// -----------------------------------------------------------------------------

data class MessageSegment(
    val content: String,
    val isCode: Boolean,
    val language: String = "code"
)

fun parseMessageContent(text: String): List<MessageSegment> {
    val segments = mutableListOf<MessageSegment>()
    val parts = text.split("```")
    for (i in parts.indices) {
        val part = parts[i]
        if (i % 2 == 1) { // Inside a code block
            val firstLineBreak = part.indexOf('\n')
            if (firstLineBreak != -1) {
                val lang = part.substring(0, firstLineBreak).trim()
                val code = part.substring(firstLineBreak + 1)
                segments.add(
                    MessageSegment(
                        content = code,
                        isCode = true,
                        language = lang.ifEmpty { "code" }
                    )
                )
            } else {
                segments.add(MessageSegment(content = part, isCode = true, language = "code"))
            }
        } else { // Regular text
            if (part.isNotEmpty()) {
                segments.add(MessageSegment(content = part, isCode = false))
            }
        }
    }
    return segments
}

@Composable
fun ChatMessageItem(
    message: ChatMessageEntity,
    onSaveSnippet: (String, String, String, String) -> Unit
) {
    val isUser = message.role == "user"
    val segments = remember(message.content) { parseMessageContent(message.content) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Role Header
        Text(
            text = if (isUser) "USER" else "DEEPSEEK_BOT",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (isUser) TextSecondary else NeonCyan
            ),
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
        )

        // Message container
        Column(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 12.dp
                    )
                )
                .background(if (isUser) SlateSurface else CharcoalVariant)
                .border(
                    1.dp,
                    if (isUser) CodeBorder else NeonCyan.copy(alpha = 0.3f),
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 12.dp
                    )
                )
                .padding(12.dp)
        ) {
            segments.forEach { segment ->
                if (segment.isCode) {
                    Spacer(modifier = Modifier.height(6.dp))
                    CodeBlockComponent(
                        code = segment.content,
                        language = segment.language,
                        onSaveSnippet = onSaveSnippet
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                } else {
                    Text(
                        text = segment.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CodeBlockComponent(
    code: String,
    language: String,
    onSaveSnippet: (String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isSaved by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CarbonDark)
            .border(1.dp, CodeBorder, RoundedCornerShape(8.dp))
    ) {
        // Gutter-Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CharcoalVariant)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            )
            Row {
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Copied Code", code)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        onSaveSnippet("Snippet - $language", "Code generated via DeepSeek", code, language)
                        isSaved = true
                        Toast.makeText(context, "Saved to Snippets", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp),
                    enabled = !isSaved
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Bookmark,
                        contentDescription = "Save Snippet",
                        tint = if (isSaved) TerminalGreen else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Code and Lines
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            val lines = code.trimEnd().split("\n")
            val lineNumbers = lines.indices.map { (it + 1).toString() }.joinToString("\n")

            Text(
                text = lineNumbers,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary.copy(alpha = 0.5f),
                    textAlign = TextAlign.End
                ),
                modifier = Modifier.padding(end = 12.dp)
            )

            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
            )
        }
    }
}

// -----------------------------------------------------------------------------
// DEBUGGER WORKSPACE TAB
// -----------------------------------------------------------------------------

@Composable
fun DebuggerScreen(viewModel: AssistantViewModel) {
    val debuggerCode by viewModel.debuggerCode.collectAsStateWithLifecycle()
    val debuggerResult by viewModel.debuggerResult.collectAsStateWithLifecycle()
    val debuggerAction by viewModel.debuggerAction.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Code Analysis & Debugging",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Submit a block of code to find bugs, optimize execution, get explanations, or generate tests.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Code Input Panel
        OutlinedTextField(
            value = debuggerCode,
            onValueChange = { viewModel.setDebuggerCode(it) },
            placeholder = {
                Text(
                    "Paste your source code here (e.g., function, full file, query)...",
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .testTag("debugger_input_code"),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = CodeBorder,
                focusedContainerColor = CharcoalVariant,
                unfocusedContainerColor = CharcoalVariant
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Debugger Actions
        Text(
            text = "CHOOSE ANALYSIS TYPE",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DebuggerActionChip(
                label = "EXPLAIN",
                selected = debuggerAction == DebuggerAction.EXPLAIN,
                onClick = { viewModel.setDebuggerAction(DebuggerAction.EXPLAIN) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            DebuggerActionChip(
                label = "FIX BUGS",
                selected = debuggerAction == DebuggerAction.DEBUG,
                onClick = { viewModel.setDebuggerAction(DebuggerAction.DEBUG) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            DebuggerActionChip(
                label = "OPTIMIZE",
                selected = debuggerAction == DebuggerAction.OPTIMIZE,
                onClick = { viewModel.setDebuggerAction(DebuggerAction.OPTIMIZE) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            DebuggerActionChip(
                label = "TESTS",
                selected = debuggerAction == DebuggerAction.UNIT_TEST,
                onClick = { viewModel.setDebuggerAction(DebuggerAction.UNIT_TEST) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Run Action Button
        Button(
            onClick = { viewModel.executeDebuggerAction() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("run_debugger_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (debuggerCode.trim().isEmpty() || isGenerating) CharcoalVariant else NeonCyan,
                contentColor = CarbonDark
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = debuggerCode.trim().isNotEmpty() && !isGenerating
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = CarbonDark,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Analyzing codebase...", fontWeight = FontWeight.Bold)
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Run",
                    tint = CarbonDark
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EXECUTE DEEPSEEK ANALYZER",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Results Section
        debuggerResult?.let { result ->
            Text(
                text = "ANALYSIS REPORT OUTPUT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val parsedSegments = remember(result) { parseMessageContent(result) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SlateSurface)
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                parsedSegments.forEach { segment ->
                    if (segment.isCode) {
                        CodeBlockComponent(
                            code = segment.content,
                            language = segment.language,
                            onSaveSnippet = { title, desc, code, lang ->
                                viewModel.saveCodeSnippet(title, desc, code, lang)
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        Text(
                            text = segment.content,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DebuggerActionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .border(
                1.dp,
                if (selected) NeonCyan else CodeBorder,
                RoundedCornerShape(8.dp)
            ),
        color = if (selected) NeonCyan.copy(alpha = 0.15f) else SlateSurface
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) NeonCyan else TextSecondary
                )
            )
        }
    }
}

// -----------------------------------------------------------------------------
// SNIPPETS HUB TAB
// -----------------------------------------------------------------------------

@Composable
fun SnippetsScreen(viewModel: AssistantViewModel) {
    val snippets by viewModel.savedSnippets.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    val filteredSnippets = remember(snippets, searchQuery) {
        snippets.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.language.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Saved Code Repository",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Your localized repository of codes and generated templates.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by title, desc or language...", color = TextSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("snippets_search_field"),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = CodeBorder,
                focusedContainerColor = SlateSurface,
                unfocusedContainerColor = SlateSurface
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredSnippets.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "No snippets",
                    tint = TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (searchQuery.isNotEmpty()) "No snippets matched search" else "No saved snippets",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredSnippets) { snippet ->
                    SnippetCard(
                        snippet = snippet,
                        onDelete = { viewModel.deleteSnippet(snippet) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun SnippetCard(
    snippet: SavedSnippet,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SlateSurface)
            .border(1.dp, CodeBorder, RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = snippet.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = snippet.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = NeonCyan.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
            ) {
                Text(
                    text = snippet.language.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                // Code block viewing
                CodeBlockComponent(
                    code = snippet.code,
                    language = snippet.language,
                    onSaveSnippet = { _, _, _, _ -> }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Copied Snippet", snippet.code)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Snippet copied", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code snippet",
                            tint = NeonCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete snippet",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SETTINGS TAB
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: AssistantViewModel) {
    val apiKey by viewModel.apiKey.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    var localKeyInput by remember { mutableStateOf(apiKey) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Developer Configurations",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Configure core settings, API endpoints, and models.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // API Key Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = SlateSurface,
            border = BorderStroke(1.dp, CodeBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DEEPSEEK API KEYS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your API key is securely saved. You can modify it locally to change endpoints or permissions.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = localKeyInput,
                    onValueChange = { localKeyInput = it },
                    label = { Text("DeepSeek API Key", color = TextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_api_key_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CodeBorder,
                        focusedContainerColor = CharcoalVariant,
                        unfocusedContainerColor = CharcoalVariant
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                val context = LocalContext.current
                Button(
                    onClick = {
                        viewModel.updateApiKey(localKeyInput)
                        Toast.makeText(context, "API Key updated successfully!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = CarbonDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("SAVE API KEY", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Model selector section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = SlateSurface,
            border = BorderStroke(1.dp, CodeBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "AI MODEL PARAMETERS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Choose the model best suited for your coding queries.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Model selection:",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                    )

                    Row {
                        FilterChip(
                            selected = selectedModel == "deepseek-chat",
                            onClick = { viewModel.selectModel("deepseek-chat") },
                            label = { Text("deepseek-chat") },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = TextSecondary,
                                selectedLabelColor = CarbonDark,
                                selectedContainerColor = NeonCyan
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = selectedModel == "deepseek-coder",
                            onClick = { viewModel.selectModel("deepseek-coder") },
                            label = { Text("deepseek-coder") },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = TextSecondary,
                                selectedLabelColor = CarbonDark,
                                selectedContainerColor = NeonCyan
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sandbox & No Restriction Info
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = SlateSurface,
            border = BorderStroke(1.dp, CodeBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Unrestricted Mode",
                        tint = ElectricAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "UNRESTRICTED CODER ENGINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = ElectricAmber,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "This environment is set to fully unrestricted. Output filters and system instructions are tuned to optimize and explain any complex code segments or systems requested without safety interference.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 16.sp)
                )
            }
        }
    }
}
