package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenAiChatRequest(
    val model: String,
    val messages: List<OpenAiChatMessage>,
    val temperature: Float? = 0.7f,
    @Json(name = "max_tokens") val maxTokens: Int? = 2048
)

@JsonClass(generateAdapter = true)
data class OpenAiChatMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class OpenAiChatResponse(
    val choices: List<OpenAiChoice>? = null,
    val error: OpenAiError? = null
)

@JsonClass(generateAdapter = true)
data class OpenAiChoice(
    val index: Int? = null,
    val message: OpenAiChatMessage? = null,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenAiError(
    val message: String? = null,
    val type: String? = null,
    val code: String? = null
)

interface GroqApiService {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: OpenAiChatRequest
    ): OpenAiChatResponse
}

object GroqApiClient {
    private const val BASE_URL = "https://api.groq.com/openai/v1/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    val apiService: GroqApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GroqApiService::class.java)
    }
}
