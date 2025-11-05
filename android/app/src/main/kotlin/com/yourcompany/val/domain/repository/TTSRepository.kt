package com.yourcompany.val.domain.repository

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume

class TTSRepository(context: Context) {
    private var tts: TextToSpeech? = null
    private var onSpeechComplete: (() -> Unit)? = null
    private var isInitialized = false
    
    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(1.0f)
                tts?.setPitch(1.0f)
                
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        // Speech started
                    }
                    
                    override fun onDone(utteranceId: String?) {
                        onSpeechComplete?.invoke()
                    }
                    
                    override fun onError(utteranceId: String?) {
                        onSpeechComplete?.invoke()
                    }
                })
                
                isInitialized = true
            }
        }
    }
    
    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (!isInitialized) {
            onComplete?.invoke()
            return
        }
        
        onSpeechComplete = onComplete
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
    }
    
    suspend fun speakAwait(text: String) = suspendCancellableCoroutine { continuation ->
        speak(text) {
            continuation.resume(Unit)
        }
    }
    
    fun stop() {
        tts?.stop()
    }
    
    fun release() {
        tts?.shutdown()
    }
    
    fun setVoice(voiceName: String) {
        val voices = tts?.voices ?: return
        val selectedVoice = voices.find { it.name == voiceName }
        selectedVoice?.let { tts?.voice = it }
    }
    
    fun getAvailableVoices(): List<String> {
        return tts?.voices?.map { it.name } ?: emptyList()
    }
}
