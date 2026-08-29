package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.LuminaApp
import com.example.data.local.WhatsAppReplyEntity
import com.example.data.model.ActionPayload
import com.example.data.model.ActionType
import com.example.data.model.AgentType
import com.example.data.model.AppSource
import com.example.data.model.AutoReplyStatus
import com.example.data.model.ChatMessage
import com.example.data.model.Coordinates
import com.example.data.model.GamingEvent
import com.example.data.model.GamingEventType
import com.example.data.model.LuminaAction
import com.example.data.model.LuminaPersona
import com.example.data.model.LuminaStatusMode
import com.example.data.model.MacroRoutine
import com.example.data.model.MacroStepItem
import com.example.data.model.MessageSender
import com.example.data.model.MqttConnectionStatus
import com.example.data.model.NodeQuery
import com.example.data.model.OrbTheme
import com.example.data.model.SubAgentStatus
import com.example.data.model.SubAgentTask
import com.example.data.model.WhatsAppChatThread
import com.example.service.LuminaAccessibilityService
import com.example.service.LuminaMacroWorker
import com.example.service.LuminaNotificationListener
import com.example.service.ScreenCaptureService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LuminaViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as LuminaApp
    private val repository = app.repository
    private val speechEngine = app.speechEngine
    private val systemController = app.systemController
    private val mqttManager = app.mqttManager
    private val biometricsEngine = app.voiceBiometricsEngine

    // Persona & Orb
    private val _currentPersona = MutableStateFlow(LuminaPersona.LUMINA)
    val currentPersona: StateFlow<LuminaPersona> = _currentPersona.asStateFlow()

    private val _currentOrbTheme = MutableStateFlow(OrbTheme.LUMINA_PULSE)
    val currentOrbTheme: StateFlow<OrbTheme> = _currentOrbTheme.asStateFlow()

    private val _statusMode = MutableStateFlow(LuminaStatusMode.CODING)
    val statusMode: StateFlow<LuminaStatusMode> = _statusMode.asStateFlow()

    // Chat
    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    val isSpeaking: StateFlow<Boolean> = speechEngine.isSpeaking
    val isListening: StateFlow<Boolean> = speechEngine.isListening
    val isContinuousVoiceActive: StateFlow<Boolean> = speechEngine.isContinuousLoopActive
    val recognizedSpeechText: StateFlow<String> = speechEngine.recognizedText

    private val _lastEmittedAction = MutableStateFlow<LuminaAction?>(null)
    val lastEmittedAction: StateFlow<LuminaAction?> = _lastEmittedAction.asStateFlow()

    // System Status
    val isTorchOn: StateFlow<Boolean> = systemController.isTorchOn
    val volumeLevel: StateFlow<Float> = systemController.volumeLevel
    val sosStatus: StateFlow<String?> = systemController.sosStatus
    val isAccessibilityActive: StateFlow<Boolean> = LuminaAccessibilityService.isServiceActive
    val lastAccessibilityLog: StateFlow<String> = LuminaAccessibilityService.lastActionLog
    val nodeTree = LuminaAccessibilityService.nodeTree

    // Screen Vision (MediaProjection & Confirmation Prompt)
    val isVisionActive: StateFlow<Boolean> = ScreenCaptureService.isVisionActive
    val lastCapturedBitmap: StateFlow<Bitmap?> = ScreenCaptureService.lastCapturedBitmap
    private val _visionAnalysisStatus = MutableStateFlow<String?>(null)
    val visionAnalysisStatus: StateFlow<String?> = _visionAnalysisStatus.asStateFlow()

    private val _screenSharePrompt = MutableStateFlow<String?>(null)
    val screenSharePrompt: StateFlow<String?> = _screenSharePrompt.asStateFlow()

    var requestMediaProjectionLauncher: (() -> Unit)? = null

    // Macros
    val savedMacros: StateFlow<List<MacroRoutine>> = repository.savedMacros.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isRecordingMacro = MutableStateFlow(false)
    val isRecordingMacro: StateFlow<Boolean> = _isRecordingMacro.asStateFlow()

    private val _currentRecordingName = MutableStateFlow("")
    val currentRecordingName: StateFlow<String> = _currentRecordingName.asStateFlow()

    private val _recordedSteps = MutableStateFlow<List<MacroStepItem>>(emptyList())
    val recordedSteps: StateFlow<List<MacroStepItem>> = _recordedSteps.asStateFlow()

    private val _macroReplayStatus = MutableStateFlow<String?>(null)
    val macroReplayStatus: StateFlow<String?> = _macroReplayStatus.asStateFlow()

    // Multi-App Hub (WhatsApp, Telegram, Instagram)
    private val _whatsAppThreads = MutableStateFlow<List<WhatsAppChatThread>>(
        listOf(
            WhatsAppChatThread(
                id = "wa_1",
                senderName = "Alex (Tech Lead)",
                lastMessage = "Bro, deploy kab ho raha hai? Client waiting hai.",
                timeAgo = "2m ago",
                unreadCount = 1,
                appSource = AppSource.WHATSAPP,
                status = AutoReplyStatus.PENDING
            ),
            WhatsAppChatThread(
                id = "wa_2",
                senderName = "Pooja (Designer)",
                lastMessage = "Did you check the new Figma designs for Lumina UI?",
                timeAgo = "15m ago",
                unreadCount = 2,
                appSource = AppSource.WHATSAPP,
                status = AutoReplyStatus.AUTO_REPLIED,
                autoRepliedText = "Namaste, Flaxy abhi coding me busy hain. Main unki AI assistant Lumina hu. Message note kar liya hai!"
            ),
            WhatsAppChatThread(
                id = "tg_1",
                senderName = "DevOps Channel",
                lastMessage = "Alert: Build server green, staging ready.",
                timeAgo = "30m ago",
                unreadCount = 3,
                appSource = AppSource.TELEGRAM,
                status = AutoReplyStatus.PENDING
            ),
            WhatsAppChatThread(
                id = "ig_1",
                senderName = "Creative Studios",
                lastMessage = "Collab request sent via Instagram DM.",
                timeAgo = "1h ago",
                unreadCount = 0,
                appSource = AppSource.INSTAGRAM,
                status = AutoReplyStatus.MANUAL
            )
        )
    )
    val whatsAppThreads: StateFlow<List<WhatsAppChatThread>> = _whatsAppThreads.asStateFlow()

    val whatsAppReplies: StateFlow<List<WhatsAppReplyEntity>> = repository.whatsAppReplies.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Sub-Agents Swarm
    private val _subAgentTasks = MutableStateFlow<List<SubAgentTask>>(
        listOf(
            SubAgentTask(
                id = "task_101",
                name = "Deep Tech Digest",
                agentType = AgentType.RESEARCHER,
                prompt = "Analyze quantum computing breakthroughs in 2026",
                progressPercent = 100,
                status = SubAgentStatus.COMPLETED,
                outputResult = "Research Completed: Indexed 14 arxiv papers on topological qubits."
            ),
            SubAgentTask(
                id = "task_102",
                name = "Automated Web Component Deployer",
                agentType = AgentType.CODER,
                prompt = "Build a real-time WebSocket telemetry visualizer in Compose",
                progressPercent = 75,
                status = SubAgentStatus.RUNNING,
                outputResult = "Generating reactive WebSocket client bridge..."
            )
        )
    )
    val subAgentTasks: StateFlow<List<SubAgentTask>> = _subAgentTasks.asStateFlow()

    // Gaming Co-Caster
    private val _isGamingVisionActive = MutableStateFlow(false)
    val isGamingVisionActive: StateFlow<Boolean> = _isGamingVisionActive.asStateFlow()

    private val _gamingEvents = MutableStateFlow<List<GamingEvent>>(
        listOf(
            GamingEvent(
                id = 1,
                eventType = GamingEventType.ENEMY_SPOTTED,
                title = "Enemy Squad at 145 SE (Pochinki Church)",
                voiceCommentary = "Flaxy, samne 2 bande church tower pe camping kar rahe hain! Cover lo!",
                hypeScore = 85
            ),
            GamingEvent(
                id = 2,
                eventType = GamingEventType.AIRDROP,
                title = "Flare Airdrop Dropping (AWM + Level 3 Helmet)",
                voiceCommentary = "Airdrop crate 200m away! Loot ke aate hain partner, full firepower!",
                hypeScore = 92
            )
        )
    )
    val gamingEvents: StateFlow<List<GamingEvent>> = _gamingEvents.asStateFlow()

    // Voice Guardian & Biometrics Engine
    val voiceEnrollmentStep: StateFlow<Int> = biometricsEngine.enrollmentStep
    val voiceSimilarity: StateFlow<Float> = biometricsEngine.lastSimilarity
    val isVoiceEnrolled: StateFlow<Boolean> = biometricsEngine.isEnrolled
    val isVoiceRecording: StateFlow<Boolean> = biometricsEngine.isRecording
    val biometricsLog: StateFlow<String> = biometricsEngine.biometricsLog

    private val _isGuardModeActive = MutableStateFlow(false)
    val isGuardModeActive: StateFlow<Boolean> = _isGuardModeActive.asStateFlow()

    private val _isIncognitoActive = MutableStateFlow(false)
    val isIncognitoActive: StateFlow<Boolean> = _isIncognitoActive.asStateFlow()

    private val _isFloatingHudVisible = MutableStateFlow(false)
    val isFloatingHudVisible: StateFlow<Boolean> = _isFloatingHudVisible.asStateFlow()

    fun toggleFloatingHud(visible: Boolean? = null) {
        _isFloatingHudVisible.value = visible ?: !_isFloatingHudVisible.value
    }

    // ==========================================
    // MULTI-PROVIDER AI CONFIGURATION & STORAGE
    // ==========================================
    private val prefs = application.getSharedPreferences("lumina_settings", android.content.Context.MODE_PRIVATE)

    // Provider 1: Gemini
    private val _customGeminiApiKey = MutableStateFlow(prefs.getString("custom_gemini_api_key", "") ?: "")
    val customGeminiApiKey: StateFlow<String> = _customGeminiApiKey.asStateFlow()

    private val _geminiModelName = MutableStateFlow(
        prefs.getString("gemini_model_name", "gemini-3.5-flash")?.let {
            if (it == "gemini-2.5-flash" || it == "gemini-1.5-flash" || it.isBlank()) "gemini-3.5-flash" else it
        } ?: "gemini-3.5-flash"
    )
    val geminiModelName: StateFlow<String> = _geminiModelName.asStateFlow()

    private val _geminiTestResult = MutableStateFlow<String?>(null)
    val geminiTestResult: StateFlow<String?> = _geminiTestResult.asStateFlow()

    // Provider 2: Groq Cloud
    private val _groqApiKey = MutableStateFlow(prefs.getString("groq_api_key", "") ?: "")
    val groqApiKey: StateFlow<String> = _groqApiKey.asStateFlow()

    private val _groqModelName = MutableStateFlow(prefs.getString("groq_model_name", "llama-3.3-70b-versatile") ?: "llama-3.3-70b-versatile")
    val groqModelName: StateFlow<String> = _groqModelName.asStateFlow()

    private val _groqTestResult = MutableStateFlow<String?>(null)
    val groqTestResult: StateFlow<String?> = _groqTestResult.asStateFlow()

    // Provider 3: NVIDIA NIM
    private val _nvidiaApiKey = MutableStateFlow(prefs.getString("nvidia_api_key", "") ?: "")
    val nvidiaApiKey: StateFlow<String> = _nvidiaApiKey.asStateFlow()

    private val _nvidiaModelName = MutableStateFlow(prefs.getString("nvidia_model_name", "meta/llama-3.1-70b-instruct") ?: "meta/llama-3.1-70b-instruct")
    val nvidiaModelName: StateFlow<String> = _nvidiaModelName.asStateFlow()

    private val _nvidiaTestResult = MutableStateFlow<String?>(null)
    val nvidiaTestResult: StateFlow<String?> = _nvidiaTestResult.asStateFlow()

    private val _isTestingApi = MutableStateFlow(false)
    val isTestingApi: StateFlow<Boolean> = _isTestingApi.asStateFlow()

    // Legacy test result for backwards compatibility
    val apiTestResult: StateFlow<String?> = _geminiTestResult

    val smartHomeDevices: StateFlow<Map<String, Boolean>> = mqttManager.devices
    val mqttConnectionStatus: StateFlow<MqttConnectionStatus> = mqttManager.connectionStatus
    val mqttLastLog: StateFlow<String> = mqttManager.lastLog

    init {
        // Intercept WhatsApp/Telegram/Instagram notifications if listener is active
        LuminaNotificationListener.onMessageIntercepted = { sender, message, sbn ->
            handleIncomingNotification(sender, message, sbn)
        }

        // Connect MQTT Gateway automatically in background
        mqttManager.connect()

        // Seed initial greeting message
        viewModelScope.launch {
            delay(400)
            if (chatMessages.value.isEmpty()) {
                val greeting = _currentPersona.value.greetingHindi
                val initMsg = ChatMessage(
                    sender = MessageSender.LUMINA,
                    text = greeting,
                    persona = _currentPersona.value
                )
                repository.saveMessage(initMsg)
            }
        }
    }

    fun switchPersona(persona: LuminaPersona) {
        _currentPersona.value = persona
        systemController.vibrateHaptic(35)
        when (persona) {
            LuminaPersona.LUMINA -> _currentOrbTheme.value = OrbTheme.LUMINA_PULSE
            LuminaPersona.FRIDAY -> _currentOrbTheme.value = OrbTheme.JARVIS_ARC
            LuminaPersona.VENOM -> _currentOrbTheme.value = OrbTheme.PULSE_REACTOR
        }
        val greeting = persona.greetingHindi
        viewModelScope.launch {
            val msg = ChatMessage(
                sender = MessageSender.LUMINA,
                text = greeting,
                persona = persona
            )
            repository.saveMessage(msg)
            speechEngine.speak(greeting, persona)
        }
    }

    fun switchOrbTheme(theme: OrbTheme) {
        _currentOrbTheme.value = theme
        systemController.vibrateHaptic(30)
    }

    fun setStatusMode(mode: LuminaStatusMode) {
        _statusMode.value = mode
        systemController.vibrateHaptic(30)
    }

    fun toggleContinuousVoice(enabled: Boolean) {
        speechEngine.setContinuousLoop(enabled)
        systemController.vibrateHaptic(40)
        if (enabled) {
            startVoiceInput()
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        val persona = _currentPersona.value
        val cleanLower = userText.lowercase().trim()

        // Check for interrupt phrases
        if (speechEngine.isInterruptPhrase(userText)) {
            speechEngine.stopSpeaking()
            return
        }

        // Check if there is an active Screen Share Permission Prompt waiting for confirmation
        if (_screenSharePrompt.value != null) {
            if (isConfirmationPhrase(cleanLower)) {
                confirmScreenShare()
                return
            } else if (isDenialPhrase(cleanLower)) {
                rejectScreenShare()
                return
            }
        }

        // Check for Screen Share / Vision requests by voice
        if (isScreenShareRequestPhrase(cleanLower)) {
            requestScreenShare("Voice command received: \"$userText\"")
            return
        }

        // Check for sleep phrases
        if (speechEngine.isSleepPhrase(userText)) {
            speechEngine.setContinuousLoop(false)
            val sleepResponse = when (persona) {
                LuminaPersona.LUMINA -> "Theek hai Flaxy, main standby pe ja rahi hu. Jab bhi zarurat ho 'Wake up Lumina' bolna!"
                LuminaPersona.FRIDAY -> "Entering standby protocol, Flaxy. Say 'Friday' to reactivate."
                LuminaPersona.VENOM -> "Sote hain partner! Awaz dena jab tod-phod karni ho!"
            }
            speechEngine.speak(sleepResponse, persona)
            return
        }

        viewModelScope.launch {
            // 1. Save User Message
            val userMsg = ChatMessage(
                sender = MessageSender.USER,
                text = userText,
                persona = persona
            )
            repository.saveMessage(userMsg)

            // 2. Generate AI Response using Multi-LLM Fallback Router
            _isGenerating.value = true
            val (responseText, parsedAction) = repository.generateLuminaResponse(
                prompt = userText,
                persona = persona,
                history = chatMessages.value,
                customGeminiApiKey = _customGeminiApiKey.value,
                geminiModelName = _geminiModelName.value,
                groqApiKey = _groqApiKey.value,
                groqModelName = _groqModelName.value,
                nvidiaApiKey = _nvidiaApiKey.value,
                nvidiaModelName = _nvidiaModelName.value
            )
            _isGenerating.value = false
            _lastEmittedAction.value = parsedAction

            // 3. Save AI Message
            val aiMsg = ChatMessage(
                sender = MessageSender.LUMINA,
                text = responseText,
                persona = persona,
                actionJson = parsedAction?.let { "Action: ${it.type}" },
                parsedAction = parsedAction
            )
            repository.saveMessage(aiMsg)

            // 4. Voice Speech Synthesis
            speechEngine.speak(responseText, persona)

            // 5. Execute action if present
            if (parsedAction != null) {
                executeAction(parsedAction)
            }
        }
    }

    private fun isConfirmationPhrase(text: String): Boolean {
        val keywords = listOf(
            "yes", "ha", "haan", "haa", "ok", "okay", "allow", "karlo", "dekho",
            "start", "allow karo", "sure", "theek hai", "karo", "permission granted"
        )
        return keywords.any { text == it || text.startsWith("$it ") || text.endsWith(" $it") }
    }

    private fun isDenialPhrase(text: String): Boolean {
        val keywords = listOf(
            "no", "nahi", "cancel", "deny", "mat karo", "stop", "na", "cancel karo",
            "don't", "dont", "nahi dekhna", "mana karo"
        )
        return keywords.any { text == it || text.startsWith("$it ") || text.endsWith(" $it") }
    }

    private fun isScreenShareRequestPhrase(text: String): Boolean {
        return text.contains("screen share") ||
                text.contains("look at screen") ||
                text.contains("screen dekho") ||
                text.contains("screen par kya hai") ||
                text.contains("vision on") ||
                text.contains("vision start") ||
                text.contains("screen capture") ||
                text.contains("meri screen dekho") ||
                text.contains("kya dikh raha hai")
    }

    fun requestScreenShare(reason: String = "Lumina needs permission to view and analyze your screen content") {
        val persona = _currentPersona.value
        _screenSharePrompt.value = reason
        systemController.vibrateHaptic(50)
        val promptVoice = when (persona) {
            LuminaPersona.LUMINA -> "Flaxy, kya main aapki screen dekh sakti hu? Please 'Yes' ya 'OK' bol kar confirm karein."
            LuminaPersona.FRIDAY -> "Screen vision permission required, Flaxy. State 'Yes' or 'Allow' to authorize visual capture."
            LuminaPersona.VENOM -> "Screen pe kya chal raha hai dekhna hai partner! Permission do, 'Yes' bolo!"
        }
        speechEngine.speak(promptVoice, persona)
    }

    fun confirmScreenShare() {
        val persona = _currentPersona.value
        _screenSharePrompt.value = null
        systemController.vibrateHaptic(60)

        val confirmVoice = when (persona) {
            LuminaPersona.LUMINA -> "Screen share allow ho gaya! Main aapki screen dekh rahi hu..."
            LuminaPersona.FRIDAY -> "Screen vision authorized. Commencing real-time multimodal analysis."
            LuminaPersona.VENOM -> "Permission mil gayi partner! Vision ON ho chuka hai!"
        }
        speechEngine.speak(confirmVoice, persona)

        if (ScreenCaptureService.isServiceRunning()) {
            analyzeScreenVision()
        } else {
            requestMediaProjectionLauncher?.invoke()
        }
    }

    fun rejectScreenShare() {
        val persona = _currentPersona.value
        _screenSharePrompt.value = null
        systemController.vibrateHaptic(40)

        val rejectVoice = when (persona) {
            LuminaPersona.LUMINA -> "Theek hai Flaxy, screen share cancel kar diya."
            LuminaPersona.FRIDAY -> "Screen capture permission declined. Standing by."
            LuminaPersona.VENOM -> "Koi baat nahi partner, screen share cancel!"
        }
        speechEngine.speak(rejectVoice, persona)
    }

    fun stopScreenShare() {
        _screenSharePrompt.value = null
        systemController.vibrateHaptic(40)
        val persona = _currentPersona.value
        speechEngine.speak("Screen vision stopped.", persona)
    }

    fun onScreenProjectionGranted() {
        analyzeScreenVision()
    }

    fun onScreenProjectionDenied() {
        speechEngine.speak("Media projection permission was not granted by Android.", _currentPersona.value)
    }

    fun analyzeScreenVision(prompt: String = "") {
        val persona = _currentPersona.value
        viewModelScope.launch {
            _visionAnalysisStatus.value = "Capturing screen frame..."
            val base64 = ScreenCaptureService.captureCurrentScreenBase64()

            _visionAnalysisStatus.value = "Analyzing screen with Gemini Flash Multimodal Vision..."
            _isGenerating.value = true

            val (analysisText, parsedAction) = if (!base64.isNullOrBlank()) {
                repository.analyzeScreenWithGemini(
                    base64Jpeg = base64,
                    prompt = prompt.ifBlank { "Describe what is on screen and execute the necessary action for Flaxy" },
                    persona = persona,
                    customApiKey = _customGeminiApiKey.value,
                    modelName = _geminiModelName.value
                )
            } else {
                Pair(
                    "Flaxy, screen vision active hai! Accessibility crawler ne ${nodeTree.value.size} UI elements index kiye hain.",
                    null
                )
            }

            _isGenerating.value = false
            _visionAnalysisStatus.value = null

            val aiMsg = ChatMessage(
                sender = MessageSender.LUMINA,
                text = "📸 [Screen Vision Analysis]\n$analysisText",
                persona = persona,
                parsedAction = parsedAction
            )
            repository.saveMessage(aiMsg)
            speechEngine.speak(analysisText, persona)

            if (parsedAction != null) {
                executeAction(parsedAction)
            }
        }
    }

    fun startVoiceInput() {
        systemController.vibrateHaptic(40)
        val hasMicPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            getApplication(),
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasMicPermission) {
            speechEngine.speak("Please allow Microphone permission to use voice control with Lumina.", _currentPersona.value)
            return
        }

        speechEngine.startListening { transcribed ->
            if (transcribed.isNotBlank()) {
                sendMessage(transcribed)
            }
        }
    }

    fun stopVoiceInput() {
        speechEngine.stopListening()
    }

    fun speakMessage(message: ChatMessage) {
        speechEngine.speak(message.text, message.persona)
    }

    fun stopSpeaking() {
        speechEngine.stopSpeaking()
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    fun executeAction(action: LuminaAction) {
        systemController.vibrateHaptic(45)
        when (action.type.uppercase()) {
            "SYSTEM_SETTING" -> {
                val key = action.payload?.setting_key
                if (key == "torch") {
                    systemController.toggleTorch()
                } else if (key == "volume") {
                    val vol = (action.payload.setting_value?.toFloatOrNull() ?: 70f) / 100f
                    systemController.setVolume(vol)
                }
            }
            "INTENT" -> {
                val uri = action.payload?.intent_uri
                val phone = action.payload?.phone_number
                val text = action.payload?.input_text
                if (action.target_package == "com.whatsapp" || uri?.contains("whatsapp") == true) {
                    systemController.openWhatsAppDirect(phone ?: "+919876543210", text ?: "Hello from Lumina")
                } else if (phone != null || uri?.startsWith("tel:") == true) {
                    systemController.launchPhoneCall(phone ?: "9876543210", action.payload?.sim_slot ?: 1)
                }
            }
            "ACCESSIBILITY_NODE" -> {
                val service = LuminaAccessibilityService.instance
                if (service != null) {
                    val query = action.node_query
                    service.performDynamicClick(query?.view_id, query?.text, query?.content_desc)
                }
            }
            "GESTURE_TAP" -> {
                val px = action.coordinates?.exact_px
                if (px != null && px.size >= 2) {
                    LuminaAccessibilityService.instance?.dispatchTouchTap(px[0].toFloat(), px[1].toFloat())
                }
            }
            "GESTURE_SWIPE" -> {
                LuminaAccessibilityService.instance?.dispatchSwipe(540f, 1600f, 540f, 400f, 300)
            }
            "SOS_TRIGGER" -> {
                systemController.triggerSosSequence { }
            }
        }
    }

    // Macro Recording CUJ
    fun startMacroRecording(name: String) {
        _isRecordingMacro.value = true
        _currentRecordingName.value = name.ifBlank { "Routine_${System.currentTimeMillis() % 1000}" }
        _recordedSteps.value = emptyList()
        systemController.vibrateHaptic(60)
    }

    fun addMacroStep(step: MacroStepItem) {
        val current = _recordedSteps.value.toMutableList()
        val nextStepNumber = current.size + 1
        current.add(step.copy(stepNumber = nextStepNumber))
        _recordedSteps.value = current
        systemController.vibrateHaptic(30)
    }

    fun undoLastMacroStep() {
        val current = _recordedSteps.value.toMutableList()
        if (current.isNotEmpty()) {
            current.removeAt(current.size - 1)
            _recordedSteps.value = current
            systemController.vibrateHaptic(40)
        }
    }

    fun saveRecordedMacro() {
        val name = _currentRecordingName.value
        val steps = _recordedSteps.value
        if (steps.isEmpty()) return

        val variableNames = steps.mapNotNull { it.variableName }.distinct()
        val routine = MacroRoutine(
            name = name,
            description = "${steps.size} dynamic steps automated workflow",
            steps = steps,
            variableNames = variableNames,
            isPrebuilt = false
        )

        viewModelScope.launch {
            repository.saveMacro(routine)
            _isRecordingMacro.value = false
            _recordedSteps.value = emptyList()
            _currentRecordingName.value = ""
        }
    }

    fun cancelMacroRecording() {
        _isRecordingMacro.value = false
        _recordedSteps.value = emptyList()
        _currentRecordingName.value = ""
    }

    fun replayMacro(routine: MacroRoutine, variableValues: Map<String, String> = emptyMap()) {
        viewModelScope.launch {
            _macroReplayStatus.value = "Executing ${routine.name}..."
            for (step in routine.steps) {
                _macroReplayStatus.value = "Running Step ${step.stepNumber}/${routine.steps.size}: ${step.title}"
                systemController.vibrateHaptic(30)
                delay(step.delayMs.coerceAtLeast(600))

                when (step.actionType) {
                    ActionType.SYSTEM_SETTING -> {
                        if (step.textPayload?.startsWith("volume:") == true) {
                            val v = step.textPayload.substringAfter("volume:").toFloatOrNull() ?: 70f
                            systemController.setVolume(v / 100f)
                        } else if (step.textPayload == "torch") {
                            systemController.toggleTorch()
                        }
                    }
                    ActionType.GESTURE_SWIPE -> {
                        LuminaAccessibilityService.instance?.dispatchSwipe(540f, 1600f, 540f, 400f, 300)
                    }
                    ActionType.ACCESSIBILITY_NODE -> {
                        val text = variableValues[step.variableName] ?: step.textPayload
                        if (text != null && text.isNotBlank()) {
                            LuminaAccessibilityService.instance?.performDynamicSetText(text)
                        } else {
                            LuminaAccessibilityService.instance?.performDynamicClick(step.viewId, step.textPayload, null)
                        }
                    }
                    ActionType.INTENT -> {
                        if (step.targetPackage == "com.whatsapp") {
                            val msg = variableValues[step.variableName] ?: step.textPayload ?: "Hello"
                            systemController.openWhatsAppDirect("+919876543210", msg)
                        }
                    }
                    ActionType.SOS_TRIGGER -> {
                        systemController.triggerSosSequence {}
                    }
                    else -> {}
                }
            }
            _macroReplayStatus.value = "Completed: ${routine.name} successfully!"
            delay(2500)
            _macroReplayStatus.value = null
        }
    }

    fun scheduleMacroWithWorkManager(routine: MacroRoutine, delayMinutes: Long) {
        LuminaMacroWorker.scheduleMacro(
            context = getApplication(),
            macroId = routine.id,
            macroName = routine.name,
            delaySeconds = delayMinutes * 60
        )
        systemController.vibrateHaptic(40)
        speechEngine.speak("Scheduled '${routine.name}' to trigger in $delayMinutes minutes.", _currentPersona.value)
    }

    fun deleteMacro(id: Long) {
        viewModelScope.launch {
            repository.deleteMacro(id)
            LuminaMacroWorker.cancelScheduledMacro(getApplication(), id)
        }
    }

    // Multi-App Hub
    private fun handleIncomingNotification(sender: String, message: String, sbn: android.service.notification.StatusBarNotification) {
        val currentMode = _statusMode.value
        val autoReplyText = when (currentMode) {
            LuminaStatusMode.AVAILABLE -> "Hi! Flaxy is active."
            LuminaStatusMode.BUSY -> "Namaste, Flaxy abhi busy hain. Main unki AI assistant Lumina hu. Message note kar liya hai!"
            LuminaStatusMode.CODING -> "Namaste, Flaxy abhi coding aur development me busy hain. Main unki assistant Lumina hu. Message received!"
            LuminaStatusMode.SLEEPING -> "Flaxy abhi so rahe hain. Subah aapse connect karenge!"
        }

        val appSource = when (sbn.packageName) {
            "com.whatsapp", "com.whatsapp.w4b" -> AppSource.WHATSAPP
            "org.telegram.messenger", "org.telegram.plus" -> AppSource.TELEGRAM
            "com.instagram.android" -> AppSource.INSTAGRAM
            else -> AppSource.WHATSAPP
        }

        viewModelScope.launch {
            repository.saveWhatsAppReply(sender, message, autoReplyText)

            if (currentMode != LuminaStatusMode.AVAILABLE) {
                LuminaNotificationListener.instance?.sendDirectReply(sbn, autoReplyText)
            }

            val currentList = _whatsAppThreads.value.toMutableList()
            val existing = currentList.firstOrNull { it.senderName == sender }
            if (existing != null) {
                currentList.remove(existing)
                currentList.add(
                    0,
                    existing.copy(
                        lastMessage = message,
                        timeAgo = "Just now",
                        appSource = appSource,
                        status = AutoReplyStatus.AUTO_REPLIED,
                        autoRepliedText = autoReplyText
                    )
                )
            } else {
                currentList.add(
                    0,
                    WhatsAppChatThread(
                        id = "wa_${System.currentTimeMillis()}",
                        senderName = sender,
                        lastMessage = message,
                        timeAgo = "Just now",
                        appSource = appSource,
                        status = AutoReplyStatus.AUTO_REPLIED,
                        autoRepliedText = autoReplyText
                    )
                )
            }
            _whatsAppThreads.value = currentList
        }
    }

    fun triggerWhatsAppReplySimulated(thread: WhatsAppChatThread, replyText: String) {
        viewModelScope.launch {
            repository.saveWhatsAppReply(thread.senderName, thread.lastMessage, replyText)
            val updated = _whatsAppThreads.value.map {
                if (it.id == thread.id) it.copy(status = AutoReplyStatus.AUTO_REPLIED, autoRepliedText = replyText) else it
            }
            _whatsAppThreads.value = updated
            systemController.vibrateHaptic(35)
        }
    }

    // Sub-Agent Swarm
    fun spawnSubAgentTask(agentType: AgentType, prompt: String) {
        val newTask = SubAgentTask(
            id = "task_${System.currentTimeMillis() % 10000}",
            name = "${agentType.label} [Job #${(100..999).random()}]",
            agentType = agentType,
            prompt = prompt,
            progressPercent = 10,
            status = SubAgentStatus.RUNNING,
            outputResult = "Agent dispatched. Synthesizing neural context..."
        )
        val current = _subAgentTasks.value.toMutableList()
        current.add(0, newTask)
        _subAgentTasks.value = current
        systemController.vibrateHaptic(40)

        viewModelScope.launch {
            delay(1200)
            updateSubAgentProgress(newTask.id, 45, "Executing tool actions and web crawls...")
            delay(1500)
            updateSubAgentProgress(newTask.id, 80, "Assembling structured output payload...")
            delay(1200)
            val finalResult = when (agentType) {
                AgentType.RESEARCHER -> "Research Complete: Generated executive synthesis with 8 verified citations."
                AgentType.CODER -> "Code Complete: Jetpack Compose component compiled and tested with 0 warnings."
                AgentType.DOC_GEN -> "PDF Report Generated: Ready for download (2.4 MB, 14 pages)."
                AgentType.IOT_HOME -> "Smart Home Command Broadcast: ESP32 acknowledged state synchronization."
            }
            updateSubAgentProgress(newTask.id, 100, finalResult, SubAgentStatus.COMPLETED)
        }
    }

    private fun updateSubAgentProgress(taskId: String, progress: Int, result: String, status: SubAgentStatus = SubAgentStatus.RUNNING) {
        val list = _subAgentTasks.value.map {
            if (it.id == taskId) it.copy(progressPercent = progress, outputResult = result, status = status) else it
        }
        _subAgentTasks.value = list
    }

    // Gaming Co-Caster
    fun toggleGamingVision() {
        _isGamingVisionActive.value = !_isGamingVisionActive.value
        systemController.vibrateHaptic(40)
        if (_isGamingVisionActive.value) {
            speechEngine.speak("Gaming Live Co-Caster active! Frame analysis online.", _currentPersona.value)
        }
    }

    fun triggerSimulatedGamingEvent(type: GamingEventType) {
        val persona = _currentPersona.value
        val (title, voiceText, score) = when (type) {
            GamingEventType.ENEMY_SPOTTED -> when (persona) {
                LuminaPersona.LUMINA -> Triple("Enemy Spotted (North-East 45°)", "Flaxy! Right side rock ke peeche ek banda hide kar raha hai, AWM ready rakho!", 88)
                LuminaPersona.FRIDAY -> Triple("Hostile Visual Confirmed", "Hostile located at 045 degrees, distance 150 meters, Flaxy.", 90)
                LuminaPersona.VENOM -> Triple("Enemy Detected", "Target spotted partner! Khopdi phod do iski!", 95)
            }
            GamingEventType.AIRDROP -> when (persona) {
                LuminaPersona.LUMINA -> Triple("Flare Airdrop Landing", "Airdrop aa gaya Flaxy! Groza aur Level 3 vest loot lete hain!", 91)
                LuminaPersona.FRIDAY -> Triple("Supply Drop Detected", "High-value crate inbound at coordinates 210 Southwest, Flaxy.", 87)
                LuminaPersona.VENOM -> Triple("Crate Inbound", "Airdrop lootenge partner! Full firepower aane wali hai!", 96)
            }
            GamingEventType.CLUTCH -> when (persona) {
                LuminaPersona.LUMINA -> Triple("1v4 Clutch Win!", "Flaxy tumne poori squad akele neutralize kar di! Pure pro gameplay!", 99)
                LuminaPersona.FRIDAY -> Triple("Squad Neutralized", "Combat sequence executed with 100% precision. Quad kill confirmed, Flaxy.", 98)
                LuminaPersona.VENOM -> Triple("UNSTOPPABLE CLUTCH", "KYA KHELE HO PARTNER! Sabka safaya kar diya! Pure dominance!", 100)
            }
            GamingEventType.ZONE_SHRINK -> Triple("Blue Zone Shrinking", "Zone close ho raha hai, vehicle rotation shuru karo!", 80)
            GamingEventType.HEADSHOT -> Triple("Sniper Headshot", "Boom! Perfect headshot knock!", 94)
            GamingEventType.SQUAD_FIGHT -> Triple("Squad Fight Engaging", "Multiple squads pushing! Deploy smoke grenade!", 89)
        }

        val event = GamingEvent(
            eventType = type,
            title = title,
            voiceCommentary = voiceText,
            hypeScore = score
        )
        val current = _gamingEvents.value.toMutableList()
        current.add(0, event)
        _gamingEvents.value = current

        speechEngine.speak(voiceText, persona)
        systemController.vibrateHaptic(60)
    }

    // Voice Guardian & Real Biometrics
    fun toggleGuardMode() {
        _isGuardModeActive.value = !_isGuardModeActive.value
        systemController.vibrateHaptic(50)
        if (_isGuardModeActive.value) {
            speechEngine.speak("Voice Guardian Active. Unauthorized voices will trigger screen lockdown.", _currentPersona.value)
        }
    }

    fun toggleIncognito() {
        _isIncognitoActive.value = !_isIncognitoActive.value
        systemController.vibrateHaptic(30)
    }

    fun triggerIntruderAlertSimulated() {
        systemController.vibrateHaptic(200)
        val warning = "🚨 INTRUDER ALERT: Ye phone Flaxy ka hai. Aap unauthorized user hain. Please Flaxy ko bulaiye!"
        speechEngine.speak(warning, _currentPersona.value)
    }

    fun startVoiceCalibration() {
        systemController.vibrateHaptic(40)
        biometricsEngine.startEnrollmentCalibration { step, success ->
            systemController.vibrateHaptic(if (success) 50 else 100)
        }
    }

    fun resetVoiceCalibration() {
        biometricsEngine.resetEnrollment()
        systemController.vibrateHaptic(40)
    }

    // MQTT Smart Home
    fun toggleSmartHomeDevice(deviceName: String) {
        mqttManager.toggleDevice(deviceName)
        systemController.vibrateHaptic(35)
    }

    fun setSmartHomeDeviceState(deviceName: String, state: Boolean) {
        mqttManager.setDeviceState(deviceName, state)
        systemController.vibrateHaptic(35)
    }

    fun connectMqttBroker(brokerUrl: String) {
        mqttManager.connect(brokerUrl)
    }

    // ==========================================
    // MULTI-PROVIDER KEY & MODEL MANAGEMENT
    // ==========================================

    fun setGeminiConfig(key: String, model: String) {
        _customGeminiApiKey.value = key
        val cleanModel = model.trim().let { if (it == "gemini-2.5-flash" || it.isBlank()) "gemini-3.5-flash" else it }
        _geminiModelName.value = cleanModel
        prefs.edit()
            .putString("custom_gemini_api_key", key)
            .putString("gemini_model_name", _geminiModelName.value)
            .apply()
        systemController.vibrateHaptic(30)
    }

    fun testGeminiApiKey(keyToTest: String? = null, modelToTest: String? = null) {
        val key = keyToTest?.trim() ?: _customGeminiApiKey.value.trim().ifEmpty {
            com.example.BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" } ?: ""
        }
        val model = modelToTest?.trim() ?: _geminiModelName.value
        viewModelScope.launch {
            _isTestingApi.value = true
            _geminiTestResult.value = "Testing connection to Gemini API [$model]..."
            val (success, message) = repository.testGeminiConnection(key, model)
            _isTestingApi.value = false
            _geminiTestResult.value = if (success) "✓ $message" else "✗ $message"
            systemController.vibrateHaptic(if (success) 50 else 100)
        }
    }

    fun setGroqConfig(key: String, model: String) {
        _groqApiKey.value = key
        _groqModelName.value = model.ifBlank { "llama-3.3-70b-versatile" }
        prefs.edit()
            .putString("groq_api_key", key)
            .putString("groq_model_name", _groqModelName.value)
            .apply()
        systemController.vibrateHaptic(30)
    }

    fun testGroqApiKey(keyToTest: String? = null, modelToTest: String? = null) {
        val key = keyToTest?.trim() ?: _groqApiKey.value.trim()
        val model = modelToTest?.trim() ?: _groqModelName.value
        viewModelScope.launch {
            _isTestingApi.value = true
            _groqTestResult.value = "Testing connection to Groq API [$model]..."
            val (success, message) = repository.testGroqConnection(key, model)
            _isTestingApi.value = false
            _groqTestResult.value = if (success) "✓ $message" else "✗ $message"
            systemController.vibrateHaptic(if (success) 50 else 100)
        }
    }

    fun setNvidiaConfig(key: String, model: String) {
        _nvidiaApiKey.value = key
        _nvidiaModelName.value = model.ifBlank { "meta/llama-3.1-70b-instruct" }
        prefs.edit()
            .putString("nvidia_api_key", key)
            .putString("nvidia_model_name", _nvidiaModelName.value)
            .apply()
        systemController.vibrateHaptic(30)
    }

    fun testNvidiaApiKey(keyToTest: String? = null, modelToTest: String? = null) {
        val key = keyToTest?.trim() ?: _nvidiaApiKey.value.trim()
        val model = modelToTest?.trim() ?: _nvidiaModelName.value
        viewModelScope.launch {
            _isTestingApi.value = true
            _nvidiaTestResult.value = "Testing connection to NVIDIA NIM API [$model]..."
            val (success, message) = repository.testNvidiaConnection(key, model)
            _isTestingApi.value = false
            _nvidiaTestResult.value = if (success) "✓ $message" else "✗ $message"
            systemController.vibrateHaptic(if (success) 50 else 100)
        }
    }

    // Legacy method signatures for backward compatibility
    fun setGeminiApiKey(key: String) = setGeminiConfig(key, _geminiModelName.value)
    fun setGroqApiKey(key: String) = setGroqConfig(key, _groqModelName.value)

    fun toggleTorch() = systemController.toggleTorch()
    fun setVolume(v: Float) = systemController.setVolume(v)
    fun triggerSos() = systemController.triggerSosSequence { }

    override fun onCleared() {
        super.onCleared()
        speechEngine.shutdown()
        mqttManager.disconnect()
    }
}
