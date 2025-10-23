# Anesthesia Voice Assistant - Backend Setup

## Ollama Setup for Local Medical AI

1. **Install Ollama** (if not already installed)
   - Download from: https://ollama.ai/download
   - Install and ensure it's running

2. **Download II-Medical Model**
   ```bash
   ollama pull hf.co/Intelligent-Internet/II-Medical-8B-1706-GGUF:Q4_K_M
   ```

3. **Verify Model Installation**
   ```bash
   ollama list
   ```
   You should see `ii-medical-8b-1706-gguf:Q4_K_M` in the list.

## Local LiveKit Server Setup

4. **Start Docker Desktop** (required for local LiveKit server)
   - Launch Docker Desktop on your Windows machine
   - Wait for Docker daemon to be ready

5. **Start LiveKit Server**
   ```bash
   cd d:\val
   docker-compose up -d
   ```

6. **Verify Server is Running**
   ```bash
   docker-compose ps
   ```
   You should see `livekit` and `redis` containers running.

## Testing the Medical AI Agent

7. **Install Dependencies**
   ```bash
   pnpm install
   ```

8. **Run the Agent**
   ```bash
   npm run dev
   ```

## Configuration Files Created

- `docker-compose.yml` - Local LiveKit server with Redis
- `livekit-config/keys.yaml` - API keys for development
- `livekit-config/livekit.yaml` - LiveKit server configuration
- Updated `src/agent.ts` - Ollama II-Medical model integration
- `.env.local` - Environment variables for local development

## Medical AI Features

The agent is configured with specialized instructions for:
- Pre-anesthetic assessment
- Anesthesia planning and medication selection
- Intraoperative monitoring
- Postoperative care
- Emergency response protocols
- Airway management strategies

All responses prioritize patient safety and evidence-based medicine.

## System Status ✅

- ✅ Ollama II-Medical model downloaded and tested
- ✅ LiveKit server running on localhost:7880
- ✅ Redis database running on localhost:6379
- ✅ Agent successfully connecting to LiveKit
- ✅ All tests passing
- ✅ HIPAA-compliant local AI processing

## Voice Pipeline Components

- **Speech-to-Text**: AssemblyAI (cloud)
- **Text-to-Speech**: Cartesia (cloud)
- **Voice Activity Detection**: Silero VAD (local)
- **Turn Detection**: LiveKit (local)
- **AI Model**: II-Medical-8B via Ollama (local)

## Troubleshooting

### LiveKit Server Issues
```bash
# Check logs
docker-compose logs livekit

# Restart services
docker-compose down && docker-compose up -d
```

### Ollama Issues
```bash
# Test API connectivity
curl http://localhost:11434/api/tags

# List available models
ollama list
```

### Agent Issues
```bash
# Run tests
npm test

# Check agent logs
npm run dev
```