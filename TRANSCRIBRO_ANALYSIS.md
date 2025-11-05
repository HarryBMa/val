# Transcribro Analysis for Val Project

## Overview
Transcribro is an Android app that provides **on-device STT + VAD**. We want to adapt its approach for our LiveKit-based voice AI project.

## What Transcribro Uses

### 1. **Silero VAD (Voice Activity Detection)** ✅
- **Technology**: ONNX Runtime + Silero VAD model
- **Status in our project**: ✅ Already using `@livekit/agents-plugin-silero`
- **Location in Transcribro**: 
  - `app/src/main/kotlin/dev/soupslurpr/transcribro/recognitionservice/silerovad/`
  - Model: `app/src/main/assets/models/silero_vad/silero_vad.with_runtime_opt.ort`

**Key Configuration from Transcribro:**
```kotlin
val SAMPLE_RATE = 16000
val START_THRESHOLD = 0.6f
val END_THRESHOLD = 0.45f
val MIN_SILENCE_DURATION_MS = 3000
val SPEECH_PAD_MS = 0
```

### 2. **Whisper.cpp (STT - Speech to Text)** ⚠️
- **Technology**: whisper.cpp (C++ with JNI bindings for Android)
- **Status in our project**: ❌ Currently using cloud-based AssemblyAI
- **Location in Transcribro**: 
  - Native code: `lib/src/main/jni/whisper/`
  - Kotlin wrapper: `lib/src/main/java/com/whispercpp/whisper/`
  - Model: `app/src/main/assets/models/whisper/ggml-model-whisper-tiny.en-q8_0.bin`

**Key Implementation Details:**
```kotlin
// From WhisperRepository.kt
suspend fun transcribeAudio(data: ShortArray): String {
    loadWhisperContextIfNull()
    // Convert to float array normalized to [-1, 1]
    var buffer = FloatArray(data.size) { index ->
        (data[index] / 32767.0f).coerceIn(-1f..1f)
    }
    
    // Minimum 32000 samples (2 seconds at 16kHz)
    if (data.size < 32000) {
        val newBuffer = FloatArray(32000)
        for ((i, value) in buffer.withIndex()) {
            newBuffer[i] = value
        }
        newBuffer.fill(0f, data.size, newBuffer.size)
        buffer = newBuffer
    }
    
    val transcript = whisperContext.value?.transcribeData(buffer, ((data.size / 16000f) * 1000f).toLong()) ?: ""
    return transcript.removeSuffix(" .") // remove hallucination
}
```

### 3. **TTS (Text to Speech)** ❌
- **Status**: Transcribro doesn't have TTS (it's STT-only for keyboard input)
- **Status in our project**: ⚠️ Currently using cloud-based Cartesia (via LiveKit Cloud)

## What We Can Adapt

### Option 1: **Use whisper.cpp in Browser** (Recommended for Web)
Since you have a React/Next.js frontend, you can use:
- **[whisper.cpp compiled to WASM](https://github.com/ggerganov/whisper.cpp/tree/master/examples/wasm)** for browser-based STT
- **[transformers.js](https://github.com/xenova/transformers.js)** for browser ML models (includes Whisper)

### Option 2: **Node.js Whisper Integration** (Backend)
For your Node.js agent:
- **[@livekit/agents-plugin-whisper](https://docs.livekit.io/agents/models/stt/)** (if available)
- **[whisper-node](https://github.com/arilyn-cc/whisper-node)** - Node.js bindings for whisper.cpp
- **[openai-whisper-turbo](https://www.npmjs.com/package/openai-whisper-turbo)** - OpenAI's API

### Option 3: **For TTS - Browser-based Solutions**
Since Transcribro doesn't help with TTS, here are options:
- **[Web Speech API](https://developer.mozilla.org/en-US/docs/Web/API/Web_Speech_API)** (native browser TTS)
- **[Coqui TTS](https://github.com/coqui-ai/TTS)** (open-source, can run in browser via WASM)
- **[@mozilla/tts](https://github.com/mozilla/TTS)** (Firefox's TTS)
- **[transformers.js with Bark](https://huggingface.co/docs/transformers.js/index)** (browser ML TTS)

## Architecture Comparison

### Transcribro Architecture:
```
[Android App]
    ↓
[AudioRecord (16kHz)] → [Silero VAD] → [Whisper.cpp STT]
    ↓                        ↓
[Voice Input]          [Speech Detection]
```

### Your Current Architecture:
```
[React Frontend] ←→ [LiveKit Cloud] ←→ [Node.js Agent]
                          ↓
        [OpenAI LLM + AssemblyAI STT + Cartesia TTS]
```

### Proposed Hybrid Architecture:
```
[React Frontend]
    ↓
[Browser Audio API] → [Silero VAD (WASM)] → [Whisper WASM STT]
    ↓                                              ↓
[LiveKit Client] ←→ [LiveKit Cloud] ←→ [Node.js Agent]
    ↓                                              ↓
[Web Speech TTS]              [OpenAI LLM (cloud or local)]
```

## Implementation Steps

### Phase 1: **Keep Current Setup** (already done) ✅
- You already have VAD via `@livekit/agents-plugin-silero`
- Continue using cloud STT/TTS for now

### Phase 2: **Add On-Device STT** (focus on this)
1. Add whisper.cpp WASM to your React frontend
2. Process audio in browser before sending to LiveKit
3. Reduce cloud API costs

### Phase 3: **Add On-Device TTS** (optional)
1. Implement Web Speech API or transformers.js
2. Generate speech client-side
3. Further reduce latency and costs

## Code Snippets to Adapt

### From MainRecognitionService.kt (VAD Configuration):
```kotlin
// These values can be used in your LiveKit Silero plugin config
val audioFormat = AudioFormat.Builder()
    .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
    .setSampleRate(16000)
    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
    .build()

val SAMPLE_RATE = 16000
val START_THRESHOLD = 0.6f
val END_THRESHOLD = 0.45f
val MIN_SILENCE_DURATION_MS = 3000
val SPEECH_PAD_MS = 24000  // 24ms padding before speech start
```

### Transcribro's VAD Flow:
```kotlin
// Detect speech segments
sileroVadRepository.detect(buffer)?.forEach {
    when (it.key) {
        "start" -> {
            isSpeaking = true
            listener?.beginningOfSpeech()
            transcriptions[transcriptionIndex]!!.start = it.value
        }
        "end" -> {
            isSpeaking = false
            listener?.endOfSpeech()
            transcription.end = it.value
            // Then trigger Whisper transcription
            transcribeJob = transcribeScope.launch {
                transcription.text = whisperRepository.transcribeAudio(
                    transcription.audioData.slice(
                        ((transcription.start!! - speechStartPadMs).coerceAtLeast(0))
                        ..((transcription.end!!).coerceAtMost(transcription.audioData.size - 1))
                    ).toShortArray()
                )
            }
        }
    }
}
```

## Key Takeaways

1. **VAD**: You're already good with Silero VAD ✅
2. **STT**: Transcribro uses whisper.cpp (C++) - you need JavaScript equivalent
3. **TTS**: Transcribro doesn't have this - need separate solution
4. **Focus**: Prioritize **browser-based Whisper WASM** for on-device STT
5. **LLM**: Keep cloud-based for now, or explore local models later

## Next Steps

1. ✅ Review Silero VAD configuration in your agent
2. 🔄 Implement whisper.cpp WASM in React frontend
3. 🔄 Add Web Speech API or transformers.js TTS
4. 📊 Compare performance: cloud vs on-device
5. 💰 Measure cost savings

## Useful Resources

- [whisper.cpp WASM example](https://github.com/ggerganov/whisper.cpp/tree/master/examples/wasm)
- [transformers.js docs](https://huggingface.co/docs/transformers.js)
- [LiveKit Agents STT plugins](https://docs.livekit.io/agents/models/stt/)
- [Web Speech API guide](https://developer.mozilla.org/en-US/docs/Web/API/Web_Speech_API)
- [Silero VAD implementation](https://github.com/snakers4/silero-vad)

## License Compatibility

✅ **Whisper.cpp**: MIT License (compatible)
✅ **Silero VAD**: MIT License (compatible)
✅ **Transcribro**: AGPL-3.0 (code adapted, not copied)
