package com.example.data.repository

import com.example.data.api.DeepSeekApi
import com.example.data.db.ChatDao
import com.example.data.db.ChatSession
import com.example.data.db.ChatMessageEntity
import com.example.data.db.SnippetDao
import com.example.data.db.SavedSnippet
import com.example.data.db.ProjectDao
import com.example.data.db.Project
import com.example.data.db.ProjectFile
import com.example.data.model.DeepSeekMessage
import com.example.data.model.DeepSeekRequest
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AssistantRepository(
    private val chatDao: ChatDao,
    private val snippetDao: SnippetDao,
    private val projectDao: ProjectDao,
    private val api: DeepSeekApi
) {
    // Projects
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()

    suspend fun getProjectById(id: String): Project? {
        return projectDao.getProjectById(id)
    }

    suspend fun createProject(name: String, description: String): String {
        val id = UUID.randomUUID().toString()
        val project = Project(
            id = id,
            name = name,
            description = description,
            createdAt = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis()
        )
        projectDao.insertProject(project)
        return id
    }

    suspend fun deleteProject(projectId: String) {
        projectDao.deleteFilesByProjectId(projectId)
        // Also delete sessions associated with this project
        // (For simplicity we keep them or can delete them. Let's delete sessions and their messages)
        chatDao.getSessionsForProject(projectId).collect { sessions ->
            sessions.forEach { session ->
                chatDao.deleteMessagesBySessionId(session.id)
                chatDao.deleteSessionById(session.id)
            }
        }
        projectDao.deleteProjectById(projectId)
    }

    suspend fun insertProjectDirect(project: Project) {
        projectDao.insertProject(project)
    }

    // Workspace Files for Projects (WriteFileTool targets these!)
    fun getFilesForProject(projectId: String): Flow<List<ProjectFile>> {
        return projectDao.getFilesForProject(projectId)
    }

    suspend fun getFileByPath(projectId: String, path: String): ProjectFile? {
        return projectDao.getFileByPath(projectId, path)
    }

    suspend fun saveFile(projectId: String, path: String, content: String) {
        val existing = projectDao.getFileByPath(projectId, path)
        val file = if (existing != null) {
            existing.copy(content = content, lastUpdated = System.currentTimeMillis())
        } else {
            ProjectFile(projectId = projectId, path = path, content = content)
        }
        projectDao.insertFile(file)
        
        // Update project's lastModified timestamp
        projectDao.getProjectById(projectId)?.let { project ->
            projectDao.insertProject(project.copy(lastModified = System.currentTimeMillis()))
        }
    }

    suspend fun deleteFile(projectId: String, path: String) {
        projectDao.deleteFileByPath(projectId, path)
    }

    // Chat Sessions
    val allSessions: Flow<List<ChatSession>> = chatDao.getAllSessions()

    fun getSessionsForProject(projectId: String): Flow<List<ChatSession>> {
        return chatDao.getSessionsForProject(projectId)
    }

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun createNewSession(title: String, projectId: String = "default"): String {
        val id = UUID.randomUUID().toString()
        val session = ChatSession(id = id, title = title, projectId = projectId)
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
