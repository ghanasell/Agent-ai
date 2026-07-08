package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey val id: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val role: String, // "user", "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_snippets")
data class SavedSnippet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val code: String,
    val language: String,
    val timestamp: Long = System.currentTimeMillis()
)
