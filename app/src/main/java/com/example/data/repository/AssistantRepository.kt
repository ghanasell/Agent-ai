package com.example.data.repository

import com.example.data.api.DeepSeekApi
import com.example.data.db.ChatDao
import com.example.data.db.ChatSession
import com.example.data.db.ChatMessageEntity
import com.example.data.db.SnippetDao
import com.example.data.db.SavedSnippet
import com.example.data.model.DeepSeekMessage
import com.example.data.model.DeepSeekRequest
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AssistantRepository(
    private val chatDao: ChatDao,
    private val snippetDao: SnippetDao,
    private val api: DeepSeekApi
) {
    // Chat Sessions
    val allSessions: Flow<List<ChatSession>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun createNewSession(title: String): String {
        val id = UUID.randomUUID().toString()
        val session = ChatSession(id = id, title = title)
        chatDao.insertSession(session)
        return id
    }

    suspend fun insertMessage(sessionId: String, role: String, content: String) {
        val msg = ChatMessageEntity(
            sessionId = sessionId,
            role = role,
            content = content
        )
        chatDao.insertMessage(msg)
    }

    suspend fun updateSessionTitle(sessionId: String, title: String) {
        chatDao.updateSessionTitle(sessionId, title)
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteMessagesBySessionId(sessionId)
        chatDao.deleteSessionById(sessionId)
    }

    // Code Snippets
    val allSnippets: Flow<List<SavedSnippet>> = snippetDao.getAllSnippets()

    suspend fun saveSnippet(title: String, description: String, code: String, language: String) {
        val snippet = SavedSnippet(
            title = title,
            description = description,
            code = code,
            language = language
        )
        snippetDao.insertSnippet(snippet)
    }

    suspend fun deleteSnippet(snippet: SavedSnippet) {
        snippetDao.deleteSnippet(snippet)
    }

    suspend fun deleteSnippetById(id: Int) {
        snippetDao.deleteSnippetById(id)
    }

    // DeepSeek API Execution
    suspend fun generateCode(
        apiKey: String,
        model: String,
        messages: List<DeepSeekMessage>,
        temperature: Double = 0.2
    ): String {
        val authHeader = "Bearer $apiKey"
        val request = DeepSeekRequest(
            model = model,
            messages = messages,
            temperature = temperature
        )
        val response = api.getChatCompletion(authHeader, request)
        return response.choices.firstOrNull()?.message?.content ?: "No response received."
    }
}
