package com.bananchiki.wakeup.data.ai

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.bananchiki.wakeup.BuildConfig

class TextGenerator {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 10000
        }
    }

    private val apiKey = BuildConfig.OPENROUTER_API_KEY

    suspend fun generateText(prompt: String): String? {
        return try {

            val response: ChatResponse = client.post("https://openrouter.ai/api/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(
                    ChatRequest(
                        model = "deepseek/deepseek-v4-flash:free",
                        messages = listOf(ChatMessage(role = "user", content = prompt))
                    )
                )
            }.body()

            response.choices.firstOrNull()?.message?.content

        } catch (e: Exception) {
            e.printStackTrace()
            "Ошибка: ${e.message}"
        }
    }
}
