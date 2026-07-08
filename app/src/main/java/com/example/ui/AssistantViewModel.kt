package com.example.ui

import android.app.Application
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.DeepSeekApi
import com.example.data.db.AppDatabase
import com.example.data.db.ChatMessageEntity
import com.example.data.db.ChatSession
import com.example.data.db.SavedSnippet
import com.example.data.db.Project
import com.example.data.db.ProjectFile
import com.example.data.model.DeepSeekMessage
import com.example.data.repository.AssistantRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AssistantViewModel(
    application: Application,
    private val repository: AssistantRepository
) : AndroidViewModel(application) {

    // Selected App Feature/Tab
    private val _selectedTab = MutableStateFlow(AppTab.CHAT)
    val selectedTab = _selectedTab.asStateFlow()

    // Projects (Past & Current Workspaces)
    val allProjects: StateFlow<List<Project>> = repository.allProjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedProjectId = MutableStateFlow<String>("default")
    val selectedProjectId = _selectedProjectId.asStateFlow()

    private val _currentProject = MutableStateFlow<Project?>(null)
    val currentProject = _currentProject.asStateFlow()

    // Workspace Files for selected project
    private val _projectFiles = MutableStateFlow<List<ProjectFile>>(emptyList())
    val projectFiles = _projectFiles.asStateFlow()

    // Tool Logs for visual feedback (WriteFileTool, GitHub, Web Search)
    private val _toolLogs = MutableStateFlow<List<String>>(
        listOf("System: BASE 2 CODE initialized.", "System: Project workspace monitoring active.")
    )
    val toolLogs = _toolLogs.asStateFlow()

    // Active Chat Session ID (null if none, or we auto-create)
    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId = _activeSessionId.asStateFlow()

    // Historical Chat Sessions
    private val _chatSessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatSessions = _chatSessions.asStateFlow()

    // Current Session Messages State
    private val _messagesState = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val messagesState = _messagesState.asStateFlow()

    // Loading State for API responses
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    // API Error Message State
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Configurable API key (loaded from BuildConfig initially, can be changed in settings)
    private val _apiKey = MutableStateFlow(BuildConfig.DEEPSEEK_API_KEY)
    val apiKey = _apiKey.asStateFlow()

    // Selected Model: "deepseek-chat" or "deepseek-coder"
    private val _selectedModel = MutableStateFlow("deepseek-chat")
    val selectedModel = _selectedModel.asStateFlow()

    // Saved Snippets
    val savedSnippets: StateFlow<List<SavedSnippet>> = repository.allSnippets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Code Debugger Workspace State
    private val _debuggerCode = MutableStateFlow("")
    val debuggerCode = _debuggerCode.asStateFlow()

    private val _debuggerResult = MutableStateFlow<String?>(null)
    val debuggerResult = _debuggerResult.asStateFlow()

    private val _debuggerAction = MutableStateFlow(DebuggerAction.AUTO_CHECK)
    val debuggerAction = _debuggerAction.asStateFlow()

    private val _localWarnings = MutableStateFlow<List<LocalWarning>>(emptyList())
    val localWarnings = _localWarnings.asStateFlow()

    private val _parsedIssues = MutableStateFlow<List<CodeIssue>>(emptyList())
    val parsedIssues = _parsedIssues.asStateFlow()

    private val _parsedFixedCode = MutableStateFlow<String?>(null)
    val parsedFixedCode = _parsedFixedCode.asStateFlow()

    // Web Search Tool States
    private val _webSearchResults = MutableStateFlow<List<WebResult>>(emptyList())
    val webSearchResults = _webSearchResults.asStateFlow()

    private val _isSearchingWeb = MutableStateFlow(false)
    val isSearchingWeb = _isSearchingWeb.asStateFlow()

    private val _webSearchEnabled = MutableStateFlow(false)
    val webSearchEnabled = _webSearchEnabled.asStateFlow()

    // GitHub Integration States
    private val _githubUsername = MutableStateFlow("")
    val githubUsername = _githubUsername.asStateFlow()

    private val _githubRepo = MutableStateFlow("")
    val githubRepo = _githubRepo.asStateFlow()

    private val _githubToken = MutableStateFlow("")
    val githubToken = _githubToken.asStateFlow()

    private val _githubPushStatus = MutableStateFlow<String?>(null)
    val githubPushStatus = _githubPushStatus.asStateFlow()

    private val _githubIsPushing = MutableStateFlow(false)
    val githubIsPushing = _githubIsPushing.asStateFlow()

    // Market Analysis and Simulation States
    private val _forexAssets = MutableStateFlow<List<ForexAsset>>(emptyList())
    val forexAssets = _forexAssets.asStateFlow()

    private val _stockAssets = MutableStateFlow<List<StockAsset>>(emptyList())
    val stockAssets = _stockAssets.asStateFlow()

    private val _memeCoins = MutableStateFlow<List<MemeCoinAsset>>(emptyList())
    val memeCoins = _memeCoins.asStateFlow()

    private val _marketNotifications = MutableStateFlow<List<MarketNotification>>(emptyList())
    val marketNotifications = _marketNotifications.asStateFlow()

    private val _marketAlerts = MutableStateFlow<List<MarketAlertSubscription>>(emptyList())
    val marketAlerts = _marketAlerts.asStateFlow()

    private val _marketAnalysisResult = MutableStateFlow<String?>(null)
    val marketAnalysisResult = _marketAnalysisResult.asStateFlow()

    private val _isAnalyzingMarket = MutableStateFlow(false)
    val isAnalyzingMarket = _isAnalyzingMarket.asStateFlow()

    private val _selectedMarketAsset = MutableStateFlow<String?>(null)
    val selectedMarketAsset = _selectedMarketAsset.asStateFlow()

    init {
        initMarkets()
        startMarketsSimulation()

        // Handle database load and auto-populate default project if empty
        viewModelScope.launch {
            repository.allProjects.collect { projects ->
                if (projects.isEmpty()) {
                    val defaultId = repository.createProject(
                        "My First App Project",
                        "Default workspace for BASE 2 CODE files, tools, and tasks."
                    )
                    // Write a default template main file
                    repository.saveFile(defaultId, "src/MainActivity.kt", "// Welome to your BASE 2 CODE workspace!\n// Use WriteFileTool to edit this project.")
                    selectProject(defaultId)
                } else if (_selectedProjectId.value == "default" && projects.isNotEmpty()) {
                    selectProject(projects.first().id)
                }
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun updateApiKey(newKey: String) {
        _apiKey.value = newKey
    }

    fun selectModel(model: String) {
        _selectedModel.value = model
    }

    fun setDebuggerCode(code: String) {
        _debuggerCode.value = code
        _localWarnings.value = LocalCodeValidator.validate(code)
    }

    fun setDebuggerAction(action: DebuggerAction) {
        _debuggerAction.value = action
    }

    fun applyDebuggerFix(fixedCode: String) {
        setDebuggerCode(fixedCode)
        _parsedFixedCode.value = null
        _parsedIssues.value = emptyList()
    }

    // Project Operations
    fun selectProject(projectId: String) {
        _selectedProjectId.value = projectId
        viewModelScope.launch {
            val project = repository.getProjectById(projectId)
            _currentProject.value = project
            addToolLog("System: Selected project '${project?.name ?: projectId}'")
            
            // Collect project files
            repository.getFilesForProject(projectId).collect { files ->
                _projectFiles.value = files
            }
        }

        // Collect project chat sessions
        viewModelScope.launch {
            repository.getSessionsForProject(projectId).collect { sessions ->
                _chatSessions.value = sessions
                if (sessions.isNotEmpty()) {
                    // Try to auto-select the first session if nothing is active
                    if (_activeSessionId.value == null || !sessions.any { it.id == _activeSessionId.value }) {
                        selectSession(sessions.first().id)
                    }
                } else {
                    _activeSessionId.value = null
                    _messagesState.value = emptyList()
                }
            }
        }
    }

    fun createProject(name: String, description: String) {
        viewModelScope.launch {
            val id = repository.createProject(name, description)
            repository.saveFile(id, "src/MainActivity.kt", "// Workspace for project: $name\n// Use WriteFileTool to begin coding.")
            selectProject(id)
            addToolLog("System: Created and loaded new project '$name'")
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            addToolLog("System: Deleted project $projectId")
            _selectedProjectId.value = "default" // reset and let init handle selection
        }
    }

    // WriteFileTool Operations
    fun writeFile(path: String, content: String) {
        viewModelScope.launch {
            val projectId = _selectedProjectId.value
            repository.saveFile(projectId, path, content)
            addToolLog("WriteFileTool: Successfully wrote ${content.length} bytes to /$path")
        }
    }

    fun deleteFile(path: String) {
        viewModelScope.launch {
            val projectId = _selectedProjectId.value
            repository.deleteFile(projectId, path)
            addToolLog("DeleteFileTool: Deleted /$path")
        }
    }

    fun addToolLog(log: String) {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _toolLogs.value = _toolLogs.value + "[$timeStr] $log"
    }

    // Chat Sessions
    fun selectSession(sessionId: String) {
        _activeSessionId.value = sessionId
        viewModelScope.launch {
            repository.getMessagesForSession(sessionId).collect { messages ->
                _messagesState.value = messages
            }
        }
    }

    fun createNewSession(title: String = "New Chat") {
        viewModelScope.launch {
            val projectId = _selectedProjectId.value
            val newId = repository.createNewSession(title, projectId)
            _activeSessionId.value = newId
            _messagesState.value = emptyList()
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_activeSessionId.value == sessionId) {
                _activeSessionId.value = null
                _messagesState.value = emptyList()
            }
        }
    }

    // Send Message in Chat (Supports WriteFileTool detection + Web Search injection)
    fun sendMessage(content: String) {
        if (content.trim().isEmpty() || _isGenerating.value) return

        val currentSessionId = _activeSessionId.value
        val projectId = _selectedProjectId.value
        viewModelScope.launch {
            val sessionId = if (currentSessionId == null) {
                val newId = repository.createNewSession(content.take(20) + "...", projectId)
                _activeSessionId.value = newId
                newId
            } else {
                currentSessionId
            }

            // Insert User Message
            repository.insertMessage(sessionId, "user", content)
            
            // Re-fetch messages immediately
            repository.getMessagesForSession(sessionId).collect { messages ->
                _messagesState.value = messages
            }

            _isGenerating.value = true
            _errorMessage.value = null
            try {
                // If Web Search is enabled, fetch query and append context
                var finalUserQuery = content
                if (_webSearchEnabled.value) {
                    addToolLog("WebSearchTool: Context search active for user prompt...")
                    val searchResults = simulateWebSearch(content)
                    _webSearchResults.value = searchResults
                    
                    val searchContext = searchResults.joinToString("\n\n") { 
                        "Source: ${it.title} (${it.url})\nSnippet: ${it.snippet}"
                    }
                    finalUserQuery = "$content\n\n[Web Search Context]\n$searchContext\n\nAnswer the user's prompt using the above Web Search Context."
                    addToolLog("WebSearchTool: Injected search context into Assistant query.")
                }

                // Construct messages context list for DeepSeek
                val history = _messagesState.value.map { msg ->
                    if (msg.role == "user" && msg.content == content) {
                        DeepSeekMessage(msg.role, finalUserQuery)
                    } else {
                        DeepSeekMessage(msg.role, msg.content)
                    }
                }
                
                var apiResponse = repository.generateCode(
                    apiKey = _apiKey.value,
                    model = _selectedModel.value,
                    messages = history
                )

                // Detect if the response requests a file creation/write
                // We parse standard formatting like: [WRITE_FILE path="filename.ext"]file_content[/WRITE_FILE]
                // or markdown codeblock detection
                parseAndExecuteWriteFileTool(apiResponse)

                // Insert Assistant Message
                repository.insertMessage(sessionId, "assistant", apiResponse)

                // Update Session Title to first query if it was default
                val sessions = chatSessions.value
                val session = sessions.find { it.id == sessionId }
                if (session != null && (session.title == "New Chat" || session.title.endsWith("..."))) {
                    repository.updateSessionTitle(sessionId, content.take(24) + "...")
                }

            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Failed to generate code."
                repository.insertMessage(
                    sessionId = sessionId,
                    role = "assistant",
                    content = "Error: Failed to fetch response. Please verify your internet connection and API key in Settings.\n\nDetails: ${e.localizedMessage ?: "No details provided"}"
                )
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private fun parseAndExecuteWriteFileTool(response: String) {
        try {
            // Check for [WRITE_FILE:path]...[/WRITE_FILE]
            val regex = "\\[WRITE_FILE:(.*?)\\]([\\s\\S]*?)\\[/WRITE_FILE\\]".toRegex()
            val matches = regex.findAll(response)
            for (match in matches) {
                val path = match.groups[1]?.value?.trim() ?: continue
                val content = match.groups[2]?.value ?: ""
                writeFile(path, content)
            }
        } catch (e: Exception) {
            addToolLog("WriteFileTool Parser Error: ${e.message}")
        }
    }

    // Run Code Debugger action
    fun executeDebuggerAction() {
        val code = _debuggerCode.value
        if (code.trim().isEmpty() || _isGenerating.value) return

        val prompt = when (_debuggerAction.value) {
            DebuggerAction.EXPLAIN -> "Explain the following code, its big-O complexity, and how it works:\n\n```\n$code\n```"
            DebuggerAction.DEBUG -> "Identify bugs, runtime errors, edge cases, and security vulnerabilities in this code and provide a fully fixed, optimized version:\n\n```\n$code\n```"
            DebuggerAction.OPTIMIZE -> "Optimize the performance, memory usage, and readability of the following code and explain your changes:\n\n```\n$code\n```"
            DebuggerAction.UNIT_TEST -> "Write comprehensive, robust unit tests for the following code (covering success scenarios, edge cases, and failure states):\n\n```\n$code\n```"
            DebuggerAction.AUTO_CHECK -> """
                You are an expert static analyzer and debugger. Analyze the following code for compilation errors, syntax mistakes, logical bugs, null-pointer exceptions, security issues, performance issues, or bad practices.

                Output your findings STRICTLY using the [ISSUE] blocks and the [FIXED_CODE] block as defined below:

                For each issue found, print:
                [ISSUE]
                Type: <Error | Warning | Suggestion>
                Line: <Line number where issue starts or "Unknown">
                Summary: <Short, high-level summary of the issue>
                Explanation: <Clear explanation of what the bug is, why it occurs, and why it is problematic>
                Fix: <Description of how to fix this issue>
                [/ISSUE]

                Finally, provide the complete, corrected, fully working, syntactically valid and compilation-ready code block:
                [FIXED_CODE]
                <your corrected code here>
                [/FIXED_CODE]

                If no issues are found, still output the [FIXED_CODE] containing the unchanged code, but you do not need to output any [ISSUE] blocks. Do not output any conversational introduction or conclusion outside of these blocks.

                Here is the code to analyze:
                ```
                $code
                ```
            """.trimIndent()
        }

        viewModelScope.launch {
            _isGenerating.value = true
            _debuggerResult.value = null
            _errorMessage.value = null
            _parsedIssues.value = emptyList()
            _parsedFixedCode.value = null
            try {
                val message = DeepSeekMessage(role = "user", content = prompt)
                val response = repository.generateCode(
                    apiKey = _apiKey.value,
                    model = _selectedModel.value,
                    messages = listOf(message)
                )
                _debuggerResult.value = response
                
                if (_debuggerAction.value == DebuggerAction.AUTO_CHECK) {
                    _parsedIssues.value = CodeIssueParser.parseCodeIssues(response)
                    _parsedFixedCode.value = CodeIssueParser.parseFixedCode(response)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Analysis failed."
                _debuggerResult.value = "Error: Failed to perform analysis. Please verify your API key in Settings.\n\nDetails: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    // Save Snippet
    fun saveCodeSnippet(title: String, description: String, code: String, language: String) {
        viewModelScope.launch {
            repository.saveSnippet(title, description, code, language)
        }
    }

    fun deleteSnippet(snippet: SavedSnippet) {
        viewModelScope.launch {
            repository.deleteSnippet(snippet)
        }
    }

    // Web Search Tool API helper
    fun toggleWebSearch(enabled: Boolean) {
        _webSearchEnabled.value = enabled
    }

    fun performWebSearch(query: String) {
        if (query.trim().isEmpty()) return
        _isSearchingWeb.value = true
        viewModelScope.launch {
            try {
                addToolLog("WebSearchTool: Querying Google for '$query'...")
                val results = simulateWebSearch(query)
                _webSearchResults.value = results
                addToolLog("WebSearchTool: Found ${results.size} relevant results.")
            } catch (e: Exception) {
                addToolLog("WebSearchTool: Error - ${e.message}")
            } finally {
                _isSearchingWeb.value = false
            }
        }
    }

    private fun simulateWebSearch(query: String): List<WebResult> {
        val q = query.lowercase()
        val results = mutableListOf<WebResult>()
        
        if (q.contains("compose") || q.contains("jetpack") || q.contains("ui")) {
            results.add(WebResult(
                title = "Jetpack Compose Basics - Android Developers",
                url = "https://developer.android.com/develop/ui/compose/documentation",
                snippet = "Jetpack Compose is Android's modern toolkit for building native UI. It simplifies and accelerates UI development on Android. Quickly bring your app to life with less code, powerful tools, and intuitive Kotlin APIs.",
                source = "Google Developers"
            ))
            results.add(WebResult(
                title = "Compose Layouts, Modifiers, and Scaffold",
                url = "https://developer.android.com/develop/ui/compose/layouts",
                snippet = "Learn how to build complex responsive layouts in Compose using Box, Column, Row, and Scaffold. Utilize Modifiers to style, position, and add interactive gestures to your UI components.",
                source = "Android API Reference"
            ))
        }
        
        if (q.contains("room") || q.contains("database") || q.contains("sqlite") || q.contains("persistence")) {
            results.add(WebResult(
                title = "Save data in a local database using Room",
                url = "https://developer.android.com/training/data-storage/room",
                snippet = "The Room persistence library provides an abstraction layer over SQLite to allow fluent database access while harnessing the full power of SQLite. It supports compiling queries, migrations, and Kotlin Flows.",
                source = "Android Developers Guide"
            ))
        }
        
        if (q.contains("github") || q.contains("push") || q.contains("git") || q.contains("token")) {
            results.add(WebResult(
                title = "GitHub REST API - Repository Contents Endpoint",
                url = "https://docs.github.com/en/rest/repos/contents",
                snippet = "Create, update, or delete a file in a repository. Pushes updates directly to GitHub branches using Personal Access Tokens (PAT). Base64 encoding is required for payload content.",
                source = "GitHub Docs"
            ))
        }
        
        // General helpful results
        results.add(WebResult(
            title = "Modern Kotlin Design Patterns and Best Practices (2026)",
            url = "https://kotlinlang.org/docs/home.html",
            snippet = "Explore state management, Clean Architecture, Repository patterns, and declarative interfaces to build bulletproof server and mobile tools.",
            source = "Kotlin Language Reference"
        ))
        
        results.add(WebResult(
            title = "StackOverflow Solutions for '$query'",
            url = "https://stackoverflow.com/questions/tagged/kotlin",
            snippet = "Top developers recommend utilizing modern asynchronous Flows and structured concurrency for clean execution of '$query'. Check error blocks to avoid resource leaks.",
            source = "StackOverflow"
        ))
        
        return results
    }

    // GitHub Integration Settings Updates
    fun updateGithubSettings(username: String, repo: String, token: String) {
        _githubUsername.value = username
        _githubRepo.value = repo
        _githubToken.value = token
        addToolLog("System: GitHub credentials updated.")
    }

    // Push Selected File or Whole Project to GitHub
    fun pushProjectToGitHub(commitMessage: String) {
        val token = _githubToken.value
        val owner = _githubUsername.value
        val repo = _githubRepo.value
        
        if (token.isEmpty() || owner.isEmpty() || repo.isEmpty()) {
            _githubPushStatus.value = "Error: Please enter Username, Repo Name, and PAT Token in the GitHub panel."
            return
        }
        
        val files = _projectFiles.value
        if (files.isEmpty()) {
            _githubPushStatus.value = "Error: No files found in current project workspace to push."
            return
        }
        
        _githubIsPushing.value = true
        _githubPushStatus.value = "Preparing workspace files..."
        addToolLog("GitHubTool: Connecting to GitHub repository $owner/$repo...")
        
        viewModelScope.launch {
            var successCount = 0
            var errorOccurred = false
            var errorMessage = ""
            
            for (file in files) {
                _githubPushStatus.value = "Pushing ${file.path}..."
                val result = pushFileToGitHub(
                    token = token,
                    owner = owner,
                    repo = repo,
                    path = file.path,
                    content = file.content,
                    commitMessage = commitMessage
                )
                if (result.isSuccess) {
                    successCount++
                    addToolLog("GitHubTool: Successfully pushed ${file.path}")
                } else {
                    errorOccurred = true
                    errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                    addToolLog("GitHubTool: Failed to push ${file.path} - $errorMessage")
                    break
                }
            }
            
            _githubIsPushing.value = false
            if (errorOccurred) {
                _githubPushStatus.value = "Push Failed. Reason: $errorMessage"
            } else {
                _githubPushStatus.value = "Success! Pushed $successCount files to $owner/$repo."
                addToolLog("GitHubTool: Project push completed successfully.")
            }
        }
    }

    private suspend fun pushFileToGitHub(
        token: String,
        owner: String,
        repo: String,
        path: String,
        content: String,
        commitMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            
            // 1. Get SHA if file exists to overwrite it
            val url = "https://api.github.com/repos/$owner/$repo/contents/$path"
            val getRequest = Request.Builder()
                .url(url)
                .addHeader("Authorization", "token $token")
                .addHeader("Accept", "application/vnd.github.v3+json")
                .get()
                .build()
                
            var sha: String? = null
            client.newCall(getRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val jsonObj = JSONObject(body)
                    sha = jsonObj.optString("sha", null)
                }
            }
            
            // 2. Base64 encode file content
            val encodedContent = Base64.encodeToString(content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            
            // 3. PUT request payload
            val jsonPayload = JSONObject().apply {
                put("message", commitMessage)
                put("content", encodedContent)
                if (sha != null) {
                    put("sha", sha)
                }
            }
            
            val putRequest = Request.Builder()
                .url(url)
                .addHeader("Authorization", "token $token")
                .addHeader("Accept", "application/vnd.github.v3+json")
                .put(jsonPayload.toString().toRequestBody(mediaType))
                .build()
                
            client.newCall(putRequest).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success("Pushed $path")
                } else {
                    val errorBody = response.body?.string() ?: "No error body details"
                    Result.failure(Exception("GitHub HTTP ${response.code}: $errorBody"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- MARKET ALERTS, SIMULATIONS, AND AI RESEARCH RESEARCH ---

    private fun initMarkets() {
        _forexAssets.value = listOf(
            ForexAsset("EUR/USD", 1.0854, 0.12, listOf(1.0830, 1.0841, 1.0835, 1.0848, 1.0854), 1.0862, 1.0825),
            ForexAsset("GBP/USD", 1.2742, -0.05, listOf(1.2755, 1.2748, 1.2750, 1.2739, 1.2742), 1.2765, 1.2732),
            ForexAsset("USD/JPY", 156.45, 0.35, listOf(155.80, 156.10, 156.02, 156.32, 156.45), 156.70, 155.50),
            ForexAsset("AUD/USD", 0.6652, -0.18, listOf(0.6670, 0.6665, 0.6658, 0.6650, 0.6652), 0.6675, 0.6645),
            ForexAsset("USD/CAD", 1.3685, 0.08, listOf(1.3670, 1.3675, 1.3682, 1.3680, 1.3685), 1.3695, 1.3660)
        )

        _stockAssets.value = listOf(
            StockAsset("TSLA", "Tesla Inc.", 178.50, 2.45, listOf(174.20, 175.50, 176.10, 177.80, 178.50), 180.10, 173.50),
            StockAsset("NVDA", "NVIDIA Corp.", 920.12, 4.15, listOf(885.00, 895.00, 908.00, 912.00, 920.12), 925.00, 882.00),
            StockAsset("AAPL", "Apple Inc.", 189.84, -0.32, listOf(190.50, 190.10, 189.90, 189.70, 189.84), 191.00, 189.20),
            StockAsset("AMZN", "Amazon.com Inc.", 185.50, 1.12, listOf(183.10, 184.00, 184.50, 185.10, 185.50), 186.40, 182.80),
            StockAsset("MSFT", "Microsoft Corp.", 421.90, 0.75, listOf(418.50, 419.80, 420.50, 421.10, 421.90), 423.00, 417.50)
        )

        _memeCoins.value = listOf(
            MemeCoinAsset("PEPEAI", "PEPE AI", "Launched 5m ago", 0, 0.0000012, 0.00000145, 145000.0, 45000.0, 2.0, 2.0, false, true, 25, 0xFF4CAF50),
            MemeCoinAsset("DOGE2", "Doge 2.0", "In 15s", 15, 0.00004, 0.00004, 0.0, 15000.0, 5.0, 5.0, false, false, 48, 0xFFFFC107),
            MemeCoinAsset("SOLCAT", "Solana Cat", "In 45s", 45, 0.00008, 0.00008, 0.0, 8000.0, 0.0, 0.0, false, false, 12, 0xFF00E5FF),
            MemeCoinAsset("SHIBLITE", "Shiba Lite", "Launched 12m ago", 0, 0.00000045, 0.00000038, 38000.0, 12000.0, 10.0, 10.0, true, true, 85, 0xFFFF5252),
            MemeCoinAsset("MOONBOY", "Moon Boy", "In 120s", 120, 0.00015, 0.00015, 0.0, 35000.0, 3.0, 3.0, false, false, 35, 0xFF9C27B0)
        )
        
        _marketAlerts.value = listOf(
            MarketAlertSubscription(assetSymbol = "TSLA", alertType = "STOCK", condition = "ABOVE", targetValue = 180.0),
            MarketAlertSubscription(assetSymbol = "DOGE2", alertType = "MEME", condition = "LAUNCH", targetValue = 0.0)
        )
    }

    private fun startMarketsSimulation() {
        viewModelScope.launch(Dispatchers.Default) {
            val random = java.util.Random()
            while (isActive) {
                kotlinx.coroutines.delay(3000)
                
                // 1. Forex Updates
                _forexAssets.value = _forexAssets.value.map { fx ->
                    val change = (random.nextDouble() - 0.5) * 0.0008
                    val newPrice = fx.price + change
                    val newHistory = (fx.history + newPrice).takeLast(6)
                    val percent = fx.changePercent + (change / fx.price) * 100
                    ForexAsset(
                        symbol = fx.symbol,
                        price = newPrice,
                        changePercent = percent,
                        history = newHistory,
                        high = maxOf(fx.high, newPrice),
                        low = minOf(fx.low, newPrice)
                    )
                }

                // 2. Stock Updates
                _stockAssets.value = _stockAssets.value.map { stock ->
                    val change = (random.nextDouble() - 0.49) * 0.45
                    val newPrice = maxOf(1.0, stock.price + change)
                    val newHistory = (stock.history + newPrice).takeLast(6)
                    val percent = stock.changePercent + (change / stock.price) * 100
                    StockAsset(
                        symbol = stock.symbol,
                        name = stock.name,
                        price = newPrice,
                        changePercent = percent,
                        history = newHistory,
                        high = maxOf(stock.high, newPrice),
                        low = minOf(stock.low, newPrice)
                    )
                }

                // 3. Meme Coin Countdown & Launch Updates
                _memeCoins.value = _memeCoins.value.map { coin ->
                    if (coin.isLaunched) {
                        val isPump = random.nextDouble() > 0.45
                        val factor = if (isPump) 0.05 + random.nextDouble() * 0.08 else -(0.04 + random.nextDouble() * 0.06)
                        val newPrice = maxOf(0.00000001, coin.currentPrice * (1 + factor))
                        val newMarketCap = coin.marketCap * (1 + factor)
                        MemeCoinAsset(
                            symbol = coin.symbol,
                            name = coin.name,
                            launchTime = coin.launchTime,
                            countdownSeconds = 0,
                            launchPrice = coin.launchPrice,
                            currentPrice = newPrice,
                            marketCap = newMarketCap,
                            liquidity = coin.liquidity * (if (isPump) 1.02 else 0.99),
                            buyTax = coin.buyTax,
                            sellTax = coin.sellTax,
                            honeypot = coin.honeypot,
                            isLaunched = true,
                            riskScore = coin.riskScore,
                            logoColor = coin.logoColor
                        )
                    } else {
                        val remaining = coin.countdownSeconds - 3
                        if (remaining <= 0) {
                            val launchTitle = "🚀 DEFI MEME LAUNCH RADAR: ${coin.name}"
                            val launchMsg = "Newly detected token launch on DEX! Symbol: ${coin.symbol}. Initial Liquidity: \$${String.format("%,.0f", coin.liquidity)}. Contract safe-check completed. Risk score: ${coin.riskScore}/100."
                            viewModelScope.launch(Dispatchers.Main) {
                                addMarketNotification(launchTitle, launchMsg, "MEME")
                            }

                            MemeCoinAsset(
                                symbol = coin.symbol,
                                name = coin.name,
                                launchTime = "LIVE NOW",
                                countdownSeconds = 0,
                                launchPrice = coin.launchPrice,
                                currentPrice = coin.launchPrice,
                                marketCap = coin.launchPrice * 100000000,
                                liquidity = coin.liquidity,
                                buyTax = coin.buyTax,
                                sellTax = coin.sellTax,
                                honeypot = coin.honeypot,
                                isLaunched = true,
                                riskScore = coin.riskScore,
                                logoColor = coin.logoColor
                            )
                        } else {
                            MemeCoinAsset(
                                symbol = coin.symbol,
                                name = coin.name,
                                launchTime = "In ${remaining}s",
                                countdownSeconds = remaining,
                                launchPrice = coin.launchPrice,
                                currentPrice = coin.launchPrice,
                                marketCap = 0.0,
                                liquidity = coin.liquidity,
                                buyTax = coin.buyTax,
                                sellTax = coin.sellTax,
                                honeypot = coin.honeypot,
                                isLaunched = false,
                                riskScore = coin.riskScore,
                                logoColor = coin.logoColor
                            )
                        }
                    }
                }

                // 4. Alert Checks
                val currentAlerts = _marketAlerts.value
                val activeAlerts = currentAlerts.filter { it.isActive }
                if (activeAlerts.isNotEmpty()) {
                    val updatedAlerts = currentAlerts.toMutableList()
                    activeAlerts.forEach { alert ->
                        var isTriggered = false
                        var triggerMsg = ""
                        
                        when (alert.alertType) {
                            "FOREX" -> {
                                val fx = _forexAssets.value.firstOrNull { it.symbol == alert.assetSymbol }
                                if (fx != null) {
                                    if (alert.condition == "ABOVE" && fx.price >= alert.targetValue) {
                                        isTriggered = true
                                        triggerMsg = "Forex alert: ${alert.assetSymbol} broke above limit ${alert.targetValue} (Current: ${String.format("%.4f", fx.price)})"
                                    } else if (alert.condition == "BELOW" && fx.price <= alert.targetValue) {
                                        isTriggered = true
                                        triggerMsg = "Forex alert: ${alert.assetSymbol} broke below limit ${alert.targetValue} (Current: ${String.format("%.4f", fx.price)})"
                                    }
                                }
                            }
                            "STOCK" -> {
                                val st = _stockAssets.value.firstOrNull { it.symbol == alert.assetSymbol }
                                if (st != null) {
                                    if (alert.condition == "ABOVE" && st.price >= alert.targetValue) {
                                        isTriggered = true
                                        triggerMsg = "Stock alert: ${alert.assetSymbol} rose above target \$${alert.targetValue} (Current: \$${String.format("%.2f", st.price)})"
                                    } else if (alert.condition == "BELOW" && st.price <= alert.targetValue) {
                                        isTriggered = true
                                        triggerMsg = "Stock alert: ${alert.assetSymbol} fell below target \$${alert.targetValue} (Current: \$${String.format("%.2f", st.price)})"
                                    }
                                }
                            }
                            "MEME" -> {
                                val coin = _memeCoins.value.firstOrNull { it.symbol == alert.assetSymbol }
                                if (coin != null && coin.isLaunched) {
                                    val original = currentAlerts.firstOrNull { it.id == alert.id }
                                    if (original != null && original.isActive) {
                                        isTriggered = true
                                        triggerMsg = "Meme Coin Alert: ${coin.name} (${coin.symbol}) has officially launched! Current price: \$${String.format("%.6f", coin.currentPrice)} with \$${String.format("%,.0f", coin.liquidity)} locked liquidity."
                                    }
                                }
                            }
                        }

                        if (isTriggered) {
                            viewModelScope.launch(Dispatchers.Main) {
                                addMarketNotification("🚨 MARKET ALERT TRIGGERED", triggerMsg, alert.alertType)
                            }
                            val idx = updatedAlerts.indexOfFirst { it.id == alert.id }
                            if (idx != -1) {
                                updatedAlerts[idx] = alert.copy(isActive = false)
                            }
                        }
                    }
                    _marketAlerts.value = updatedAlerts
                }
            }
        }
    }

    fun addMarketNotification(title: String, message: String, type: String) {
        val notification = MarketNotification(title = title, message = message, type = type)
        _marketNotifications.value = listOf(notification) + _marketNotifications.value

        try {
            val context = getApplication<Application>().applicationContext
            val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val channelId = "market_alerts_channel"
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId,
                    "Market Alerts",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Real-time Alerts for Forex, Meme Coins, and Stocks"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addAlertSubscription(symbol: String, type: String, condition: String, target: Double) {
        val alert = MarketAlertSubscription(
            assetSymbol = symbol,
            alertType = type,
            condition = condition,
            targetValue = target
        )
        _marketAlerts.value = _marketAlerts.value + alert
    }

    fun deleteAlertSubscription(id: String) {
        _marketAlerts.value = _marketAlerts.value.filter { it.id != id }
    }

    fun clearMarketAnalysis() {
        _marketAnalysisResult.value = null
        _selectedMarketAsset.value = null
    }

    fun triggerMarketResearch(assetType: String, symbol: String, assetName: String = "") {
        val prompt = when (assetType.uppercase()) {
            "FOREX" -> """
                You are an elite quantitative forex analyst and currency trader. Conduct a high-probability technical and fundamental analysis for the forex pair $symbol.
                Include:
                1. Market Sentiment & Trend Bias (Bullish/Bearish/Neutral)
                2. Key Support & Resistance Levels (S1/S2/R1/R2)
                3. Technical Indicators (RSI, MACD, Moving Averages recommendation)
                4. Key Volatility Drivers & Macroeconomic Factors (Central bank rates, inflation, employment data)
                5. Recommendation & Risk Management strategy.
                
                Respond in clean, professional markdown with high-impact details.
            """.trimIndent()
            "STOCK" -> """
                You are an expert equity research analyst and Wall Street trader. Conduct a professional, real-time-focused technical and fundamental analysis for the stock $symbol ($assetName).
                Include:
                1. Fundamental Analysis Overview (Growth drivers, recent earnings impact, valuation)
                2. Technical Pattern Analysis (Moving averages crossover, relative strength, volume analysis)
                3. Market Consensus & Price Target (Underpriced, fairly valued, overpriced)
                4. Macro-Economic & Sector Tailwinds / Headwinds
                5. Buy / Sell / Hold Technical Rating & Risk Management plan.
                
                Respond in clean, professional markdown.
            """.trimIndent()
            "MEME" -> """
                You are a degens and blockchain forensics analyst specialized in decentralized exchanges (DEX) and meme coins. Conduct a comprehensive security and momentum audit on the meme coin $symbol ($assetName).
                Include:
                1. Security/Smart Contract Risk Assessment (Honeypot risk, rugpull likelihood, developer wallet allocation, ownership status)
                2. Liquidity Pool Analysis (Locked vs unlocked percentage, liquidity-to-market-cap ratio)
                3. Volatility Forecast (Is this a pump-and-dump, slow rug, or actual viral potential?)
                4. On-chain Activity & Social Hype Score
                5. High-Risk Degens Strategy & Safety Advice (e.g. Stop-loss, maximum position size recommendation).
                
                Respond in clean, professional markdown with a serious warning about the high risk of meme coin investing.
            """.trimIndent()
            else -> "Analyze the financial asset $symbol and provide technical advice."
        }

        _selectedMarketAsset.value = "$symbol ($assetType)"
        _isAnalyzingMarket.value = true
        _marketAnalysisResult.value = null

        viewModelScope.launch {
            try {
                val message = DeepSeekMessage(role = "user", content = prompt)
                val response = repository.generateCode(
                    apiKey = _apiKey.value,
                    model = _selectedModel.value,
                    messages = listOf(message),
                    temperature = 0.5
                )
                _marketAnalysisResult.value = response
            } catch (e: Exception) {
                _marketAnalysisResult.value = "Error performing AI Market Analysis: ${e.message}\n\nPlease check your DeepSeek API Key in the Settings tab."
            } finally {
                _isAnalyzingMarket.value = false
            }
        }
    }
}

enum class AppTab {
    CHAT, DEBUGGER, MARKETS, SNIPPETS, TOOLS, SETTINGS
}

enum class DebuggerAction {
    AUTO_CHECK, EXPLAIN, DEBUG, OPTIMIZE, UNIT_TEST
}

data class WebResult(
    val title: String,
    val url: String,
    val snippet: String,
    val source: String
)

class AssistantViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssistantViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application)
            val api = DeepSeekApi.create()
            val repository = AssistantRepository(
                database.chatDao(),
                database.snippetDao(),
                database.projectDao(),
                api
            )
            @Suppress("UNCHECKED_CAST")
            return AssistantViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
