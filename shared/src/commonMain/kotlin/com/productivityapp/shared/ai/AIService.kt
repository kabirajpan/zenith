package com.productivityapp.shared.ai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GroqMessage(
    val role: String,
    val content: String
)

@Serializable
data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double = 0.7
)

@Serializable
data class GroqChoice(
    val message: GroqMessage
)

@Serializable
data class GroqResponse(
    val choices: List<GroqChoice>
)

object AIService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    private const val API_KEY = AIConfig.GROQ_API_KEY
    private const val API_URL = "https://api.groq.com/openai/v1/chat/completions"
    private const val MODEL = "llama-3.1-8b-instant"

    suspend fun getCompletion(prompt: String, systemPrompt: String): String? {
        val messages = listOf(
            GroqMessage(role = "system", content = systemPrompt),
            GroqMessage(role = "user", content = prompt)
        )
        return getCompletionWithMessages(messages)
    }

    suspend fun getCompletionWithMessages(messages: List<GroqMessage>): String? {
        return try {
            val response: GroqResponse = client.post(API_URL) {
                header(HttpHeaders.Authorization, "Bearer $API_KEY")
                contentType(ContentType.Application.Json)
                setBody(GroqRequest(
                    model = MODEL,
                    messages = messages
                ))
            }.body()

            response.choices.firstOrNull()?.message?.content
        } catch (e: Exception) {
            e.printStackTrace()
            "Error connecting to Zenith Intelligence: ${e.message}"
        }
    }
}
