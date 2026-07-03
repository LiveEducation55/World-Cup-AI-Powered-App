package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(val text: String)

@JsonClass(generateAdapter = true)
data class GeminiContent(val parts: List<GeminiPart>)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(val content: GeminiContent)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun getAiResponse(prompt: String, chatHistory: List<GeminiContent> = emptyList(), matchContextPrompt: String? = null): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "AI Insights is in Demo Mode. Please configure your GEMINI_API_KEY in the Secrets panel of Google AI Studio."
        }

        val systemPrompt = """
            You are the ultimate football tactical analyst, World Cup commentator, and statistical predictor for the FIFA World Cup 2026.
            Your tone is energetic, knowledgeable, witty, and engaging—think of a mix between Peter Drury's poetic commentary and Pep Guardiola's tactical depth.
            Use tactical insights, historic stats, team shapes, key players (like Vinicius Jr for Brazil, Mbappe for France, Messi's legacy/coaching influence, Bellingham for England, Musiala for Germany, Lamine Yamal for Spain) to construct predictions and analyses.
            If the user asks about a specific match, refer to the provided live or historical context. Since the current tournament date is around late June / early July 2026, we are currently playing the Round of 16 matches.
            Provide predictions that sound professional and fully calculated, using percentages or specific scoreline projections. Format your response with markdown, bullets, and emojis where appropriate to make it visually engaging.
        """.trimIndent()

        val fullSystemInstruction = if (matchContextPrompt != null) {
            "$systemPrompt\n\nSpecific Match Context for this conversation:\n$matchContextPrompt"
        } else {
            systemPrompt
        }

        // Build the conversation content
        val contentsList = mutableListOf<GeminiContent>()
        contentsList.addAll(chatHistory)
        contentsList.add(GeminiContent(listOf(GeminiPart(prompt))))

        val request = GeminiRequest(
            contents = contentsList,
            systemInstruction = GeminiContent(listOf(GeminiPart(fullSystemInstruction)))
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I tried to analyze this, but couldn't get a clear signal from the VAR room! Let's try again."
        } catch (e: Exception) {
            e.printStackTrace()
            "Connection error to the stadium commentary box: ${e.localizedMessage ?: "Please try again."}"
        }
    }
}
