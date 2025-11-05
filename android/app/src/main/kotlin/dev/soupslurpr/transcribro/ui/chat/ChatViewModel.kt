package dev.soupslurpr.transcribro.ui.chat

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.soupslurpr.transcribro.domain.model.ChatState
import dev.soupslurpr.transcribro.domain.model.Message
import dev.soupslurpr.transcribro.domain.repository.LLMRepository
import dev.soupslurpr.transcribro.domain.repository.TTSRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    
    // State flows
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private val _chatState = MutableStateFlow(ChatState.IDLE)
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()
    
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Repositories
    private val ttsRepository = TTSRepository(application)
    private val llmRepository = LLMRepository(
        context = application,
        localAiUrl = "http://192.168.1.111:8080" // TODO: Change to your PC's IP address
    )
    
    private val _currentLLMProvider = MutableStateFlow("Checking...")
    val currentLLMProvider: StateFlow<String> = _currentLLMProvider.asStateFlow()
    
    // Speech recognizer
    private var speechRecognizer: SpeechRecognizer? = null
    
    init {
        initializeSpeechRecognizer()
    }
    
    private fun initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(
            getApplication(),
            ComponentName(
                getApplication(),
                // Using Transcribro's MainRecognitionService
                Class.forName("dev.soupslurpr.transcribro.recognitionservice.MainRecognitionService")
            )
        )
        
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _chatState.value = ChatState.LISTENING
                _isListening.value = true
            }
            
            override fun onBeginningOfSpeech() {
                // User started speaking
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                // Audio level changed - could use for visualization
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
                // Raw audio buffer
            }
            
            override fun onEndOfSpeech() {
                _isListening.value = false
                _chatState.value = ChatState.PROCESSING_SPEECH
            }
            
            override fun onError(error: Int) {
                _isListening.value = false
                _chatState.value = ChatState.IDLE
                
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission denied"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error: $error"
                }
                _errorMessage.value = errorMsg
            }
            
            override fun onResults(results: Bundle?) {
                val transcription = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                
                if (transcription != null) {
                    handleUserSpeech(transcription)
                }
                
                _isListening.value = false
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                // Could show partial transcription
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
                // Additional events
            }
        })
    }
    
    fun startListening() {
        if (_isListening.value) return
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to start listening: ${e.message}"
        }
    }
    
    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }
    
    fun handleUserSpeech(transcription: String) {
        // Add user message
        addMessage(Message(text = transcription, isUser = true))
        
        // Get LLM response
        getLLMResponse(transcription)
    }
    
    private fun getLLMResponse(userMessage: String) {
        _chatState.value = ChatState.WAITING_FOR_LLM
        
        viewModelScope.launch {
            try {
                // Create conversation history for LLM
                val conversationHistory = _messages.value.map { message ->
                    mapOf(
                        "role" to if (message.isUser) "user" else "assistant",
                        "content" to message.text
                    )
                }
                
                // Create streaming response message
                val responseId = java.util.UUID.randomUUID().toString()
                var responseText = ""
                
                addMessage(Message(
                    id = responseId,
                    text = "",
                    isUser = false,
                    isStreaming = true
                ))
                
                // Stream LLM response with automatic fallback
                llmRepository.streamChat(
                    messages = conversationHistory,
                    onChunk = { chunk ->
                        responseText += chunk
                        updateMessage(responseId, responseText, isStreaming = true)
                    },
                    onProviderChange = { provider ->
                        _currentLLMProvider.value = when (provider) {
                            dev.soupslurpr.transcribro.domain.repository.LLMProvider.LOCAL_AI -> 
                                "🏠 LocalAI (Your PC)"
                            dev.soupslurpr.transcribro.domain.repository.LLMProvider.ON_DEVICE -> 
                                "📱 On-Device"
                        }
                    }
                )
                
                // Finalize message
                updateMessage(responseId, responseText, isStreaming = false)
                
                // Speak the response
                _chatState.value = ChatState.SPEAKING
                ttsRepository.speak(responseText) {
                    _chatState.value = ChatState.IDLE
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "LLM error: ${e.message}"
                _chatState.value = ChatState.IDLE
            }
        }
    }
    
    private fun addMessage(message: Message) {
        _messages.value = _messages.value + message
    }
    
    private fun updateMessage(id: String, text: String, isStreaming: Boolean) {
        _messages.value = _messages.value.map { message ->
            if (message.id == id) {
                message.copy(text = text, isStreaming = isStreaming)
            } else {
                message
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        ttsRepository.release()
        llmRepository.cleanup()
    }
}
