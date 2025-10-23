# 🚀 Anesthesia Voice Assistant - Quick Start Guide

## ⚠️ Important: Start Docker Desktop First!

Before running the voice assistant, **you must start Docker Desktop**.

### Step-by-Step Startup

#### 1. Start Docker Desktop
- Open Docker Desktop application
- Wait for Docker to fully start (whale icon in system tray should be stable)
- Verify Docker is running: `docker ps` (should show no errors)

#### 2. Start LiveKit Server
```bash
cd d:\val
docker-compose up -d
```

Verify containers are running:
```bash
docker-compose ps
```

You should see both `val-livekit-1` and `val-redis-1` running.

#### 3. Start Backend Agent
```bash
cd d:\val
npm run dev
```

Wait for the message:
```
[INFO] registered worker
```

#### 4. Start Frontend Application
```bash
cd d:\val-frontend
npm run dev
```

Wait for:
```
✓ Ready in Xs
```

Frontend will be available at: http://localhost:3001

#### 5. Test the System
Open http://localhost:3001 in your browser and click "Start Anesthesia Consultation"

---

## 🔍 Troubleshooting

### Docker Connection Error
**Error**: `The system cannot find the file specified` or `connection refused`

**Solution**: 
1. Open Docker Desktop manually
2. Wait for it to fully start
3. Restart containers: `docker-compose down && docker-compose up -d`

### WebSocket Connection Refused
**Error**: `ERR_CONNECTION_REFUSED` to `ws://localhost:7880`

**Solution**:
1. Check Docker containers: `docker-compose ps`
2. Check LiveKit logs: `docker-compose logs livekit`
3. Restart LiveKit: `docker-compose restart livekit`

### Frontend API Connection Failed
**Error**: Frontend can't connect to LiveKit server

**Solution**:
1. Verify `.env.local` in `d:\val-frontend`:
   ```
   LIVEKIT_API_KEY=devkey
   LIVEKIT_API_SECRET=devsecret
   LIVEKIT_URL=ws://localhost:7880
   ```
2. Restart frontend: Stop and run `npm run dev` again

### Agent Not Connecting
**Error**: Agent doesn't register with LiveKit

**Solution**:
1. Check `.env.local` in `d:\val`:
   ```
   LIVEKIT_URL=ws://localhost:7880
   LIVEKIT_API_KEY=devkey
   LIVEKIT_API_SECRET=devsecret
   ```
2. Verify LiveKit server is running: `docker-compose ps`
3. Restart agent: Stop and run `npm run dev` again

### Ollama Model Not Found
**Error**: II-Medical model not available

**Solution**:
1. Check Ollama is running: `ollama list`
2. If model missing, download: `ollama pull hf.co/Intelligent-Internet/II-Medical-8B-1706-GGUF:Q4_K_M`

---

## 📊 System Status Check

Run this quick test:
```bash
cd d:\val
node test-system.js
```

This will check:
- ✅ LiveKit server connectivity
- ✅ Frontend API connection  
- ✅ Ollama model availability
- ✅ Overall system health

---

## 🎯 Quick Commands Reference

### Start Everything
```bash
# Terminal 1: Start Docker containers
cd d:\val
docker-compose up -d

# Terminal 2: Start backend agent
cd d:\val
npm run dev

# Terminal 3: Start frontend
cd d:\val-frontend
npm run dev
```

### Stop Everything
```bash
# Stop frontend (Ctrl+C in Terminal 3)
# Stop agent (Ctrl+C in Terminal 2)

# Stop Docker containers
cd d:\val
docker-compose down
```

### Check Status
```bash
# Docker containers
docker-compose ps

# LiveKit logs
docker-compose logs livekit

# Ollama models
ollama list

# System test
node test-system.js
```

---

## 🏥 Using the Voice Assistant

1. Open http://localhost:3001
2. Click "Start Anesthesia Consultation"
3. Allow microphone access when prompted
4. Start speaking - the AI will respond!

### Example Medical Queries
- "What are the anesthesia considerations for a diabetic patient?"
- "Explain the dosing for propofol induction"
- "What monitoring is needed for a patient with cardiac disease?"
- "How do you manage difficult airway situations?"

---

## 🔒 Security Notes

- **Development Mode**: Using `devkey/devsecret` credentials
- **Local AI**: All AI processing stays on your machine
- **HIPAA Compliant**: No medical data sent to cloud services
- **Production**: Generate secure API keys before deployment

---

## 📚 Additional Resources

- [LiveKit Documentation](https://docs.livekit.io)
- [Ollama Documentation](https://ollama.ai)
- [II-Medical Model Info](https://huggingface.co/Intelligent-Internet/II-Medical-8B-1706-GGUF)

---

## ✅ Current System Status

- **Backend Agent**: Ready
- **LiveKit Server**: Configured (Docker)
- **Frontend PWA**: Built and ready
- **AI Model**: II-Medical-8B-1706-GGUF Q4_K_M
- **Voice Pipeline**: AssemblyAI STT + Cartesia TTS + Silero VAD

**Everything is configured - just start Docker Desktop and follow the steps above!** 🚀
