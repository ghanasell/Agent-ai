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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.foundation.text.BasicTextField
import com.example.data.db.ProjectFile
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = NeonCyan, // Primary brand color
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = "BASE 2 Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "BASE 2",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.3).sp,
                                    color = TextPrimary
                                )
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 1.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            if (apiKey.isNotEmpty()) TerminalGreen else ElectricAmber,
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (apiKey.isNotEmpty()) "BASE 2 Active" else "BASE 2 Inactive",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp,
                                        color = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Settings toggle
                    IconButton(
                        onClick = {
                            if (selectedTab == AppTab.SETTINGS) {
                                viewModel.selectTab(AppTab.CHAT)
                            } else {
                                viewModel.selectTab(AppTab.SETTINGS)
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedTab == AppTab.SETTINGS) Icons.Default.ChatBubble else Icons.Default.Settings,
                            contentDescription = "Toggle Settings",
                            tint = TextSecondary
                        )
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
                        selectedIconColor = Color.White,
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
                        selectedIconColor = Color.White,
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
                        selectedIconColor = Color.White,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_snippets")
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.MARKETS,
                    onClick = { viewModel.selectTab(AppTab.MARKETS) },
                    icon = { Icon(Icons.Default.ShowChart, contentDescription = "Markets") },
                    label = { Text("Markets") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_markets")
                )
                NavigationBarItem(
                    selected = selectedTab == AppTab.TOOLS,
                    onClick = { viewModel.selectTab(AppTab.TOOLS) },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Workspace") },
                    label = { Text("Workspace") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tab_tools")
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
                AppTab.MARKETS -> MarketsScreen(viewModel = viewModel)
                AppTab.SNIPPETS -> SnippetsScreen(viewModel = viewModel)
                AppTab.TOOLS -> ToolsScreen(viewModel = viewModel)
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
                        text = "BASE 2 CODE",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter any coding prompt to communicate with the BASE 2 CODE agent. Ask for architecture, algorithms, or code blocks in Kotlin, Python, JS, and beyond. Your workspace is stored safely locally.",
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
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 16.dp
                    )
                )
                .background(if (isUser) UserBubbleBg else SlateSurface)
                .border(
                    1.dp,
                    if (isUser) Color.Transparent else CodeBorder,
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 16.dp
                    )
                )
                .padding(14.dp)
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
                            color = if (isUser) UserBubbleText else TextPrimary,
                            lineHeight = 22.sp
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
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .border(1.dp, Color(0xFF2D2D30), RoundedCornerShape(12.dp))
    ) {
        // Gutter-Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2D2D30))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF00E5FF), // Vibrant cyan accent inside code blocks
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
                        tint = Color(0xFF9CA3AF),
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
                        tint = if (isSaved) Color(0xFF00E676) else Color(0xFF9CA3AF),
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
                .padding(14.dp)
        ) {
            val lines = code.trimEnd().split("\n")
            val lineNumbers = lines.indices.map { (it + 1).toString() }.joinToString("\n")

            Text(
                text = lineNumbers,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF858585),
                    textAlign = TextAlign.End
                ),
                modifier = Modifier.padding(end = 12.dp)
            )

            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFD4D4D4)
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
    val context = LocalContext.current
    val debuggerCode by viewModel.debuggerCode.collectAsStateWithLifecycle()
    val debuggerResult by viewModel.debuggerResult.collectAsStateWithLifecycle()
    val debuggerAction by viewModel.debuggerAction.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()

    val localWarnings by viewModel.localWarnings.collectAsStateWithLifecycle()
    val parsedIssues by viewModel.parsedIssues.collectAsStateWithLifecycle()
    val parsedFixedCode by viewModel.parsedFixedCode.collectAsStateWithLifecycle()
    val projectFiles by viewModel.projectFiles.collectAsStateWithLifecycle()

    var showWorkspaceDropdown by remember { mutableStateOf(false) }
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
            text = "Submit source code to locate syntactic & semantic defects, optimize, explain, or generate unit tests.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- SOURCE CODE INPUT HEADER & LOAD FILE ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SOURCE CODE INPUT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            )

            if (projectFiles.isNotEmpty()) {
                Box {
                    TextButton(
                        onClick = { showWorkspaceDropdown = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = NeonCyan),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Load File", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LOAD WORKSPACE FILE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = showWorkspaceDropdown,
                        onDismissRequest = { showWorkspaceDropdown = false }
                    ) {
                        projectFiles.forEach { file ->
                            DropdownMenuItem(
                                text = { Text(file.path, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.setDebuggerCode(file.content)
                                    showWorkspaceDropdown = false
                                    Toast.makeText(context, "Loaded ${file.path}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        // Code Input Panel
        OutlinedTextField(
            value = debuggerCode,
            onValueChange = { viewModel.setDebuggerCode(it) },
            placeholder = {
                Text(
                    "Paste your source code here or select a workspace file above...",
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

        // --- REALTIME LINT WARNINGS PANEL ---
        Spacer(modifier = Modifier.height(8.dp))
        if (localWarnings.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0x20FF5252),
                border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = "Errors", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REAL-TIME SYNTAX CHECK: ${localWarnings.size} ISSUE(S) DETECTED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5252),
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    localWarnings.take(3).forEach { warning ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(
                                text = if (warning.line != null) "• [Line ${warning.line}]: " else "• ",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFFF8A8A),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = warning.message,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (localWarnings.size > 3) {
                        Text(
                            text = "... and ${localWarnings.size - 3} more issue(s)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                        )
                    }
                }
            }
        } else if (debuggerCode.trim().isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0x1500FFCC),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Healthy", tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Real-time syntax check healthy.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NeonCyan,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Debugger Actions Grid Layout
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DebuggerActionChip(
                label = "AUTO-CHECK",
                selected = debuggerAction == DebuggerAction.AUTO_CHECK,
                onClick = { viewModel.setDebuggerAction(DebuggerAction.AUTO_CHECK) },
                modifier = Modifier.weight(1f)
            )
            DebuggerActionChip(
                label = "EXPLAIN",
                selected = debuggerAction == DebuggerAction.EXPLAIN,
                onClick = { viewModel.setDebuggerAction(DebuggerAction.EXPLAIN) },
                modifier = Modifier.weight(1f)
            )
            DebuggerActionChip(
                label = "FIX BUGS",
                selected = debuggerAction == DebuggerAction.DEBUG,
                onClick = { viewModel.setDebuggerAction(DebuggerAction.DEBUG) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DebuggerActionChip(
                label = "OPTIMIZE",
                selected = debuggerAction == DebuggerAction.OPTIMIZE,
                onClick = { viewModel.setDebuggerAction(DebuggerAction.OPTIMIZE) },
                modifier = Modifier.weight(1f)
            )
            DebuggerActionChip(
                label = "TESTS",
                selected = debuggerAction == DebuggerAction.UNIT_TEST,
                onClick = { viewModel.setDebuggerAction(DebuggerAction.UNIT_TEST) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.weight(1f)) // Balancing item placeholder
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

        // --- RESULTS AND INTERACTIVE DIAGNOSIS ---
        debuggerResult?.let { result ->
            Spacer(modifier = Modifier.height(24.dp))

            if (debuggerAction == DebuggerAction.AUTO_CHECK) {
                Text(
                    text = "AUTO-CHECK DIAGNOSIS DASHBOARD",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (parsedIssues.isEmpty()) {
                    if (result.contains("[ISSUE]") || result.contains("Type:")) {
                        // AI outputted some issues but format was slightly off, show raw/standard Markdown
                        RawMarkdownResultCard(result, viewModel)
                    } else {
                        // Show clean success card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0x1500FFCC)),
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Clean", tint = NeonCyan, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("NO DEFECTS DETECTED", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NeonCyan))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "DeepSeek Analyzer scanned the code and found zero critical bugs, logical flaws, or syntax violations. The code conforms to modern standards.",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                                )
                            }
                        }
                    }
                }

                if (parsedIssues.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        parsedIssues.forEach { issue ->
                            val severityColor = when (issue.type.trim().uppercase()) {
                                "ERROR" -> Color(0xFFFF5252)
                                "WARNING" -> Color(0xFFFFB300)
                                else -> Color(0xFF29B6F6)
                            }
                            
                            val severityBg = severityColor.copy(alpha = 0.12f)

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = SlateSurface,
                                border = BorderStroke(1.dp, severityColor.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = severityBg,
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, severityColor.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = issue.type.uppercase(),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = severityColor,
                                                    fontFamily = FontFamily.Monospace
                                                ),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }

                                        Text(
                                            text = "LINE: ${issue.line}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TextSecondary,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = issue.summary,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = issue.explanation,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Divider(color = CodeBorder)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "RECOMMENDED FIX:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = issue.fix,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TerminalGreen,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Show Fixed Code Block
                parsedFixedCode?.let { fixedCode ->
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "CORRECTED SOURCE CODE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = PolishCodeBackground,
                        border = BorderStroke(1.dp, CodeBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SUGGESTED CORRECTION",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = TextSecondary
                                    )
                                )

                                Button(
                                    onClick = { 
                                        viewModel.applyDebuggerFix(fixedCode)
                                        Toast.makeText(context, "Correction applied to input text!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Apply Fix", modifier = Modifier.size(14.dp), tint = CarbonDark)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("APPLY FIX", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CarbonDark)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = fixedCode,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = PolishCodeText
                                ),
                                modifier = Modifier.horizontalScroll(rememberScrollState())
                            )
                        }
                    }
                }

                // Fallback to raw markdown if parsing returned empty lists/nulls
                if (parsedIssues.isEmpty() && parsedFixedCode == null) {
                    RawMarkdownResultCard(result, viewModel)
                }
            } else {
                RawMarkdownResultCard(result, viewModel)
            }
        }
    }
}

@Composable
fun RawMarkdownResultCard(result: String, viewModel: AssistantViewModel) {
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

// -----------------------------------------------------------------------------
// WORKSPACE, WEB SEARCH, AND GITHUB TOOLS SCREEN
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(viewModel: AssistantViewModel) {
    val context = LocalContext.current
    val currentProject by viewModel.currentProject.collectAsStateWithLifecycle()
    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val projectFiles by viewModel.projectFiles.collectAsStateWithLifecycle()
    val toolLogs by viewModel.toolLogs.collectAsStateWithLifecycle()
    val webSearchResults by viewModel.webSearchResults.collectAsStateWithLifecycle()
    val isSearchingWeb by viewModel.isSearchingWeb.collectAsStateWithLifecycle()
    val webSearchEnabled by viewModel.webSearchEnabled.collectAsStateWithLifecycle()
    
    val githubUsername by viewModel.githubUsername.collectAsStateWithLifecycle()
    val githubRepo by viewModel.githubRepo.collectAsStateWithLifecycle()
    val githubToken by viewModel.githubToken.collectAsStateWithLifecycle()
    val githubPushStatus by viewModel.githubPushStatus.collectAsStateWithLifecycle()
    val githubIsPushing by viewModel.githubIsPushing.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf("workspace") } // "workspace", "search", "github"
    
    // Create Project dialog state
    var showCreateProjectDialog by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var newProjectDesc by remember { mutableStateOf("") }

    // Project Dropdown state
    var showProjectDropdown by remember { mutableStateOf(false) }

    // New File state
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFilePath by remember { mutableStateOf("") }
    var newFileContent by remember { mutableStateOf("") }

    // Selected File view
    var selectedFileForView by remember { mutableStateOf<ProjectFile?>(null) }
    var isEditingSelectedFile by remember { mutableStateOf(false) }
    var editingFileContent by remember { mutableStateOf("") }

    // Web search state
    var searchQuery by remember { mutableStateOf("") }

    // GitHub Push dialog state
    var showPushDialog by remember { mutableStateOf(false) }
    var commitMessageInput by remember { mutableStateOf("Initial commit via BASE 2 CODE") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- PROJECTS HEADER SELECTOR ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = SlateSurface,
            border = BorderStroke(1.dp, CodeBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PROJECT WORKSPACE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentProject?.name ?: "Loading Project...",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    Box {
                        Button(
                            onClick = { showProjectDropdown = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = CarbonDark
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("PROJECTS", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Project", tint = CarbonDark)
                        }

                        DropdownMenu(
                            expanded = showProjectDropdown,
                            onDismissRequest = { showProjectDropdown = false }
                        ) {
                            allProjects.forEach { project ->
                                DropdownMenuItem(
                                    text = { Text(project.name, fontWeight = FontWeight.SemiBold) },
                                    onClick = {
                                        viewModel.selectProject(project.id)
                                        showProjectDropdown = false
                                    }
                                )
                            }
                            Divider()
                            DropdownMenuItem(
                                text = { Text("+ CREATE NEW PROJECT", color = NeonCyan, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    showCreateProjectDialog = true
                                    showProjectDropdown = false
                                }
                            )
                        }
                    }
                }
                
                if (currentProject != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentProject?.description ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SUB TABS NAVIGATION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "workspace" to "Workspace Explorer",
                "search" to "Web Search",
                "github" to "GitHub Sync"
            ).forEach { (tabId, label) ->
                val isSelected = activeSubTab == tabId
                Button(
                    onClick = { activeSubTab = tabId },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) NeonCyan else CharcoalVariant,
                        contentColor = if (isSelected) CarbonDark else TextPrimary
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- ACTIVE SUB TAB VIEW CONTENT ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (activeSubTab) {
                "workspace" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "WORKSPACE FILES",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Button(
                                onClick = {
                                    newFilePath = ""
                                    newFileContent = ""
                                    showNewFileDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = CarbonDark
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "New File", modifier = Modifier.size(16.dp), tint = CarbonDark)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New File", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Files grid or list
                        Surface(
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = SlateSurface,
                            border = BorderStroke(1.dp, CodeBorder)
                        ) {
                            if (projectFiles.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No files in project.\nClick 'New File' to write your first file.",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(projectFiles) { file ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (selectedFileForView?.id == file.id) UserBubbleBg else CarbonDark)
                                                .clickable {
                                                    selectedFileForView = file
                                                    editingFileContent = file.content
                                                    isEditingSelectedFile = false
                                                }
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Code,
                                                    contentDescription = "Code file",
                                                    tint = NeonCyan,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = file.path,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextPrimary,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteFile(file.path)
                                                    if (selectedFileForView?.id == file.id) {
                                                        selectedFileForView = null
                                                    }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Selected File Viewer/Editor
                        Text(
                            text = "FILE VIEWER & WRITER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Surface(
                            modifier = Modifier
                                .weight(1.8f)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = PolishCodeBackground,
                            border = BorderStroke(1.dp, CodeBorder)
                        ) {
                            if (selectedFileForView == null) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Select a file to view or edit.",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = PolishCodeText, fontFamily = FontFamily.Monospace)
                                    )
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedFileForView?.path ?: "",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontFamily = FontFamily.Monospace,
                                                color = Color.LightGray,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )

                                        Row {
                                            if (isEditingSelectedFile) {
                                                IconButton(
                                                    onClick = {
                                                        viewModel.writeFile(selectedFileForView!!.path, editingFileContent)
                                                        selectedFileForView = selectedFileForView!!.copy(content = editingFileContent)
                                                        isEditingSelectedFile = false
                                                        Toast.makeText(context, "Saved Successfully!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.Green, modifier = Modifier.size(18.dp))
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                IconButton(
                                                    onClick = {
                                                        isEditingSelectedFile = false
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.Red, modifier = Modifier.size(18.dp))
                                                }
                                            } else {
                                                IconButton(
                                                    onClick = {
                                                        isEditingSelectedFile = true
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider(color = Color.DarkGray)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                                        if (isEditingSelectedFile) {
                                            BasicTextField(
                                                value = editingFileContent,
                                                onValueChange = { editingFileContent = it },
                                                textStyle = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    color = PolishCodeText
                                                ),
                                                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                                            )
                                        } else {
                                            Text(
                                                text = selectedFileForView?.content ?: "",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    color = PolishCodeText
                                                ),
                                                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "search" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = SlateSurface,
                            border = BorderStroke(1.dp, CodeBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "WEB SEARCH SETTINGS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.5f)) {
                                        Text(
                                            text = "Auto search & inject context",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Enables background internet lookups during chat queries.",
                                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                        )
                                    }
                                    Switch(
                                        checked = webSearchEnabled,
                                        onCheckedChange = { viewModel.toggleWebSearch(it) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "ONLINE INTERACTIVE LOOKUP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Enter search keywords...", color = TextSecondary) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = CodeBorder
                                )
                            )

                            Button(
                                onClick = { viewModel.performWebSearch(searchQuery) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(8.dp),
                                enabled = searchQuery.trim().isNotEmpty() && !isSearchingWeb
                            ) {
                                if (isSearchingWeb) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = CarbonDark, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Search, contentDescription = "Search", tint = CarbonDark)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Results
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = SlateSurface,
                            border = BorderStroke(1.dp, CodeBorder)
                        ) {
                            if (webSearchResults.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No search results displayed.\nType a query above to fetch online coding references.",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(webSearchResults) { result ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(CarbonDark)
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    color = UserBubbleBg,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = result.source,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = UserBubbleText,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }

                                                Text(
                                                    text = result.url.take(30) + "...",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontFamily = FontFamily.Monospace)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = result.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = NeonCyan
                                                )
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = result.snippet,
                                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "github" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = SlateSurface,
                            border = BorderStroke(1.dp, CodeBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "GITHUB CREDENTIALS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = githubUsername,
                                    onValueChange = { viewModel.updateGithubSettings(it, githubRepo, githubToken) },
                                    label = { Text("GitHub Username", color = TextSecondary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = CodeBorder
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = githubRepo,
                                    onValueChange = { viewModel.updateGithubSettings(githubUsername, it, githubToken) },
                                    label = { Text("Repository Name", color = TextSecondary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = CodeBorder
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = githubToken,
                                    onValueChange = { viewModel.updateGithubSettings(githubUsername, githubRepo, it) },
                                    label = { Text("Personal Access Token (PAT)", color = TextSecondary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = CodeBorder
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // GitHub push actions
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = SlateSurface,
                            border = BorderStroke(1.dp, CodeBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "PUSH PROJECT TO REMOTE BRANCH",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Pushes all files in the current active workspace directly to your GitHub repository.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { showPushDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = !githubIsPushing
                                ) {
                                    Icon(Icons.Default.Upload, contentDescription = "Push to GitHub", tint = CarbonDark)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("PUSH ALL FILES", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = CarbonDark))
                                }

                                if (githubPushStatus != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        color = CarbonDark,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "PUSH PROGRESS LOG:",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = githubPushStatus ?: "",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    color = if (githubPushStatus!!.contains("Success")) TerminalGreen else if (githubPushStatus!!.contains("Error")) Color.Red else TextPrimary
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- REALTIME TOOL OPERATION LOGS (FOOTER) ---
        Text(
            text = "REALTIME OPERATIONAL CONSOLE",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = PolishCodeBackground,
            border = BorderStroke(1.dp, CodeBorder)
        ) {
            val listState = rememberLazyListState()
            LaunchedEffect(toolLogs.size) {
                if (toolLogs.isNotEmpty()) {
                    listState.animateScrollToItem(toolLogs.size - 1)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(toolLogs) { log ->
                    Text(
                        text = log,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = if (log.contains("Error")) Color.Red else if (log.contains("WriteFile")) Color.Cyan else if (log.contains("GitHub")) Color.Yellow else PolishCodeText
                        )
                    )
                }
            }
        }
    }

    // --- CREATE PROJECT DIALOG ---
    if (showCreateProjectDialog) {
        AlertDialog(
            onDismissRequest = { showCreateProjectDialog = false },
            title = { Text("Create New Workspace Project", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newProjectName,
                        onValueChange = { newProjectName = it },
                        label = { Text("Project Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newProjectDesc,
                        onValueChange = { newProjectDesc = it },
                        label = { Text("Project Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProjectName.trim().isNotEmpty()) {
                            viewModel.createProject(newProjectName, newProjectDesc)
                            showCreateProjectDialog = false
                            newProjectName = ""
                            newProjectDesc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("CREATE", color = CarbonDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateProjectDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }

    // --- NEW FILE DIALOG ---
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Write Workspace File", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newFilePath,
                        onValueChange = { newFilePath = it },
                        label = { Text("Relative File Path (e.g., src/App.kt)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newFileContent,
                        onValueChange = { newFileContent = it },
                        label = { Text("File Content") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 10
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFilePath.trim().isNotEmpty()) {
                            viewModel.writeFile(newFilePath, newFileContent)
                            showNewFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("WRITE FILE", color = CarbonDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }

    // --- GITHUB COMMIT & PUSH DIALOG ---
    if (showPushDialog) {
        AlertDialog(
            onDismissRequest = { showPushDialog = false },
            title = { Text("Commit & Push Workspace", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Commit message:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = commitMessageInput,
                        onValueChange = { commitMessageInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.pushProjectToGitHub(commitMessageInput)
                        showPushDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("COMMIT & PUSH", color = CarbonDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPushDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }
}
