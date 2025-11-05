package com.yourcompany.val.domain.repository

import android.content.Context
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.*
import java.io.File

enum class LLMProvider {
    LOCAL_AI,      // LocalAI on your PC
    ON_DEVICE      // llama.cpp on phone
}

class LLMRepository(
    private val context: Context,
    private val localAiUrl: String = "http://192.168.1.100:8080" // Change to your PC's IP
) {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    private var currentProvider: LLMProvider = LLMProvider.LOCAL_AI
    private var localLlama: LocalLlamaWrapper? = null
    
    init {
        // Try to initialize on-device LLM
        initializeOnDeviceLLM()
    }
    
    /**
     * Main entry point - automatically picks best available provider
     */
    suspend fun streamChat(
        messages: List<Map<String, String>>,
        onChunk: (String) -> Unit,
        onProviderChange: (LLMProvider) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        // Try LocalAI first (your PC)
        val localAiSuccess = tryLocalAI(messages, onChunk)
        
        if (localAiSuccess) {
            currentProvider = LLMProvider.LOCAL_AI
            withContext(Dispatchers.Main) {
                onProviderChange(LLMProvider.LOCAL_AI)
            }
            return@withContext
        }
        
        // Fallback to on-device LLM
        currentProvider = LLMProvider.ON_DEVICE
        withContext(Dispatchers.Main) {
            onProviderChange(LLMProvider.ON_DEVICE)
        }
        
        tryOnDeviceLLM(messages, onChunk)
    }
    
    /**
     * Try LocalAI on your PC first (fast, free, good quality)
     */
    private suspend fun tryLocalAI(
        messages: List<Map<String, String>>,
        onChunk: (String) -> Unit
    ): Boolean {
        return try {
            // Quick health check with 2 second timeout
            val isAvailable = withTimeoutOrNull(2000) {
                try {
                    client.get("$localAiUrl/health")
                    true
                } catch (e: Exception) {
                    false
                }
            } ?: false
            
            if (!isAvailable) return false
            
            // LocalAI is available, stream from it
            val response: HttpResponse = client.post("$localAiUrl/v1/chat/completions") {
                header("Content-Type", "application/json")
                setBody(buildJsonRequest(messages, "gpt-3.5-turbo")) // Or your LocalAI model name
            }
            
            val channel: ByteReadChannel = response.bodyAsChannel()
            
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: continue
                
                if (line.startsWith("data: ")) {
                    val data = line.substring(6).trim()
                    if (data == "[DONE]") break
                    
                    try {
                        val json = Json.parseToJsonElement(data).jsonObject
                        val delta = json["choices"]
                            ?.jsonArray
                            ?.firstOrNull()
                            ?.jsonObject
                            ?.get("delta")
                            ?.jsonObject
                        
                        val content = delta?.get("content")?.jsonPrimitive?.content
                        if (content != null) {
                            withContext(Dispatchers.Main) {
                                onChunk(content)
                            }
                        }
                    } catch (e: Exception) {
                        // Skip malformed JSON
                    }
                }
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Fallback to on-device llama.cpp (works offline)
     */
    private suspend fun tryOnDeviceLLM(
        messages: List<Map<String, String>>,
        onChunk: (String) -> Unit
    ) {
        if (localLlama == null) {
            withContext(Dispatchers.Main) {
                onChunk("⚠️ On-device LLM not initialized. Please download model in settings.")
            }
            return
        }
        
        // Convert messages to prompt
        val prompt = buildPrompt(messages)
        
        // Stream tokens from llama.cpp
        localLlama?.generate(prompt) { token ->
            withContext(Dispatchers.Main) {
                onChunk(token)
            }
        }
    }
    
    /**
     * Initialize on-device llama.cpp
     */
    private fun initializeOnDeviceLLM() {
        try {
            val modelPath = File(context.filesDir, "models/llama-3.2-1b-q4.gguf")
            if (modelPath.exists()) {
                localLlama = LocalLlamaWrapper(modelPath.absolutePath)
            }
        } catch (e: Exception) {
            // Model not available, that's okay
        }
    }
    
    /**
     * Build prompt from messages (for on-device LLM)
     */
    private fun buildPrompt(messages: List<Map<String, String>>): String {
        return buildString {
            messages.forEach { message ->
                val role = message["role"] ?: "user"
                val content = message["content"] ?: ""
                
                when (role) {
                    "system" -> append("<|system|>\n$content\n")
                    "user" -> append("<|user|>\n$content\n")
                    "assistant" -> append("<|assistant|>\n$content\n")
                }
            }
            append("<|assistant|>\n")
        }
    }
    
    /**
     * Build JSON request for cloud APIs
     */
    private fun buildJsonRequest(
        messages: List<Map<String, String>>,
        model: String
    ): String {
        return buildJsonObject {
            put("model", model)
            put("stream", true)
            put("max_tokens", 512)
            put("temperature", 0.7)
            putJsonArray("messages") {
                messages.forEach { message ->
                    addJsonObject {
                        put("role", message["role"] ?: "user")
                        put("content", message["content"] ?: "")
                    }
                }
            }
        }.toString()
    }
    
    fun getCurrentProvider() = currentProvider
    
    fun cleanup() {
        localLlama?.cleanup()
        client.close()
    }
}

/**
 * Wrapper for llama.cpp JNI (to be implemented)
 */
private class LocalLlamaWrapper(private val modelPath: String) {
    private var ctx: Long = 0
    
    init {
        // Load llama.cpp native library
        System.loadLibrary("llama")
        ctx = initLlama(modelPath)
    }
    
    suspend fun generate(prompt: String, onToken: suspend (String) -> Unit) {
        // This will call native llama.cpp
        generateNative(ctx, prompt, onToken)
    }
    
    fun cleanup() {
        if (ctx != 0L) {
            freeLlama(ctx)
            ctx = 0
        }
    }
    
    // Native methods (to be implemented with JNI)
    private external fun initLlama(modelPath: String): Long
    private external suspend fun generateNative(ctx: Long, prompt: String, onToken: suspend (String) -> Unit)
    private external fun freeLlama(ctx: Long)
}
