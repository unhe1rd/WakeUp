package com.bananchiki.wakeup.data.ai
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(val model: String, val messages: List<ChatMessage>)

@Serializable
data class ChatMessage(val role: String, val content: String?)

@Serializable
data class ChatResponse(val choices: List<Choice>)

@Serializable
data class Choice(val message: ChatMessage)
