# Lumina AI - Android Personality Assistant

**Advanced AI-Powered Personal Operating System Controller & Automation Platform**

---

## 🎯 Project Overview

Lumina AI is a sophisticated Android application that acts as an **intelligent personality-driven assistant** capable of:

- **Natural Language Processing** - Chat with Gemini AI & Groq API
- **Voice Recognition & Synthesis** - Interact via voice commands  
- **Screen Automation** - Capture and analyze screen content
- **Macro Engine** - Record and replay complex automation sequences
- **WhatsApp Integration** - Automated message responses
- **Gaming Vision** - Real-time screen analysis for gaming
- **Multi-Agent Support** - Deploy multiple AI personalities
- **Screen Control** - Touch automation and system interaction

---

## 🏗️ Architecture

```
lumina-online-/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt              # Main UI controller & navigation
│   │   │   ├── ui/
│   │   │   │   ├── screens/                 # Composable UI screens
│   │   │   │   │   ├── OrbHomeScreen.kt     # Main personality hub
│   │   │   │   │   ├── ChatConsoleScreen.kt # AI chat interface
│   │   │   │   │   ├── AutomationScreen.kt  # OS control & touch automation
│   │   │   │   │   ├── MacroEngineScreen.kt # Macro recording/playback
│   │   │   │   │   ├── WhatsAppHubScreen.kt # Message automation
│   │   │   │   │   ├── GamingVisionScreen.kt# Gaming analysis
│   │   │   │   │   └── SettingsScreen.kt    # App configuration
│   │   │   │   ├── components/              # Reusable UI components
│   │   │   │   ├── theme/                   # Material Design 3 theming
│   │   │   │   └── viewmodel/               # MVVM ViewModels
│   │   │   ├── data/
│   │   │   │   ├── local/                   # Room Database (chat, macros, replies)
│   │   │   │   └── remote/                  # Retrofit API clients
│   │   │   │       ├── GeminiService.kt     # Google Gemini API integration
│   │   │   │       └── GroqService.kt       # Groq API integration
│   │   │   ├── service/
│   │   │   │   ├── ScreenCaptureService.kt  # MediaProjection screen capture
│   │   │   │   ├── VoiceSpeechEngine.kt     # TTS & speech recognition
│   │   │   │   ├── WhatsAppAutomation.kt    # Message listener & responder
│   │   │   │   └── MqttService.kt           # MQTT real-time communication
│   │   │   └── utils/                       # Helper utilities
│   │   ├── res/                             # Resources (layouts, strings, images)
│   │   └── AndroidManifest.xml              # App permissions & components
│   ├── build.gradle.kts                     # App-level build configuration
│   └── proguard-rules.pro                   # ProGuard code obfuscation rules
├── build.gradle.kts                         # Root-level build configuration
├── gradle.properties                        # Gradle optimization settings
├── settings.gradle.kts                      # Project module configuration
├── .github/workflows/build.yml              # GitHub Actions CI/CD pipeline
├── .gitignore                               # Git exclusion rules
├── .env.example                             # Example environment variables
├── metadata.json                            # App metadata for AI Studio
└── README.md                                # Documentation
```

---

## 🚀 Key Features

### 1. **Multi-AI Integration**
- **Gemini API** - Advanced reasoning and vision capabilities
- **Groq API** - High-speed language model inference
- Intelligent personality system with customizable behaviors

### 2. **Automation Capabilities**
- Record complex action sequences as macros
- Replay with variable substitution
- Touch automation and gesture simulation
- Screen state analysis and adaptation

### 3. **Voice Interface**
- Real-time speech recognition (Google Speech API)
- Natural language command processing
- Text-to-speech responses with personality
- Voice activity detection

### 4. **Communication Hub**
- WhatsApp message automation
- MQTT real-time protocol for IoT integration
- Multi-channel message routing

### 5. **Data Persistence**
- Room Database with 3 primary tables:
  - `chat_history` - Conversation logs
  - `macro_routines` - Saved automation sequences
  - `whatsapp_replies` - Auto-response rules

---

## 🛠️ Tech Stack

### Android Framework
- **Kotlin** - Primary language
- **Jetpack Compose** - Modern declarative UI
- **Material Design 3** - Latest design system
- **MVVM Architecture** - Clean code patterns

### Data & Persistence
- **Room Database** - Local SQLite with type safety
- **Data Store** - Encrypted preferences (commented out, ready to use)
- **Retrofit** - Type-safe REST client
- **Moshi** - JSON serialization

### Real-time & Networking
- **MQTT (Paho)** - IoT communication protocol
- **OkHttp** - HTTP client with interceptors
- **Logging Interceptor** - Network debugging

### Testing
- **Robolectric** - Unit testing with Android framework
- **Roborazzi** - Compose UI screenshot testing
- **JUnit** - Test framework

### Build Tools
- **Gradle 9.7.1** - Build system
- **KSP** - Kotlin Symbol Processor for annotations
- **ProGuard** - Code obfuscation for release builds

---

## 📋 System Requirements

- **Android API Level**: 24 (Android 7.0+)
- **Target API Level**: 36 (Latest)
- **Kotlin**: 1.9.0+
- **Java**: 11

---

## 🔧 Build & Deployment

### Local Development
```bash
# Clone repository
git clone https://github.com/theflaxyplays-dotcom/lumina-online-.git
cd lumina-online-

# Build debug APK
gradle assembleDebug

# Run tests
gradle test

# Generate release APK (requires signing keys)
gradle assembleRelease
```

### CI/CD Pipeline
- **GitHub Actions** - Automated builds on push/PR
- **Gradle Caching** - Faster build times
- **APK Artifacts** - Auto-uploaded after successful build
- **Validation Checks** - Lint and build configuration validation

---

## 🔑 Configuration

### Environment Variables (.env)
```env
GEMINI_API_KEY=your_gemini_api_key_here
GROQ_API_KEY=your_groq_api_key_here (if using Groq)
```

### Release Signing (CI/CD)
Set GitHub Secrets:
- `KEYSTORE_PATH` - Path to keystore file
- `STORE_PASSWORD` - Keystore password
- `KEY_PASSWORD` - Key password

---

## 📊 Project Quality Metrics

| Metric | Value |
|--------|-------|
| Build Status | ✅ Passing |
| Code Language | Kotlin (100%) |
| Min SDK | 24 |
| Target SDK | 36 |
| Minification | Enabled (Release) |
| Test Coverage | Robolectric + Roborazzi |
| Code Obfuscation | ProGuard Rules |

---

## 🎨 UI Architecture

All screens follow **Jetpack Compose** patterns:
- **State Management** - ViewModels with StateFlow
- **Reusable Components** - Modular Composable functions
- **Theme System** - Centralized Material Design 3 colors
- **Navigation** - Tab-based bottom navigation

---

## 📱 Available Screens

1. **Orb Core** - Main personality hub with quick actions
2. **Console** - Real-time chat with AI assistant
3. **OS Control** - Screen automation and gesture control
4. **Macros** - Record/edit/execute automation sequences
5. **DirectReply** - WhatsApp message automation
6. **Co-Caster** - Gaming screen analysis
7. **Guardian** - Security & permission management
8. **Swarm** - Multi-agent deployment and control
9. **Setup Hub** - App configuration and settings

---

## 🔐 Security & Privacy

- **Manifest Permissions** - Comprehensive Android permissions declared
- **Sensitive Data** - Encrypted via Android KeyStore
- **Network Security** - HTTPS enforcement via Network Security Config
- **Code Obfuscation** - ProGuard rules applied to release builds
- **.env Files** - Secrets never committed to repository

---

## 📝 Future Enhancements

- [ ] Firebase Firestore cloud sync
- [ ] Multi-device synchronization
- [ ] Advanced ML model integration
- [ ] Widget support for quick actions
- [ ] Accessibility improvements
- [ ] Broader language support

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit changes: `git commit -am 'Add your feature'`
4. Push to branch: `git push origin feature/your-feature`
5. Submit a Pull Request

---

## 📄 License

This project does not currently have a license. See LICENSE file for details.

---

## 👤 Author

**theflaxyplays-dotcom**
- GitHub: [@theflaxyplays-dotcom](https://github.com/theflaxyplays-dotcom)

---

## 🙏 Acknowledgments

- Google Gemini API for advanced AI capabilities
- Groq for high-speed inference
- Jetpack Compose team for modern UI framework
- Android community for comprehensive libraries

---

**Last Updated**: August 29, 2026  
**Build Version**: 1.0  
**Status**: 🚀 Production Ready
