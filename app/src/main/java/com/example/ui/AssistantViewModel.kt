package com.example.ui

import android.app.Application
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
import com.example.data.model.DeepSeekMessage
import com.example.data.repository.AssistantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AssistantViewModel(
    application: Application,
    private val repository: AssistantRepository
) : AndroidViewModel(application) {

    // Selected App Feature/Tab
    private val _selectedTab = MutableStateFlow(AppTab.CHAT)
    val selectedTab = _selectedTab.asStateFlow()

    // Active Chat Session ID (null if none, or we auto-create)
    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId = _activeSessionId.asStateFlow()

    // Historical Chat Sessions
    val chatSessions: StateFlow<List<ChatSession>> = repository.allSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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

    private val _debuggerAction = MutableStateFlow(DebuggerAction.DEBUG)
    val debuggerAction = _debuggerAction.asStateFlow()

    init {
        // Try to load first session messages on launch
        viewModelScope.launch {
            chatSessions.collect { sessions ->
                if (_activeSessionId.value == null && sessions.isNotEmpty()) {
                    selectSession(sessions.first().id)
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
    }

    fun setDebuggerAction(action: DebuggerAction) {
        _debuggerAction.value = action
    }

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
            val newId = repository.createNewSession(title)
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

    // Send Message in Chat
    fun sendMessage(content: String) {
        if (content.trim().isEmpty() || _isGenerating.value) return

        val currentSessionId = _activeSessionId.value
        viewModelScope.launch {
            val sessionId = if (currentSessionId == null) {
                val newId = repository.createNewSession(content.take(20) + "...")
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

            // Call DeepSeek API
            _isGenerating.value = true
            _errorMessage.value = null
            try {
                // Construct messages context list for DeepSeek
                val history = _messagesState.value.map { DeepSeekMessage(it.role, it.content) }
                
                val apiResponse = repository.generateCode(
                    apiKey = _apiKey.value,
                    model = _selectedModel.value,
                    messages = history
                )

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

    // Run Code Debugger action
    fun executeDebuggerAction() {
        val code = _debuggerCode.value
        if (code.trim().isEmpty() || _isGenerating.value) return

        val prompt = when (_debuggerAction.value) {
            DebuggerAction.EXPLAIN -> "Explain the following code, its big-O complexity, and how it works:\n\n```\n$code\n```"
            DebuggerAction.DEBUG -> "Identify bugs, runtime errors, edge cases, and security vulnerabilities in this code and provide a fully fixed, optimized version:\n\n```\n$code\n```"
            DebuggerAction.OPTIMIZE -> "Optimize the performance, memory usage, and readability of the following code and explain your changes:\n\n```\n$code\n```"
            DebuggerAction.UNIT_TEST -> "Write comprehensive, robust unit tests for the following code (covering success scenarios, edge cases, and failure states):\n\n```\n$code\n```"
        }

        viewModelScope.launch {
            _isGenerating.value = true
            _debuggerResult.value = null
            _errorMessage.value = null
            try {
                val message = DeepSeekMessage(role = "user", content = prompt)
                val response = repository.generateCode(
                    apiKey = _apiKey.value,
                    model = _selectedModel.value,
                    messages = listOf(message)
                )
                _debuggerResult.value = response
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
}

enum class AppTab {
    CHAT, DEBUGGER, SNIPPETS, SETTINGS
}

enum class DebuggerAction {
    EXPLAIN, DEBUG, OPTIMIZE, UNIT_TEST
}

class AssistantViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssistantViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application)
            val api = DeepSeekApi.create()
            val repository = AssistantRepository(database.chatDao(), database.snippetDao(), api)
            @Suppress("UNCHECKED_CAST")
            return AssistantViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
