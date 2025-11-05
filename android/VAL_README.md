# Val - Voice AI Assistant (Android)

> **Consolidated Project:** This combines `val-android` (prototype) with `val-android-real` (Transcribro fork) into a production-ready Android voice assistant app.

## 🎯 What This Is

A fully-featured Android voice AI assistant with:
- ✅ **On-device STT** - Using whisper.cpp from Transcribro
- ✅ **On-device VAD** - Silero VAD for voice activity detection
- ✅ **Hybrid LLM** - LocalAI on PC (primary) + on-device fallback
- ✅ **On-device TTS** - Android's TextToSpeech API
- ✅ **Modern UI** - Material 3 with smooth animations (inspired by val-frontend)

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Enhanced Chat UI (Jetpack Compose)         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Chat Entry   │  │ Control Bar  │  │ Status Bar   │  │
│  │ (Messages)   │  │ (Mic/Chat)   │  │ (LLM Status) │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└────────────────────────┬────────────────────────────────┘
                         ↓
              ┌──────────────────────┐
              │    ChatViewModel     │
              │  (State Management)  │
              └──────────┬───────────┘
                         ↓
       ┌─────────────────┼─────────────────┐
       ↓                 ↓                 ↓
┌────────────┐  ┌────────────────┐  ┌────────────┐
│ Transcribro│  │  LLMRepository │  │    TTS     │
│  (STT+VAD) │  │  (Hybrid LLM)  │  │ Repository │
└────────────┘  └────────────────┘  └────────────┘
       ↓                 ↓                 ↓
  whisper.cpp      1. LocalAI (PC)    Android TTS
  + Silero VAD     2. On-Device LLM
```

## 📁 Project Structure

```
val-android-real/
├── app/src/main/kotlin/
│   ├── dev/soupslurpr/transcribro/        # From Transcribro fork
│   │   ├── recognitionservice/            # STT + VAD
│   │   │   ├── MainRecognitionService.kt
│   │   │   ├── whisper/                   # whisper.cpp integration
│   │   │   └── silerovad/                 # Silero VAD integration
│   │   ├── MainActivity.kt                # Original Transcribro UI
│   │   └── Transcribro.kt
│   │
│   └── com/yourcompany/val/               # NEW: Val voice assistant
│       ├── ui/
│       │   ├── MainActivity.kt            # Val entry point
│       │   ├── chat/
│       │   │   ├── ChatComponents.kt      # Chat UI components
│       │   │   ├── ControlBar.kt          # Bottom control bar
│       │   │   ├── EnhancedChatScreen.kt  # Main screen
│       │   │   └── ChatViewModel.kt       # State management
│       │   └── theme/
│       │       ├── Theme.kt               # Material 3 theme
│       │       └── Type.kt                # Typography
│       └── domain/
│           ├── model/
│           │   └── Message.kt             # Data models
│           └── repository/
│               ├── LLMRepository.kt       # Hybrid LLM logic
│               └── TTSRepository.kt       # TTS wrapper
│
├── whisper.cpp/                           # Submodule for STT
├── build.gradle.kts
└── README.md                              # This file
```

## 🚀 Getting Started

### Prerequisites

1. **Android Studio** (latest version)
2. **LocalAI running on your PC** (see LOCALAI_SETUP.md)
3. **Android device** with 6GB+ RAM

### Setup Steps

1. **Clone the repository**
   ```bash
   cd d:\val-android-real
   git pull  # Get latest changes
   ```

2. **Update LocalAI URL**
   
   Edit `ChatViewModel.kt`:
   ```kotlin
   private val llmRepository = LLMRepository(
       context = application,
       localAiUrl = "http://YOUR_PC_IP:8080"  // Change this!
   )
   ```

3. **Open in Android Studio**
   - File → Open → Select `d:\val-android-real`
   - Wait for Gradle sync

4. **Build and Run**
   - Click Run (Shift+F10)
   - Grant microphone permission
   - Tap microphone button to start!

## 🎨 UI Components (Ported from val-frontend)

### ChatComponents.kt
- **ChatEntry** - Individual message bubbles with timestamps
- **ChatTranscript** - Scrollable message list with fade effects
- **EmptyState** - Welcome screen when no messages
- **TypingIndicator** - Animated cursor for streaming responses

### ControlBar.kt
- **AgentControlBar** - Bottom control panel
- **MicrophoneButton** - Pulsing animation when listening
- **ChatInput** - Text input with send button
- **StatusIndicator** - Shows current state (listening/thinking/speaking)

### EnhancedChatScreen.kt
- **Main screen** - Combines all components
- **Animations** - Smooth transitions matching val-frontend
- **State management** - Reactive UI with Compose flows

## 🆕 What's New vs. Original val-android

| Feature | Original (val-android) | Enhanced (val-android-real) |
|---------|------------------------|------------------------------|
| **UI Framework** | Basic Material 3 | Enhanced with animations & gradients |
| **Chat Bubbles** | Simple rectangles | Rounded bubbles with timestamps |
| **Animations** | Minimal | Fade in/out, slide, pulse effects |
| **Status Display** | Basic text | Animated status indicator |
| **Empty State** | None | Welcome screen with start button |
| **Control Bar** | Simple FAB | Full control panel with chat toggle |
| **Message List** | Basic LazyColumn | Scrollable with fade gradients |
| **Typing Indicator** | No | Animated cursor during streaming |

## 💰 Cost Comparison

| Component | Solution | Cost |
|-----------|----------|------|
| **STT** | Transcribro (whisper.cpp) | **$0** |
| **VAD** | Transcribro (Silero) | **$0** |
| **LLM** | LocalAI on PC | **$0** |
| **TTS** | Android API | **$0** |
| **Total** | | **$0/month** 🎉 |

vs. Cloud-based:
- OpenAI Whisper: $0.006/min ($50-100/mo)
- OpenAI GPT-4: $0.03/1K tokens ($20-50/mo)
- Cloud TTS: $4/1M chars ($50-100/mo)
- **Total cloud cost: ~$120-250/month**

## 🔧 Configuration

### Update PC IP Address
Find your PC's IP:
```powershell
# Windows
Get-NetIPAddress -AddressFamily IPv4 | Where-Object {$_.InterfaceAlias -like "*Wi-Fi*"}

# Mac/Linux
ifconfig | grep "inet " | grep -v 127.0.0.1
```

Update `ChatViewModel.kt`:
```kotlin
localAiUrl = "http://192.168.1.XXX:8080"
```

### Switch LLM Model
In `LLMRepository.kt`, change:
```kotlin
setBody(buildJsonRequest(messages, "llama3.2:3b"))
```

Options:
- `llama3.2:3b` - Fast, good quality (recommended)
- `mistral:7b` - Better quality, slower
- `phi3:mini` - Fastest, lower quality

## 📱 Usage

1. **Start the app**
2. **Tap microphone** to speak
3. **Wait for transcription** (on-device)
4. **AI thinks** (LocalAI on PC or on-device fallback)
5. **Hear response** (Android TTS)

### Chat Mode
- Tap **chat icon** to open message history
- Type messages instead of speaking
- Toggle back to voice mode anytime

### Status Display
- 🎤 **Listening** - Recording your voice
- ⚙️ **Processing** - Transcribing speech
- 💭 **Thinking** - LLM generating response
  - Shows "🏠 LocalAI" or "📱 On-Device"
- 🔊 **Speaking** - Playing TTS response

## 🐛 Troubleshooting

### "Cannot resolve symbol 'MainRecognitionService'"
Make sure you're using the Transcribro fork correctly. The service should exist at:
```
dev.soupslurpr.transcribro.recognitionservice.MainRecognitionService
```

### "LocalAI not responding"
1. Check LocalAI is running: `http://YOUR_PC_IP:8080/health`
2. Verify phone and PC on same Wi-Fi
3. Check firewall allows port 8080

### "No speech detected"
1. Grant microphone permission
2. Check device volume
3. Try speaking louder/clearer
4. Check Transcribro models are installed

### App crashes on startup
1. Clean and rebuild: Build → Clean Project
2. Invalidate caches: File → Invalidate Caches / Restart
3. Check all dependencies installed

## 📚 Documentation

- **LOCALAI_SETUP.md** - How to setup LocalAI on your PC
- **ON_DEVICE_LLM_SETUP.md** - Add on-device LLM fallback
- **HYBRID_LLM_SUMMARY.md** - Architecture overview
- **Transcribro README** - Original STT/VAD documentation

## 🎯 Roadmap

### Phase 1: Core Features (✅ Done)
- [x] Port UI from val-frontend
- [x] Integrate Transcribro STT/VAD
- [x] Hybrid LLM support
- [x] Android TTS integration
- [x] Material 3 theming

### Phase 2: Enhancements (Next)
- [ ] Conversation history persistence (Room DB)
- [ ] Settings screen (model selection, voice options)
- [ ] Push-to-talk mode
- [ ] Voice activity visualization
- [ ] Export conversations

### Phase 3: Advanced (Future)
- [ ] On-device LLM fallback (llama.cpp)
- [ ] Multi-language support
- [ ] Custom wake word
- [ ] Offline mode improvements
- [ ] Background conversation

## 🤝 Credits

- **Transcribro** by [@soupslurpr](https://github.com/soupslurpr/Transcribro) - STT + VAD foundation
- **val-frontend** - Original UI design inspiration
- **whisper.cpp** - On-device speech recognition
- **Silero VAD** - Voice activity detection
- **LocalAI** - OpenAI-compatible local LLM server

## 📄 License

See LICENSE files for individual components:
- LICENSE.txt - Main project
- LICENSE.whisper.cpp.txt
- LICENSE.silero-vad.txt

## 🚀 Ready to Build!

This is the **production-ready** consolidation of:
1. **val-android** (prototype with Kotlin UI code)
2. **val-android-real** (Transcribro fork with working STT/VAD)

Everything is now in `val-android-real` with:
- ✅ Working STT/VAD from Transcribro
- ✅ Modern UI from val-frontend (ported to Kotlin)
- ✅ Hybrid LLM architecture
- ✅ All components integrated

**Just update your PC's IP and start building!** 🎉
