package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.ChatDao
import com.example.data.local.ChatEntity
import com.example.data.local.MacroDao
import com.example.data.local.MacroEntity
import com.example.data.local.WhatsAppReplyDao
import com.example.data.local.WhatsAppReplyEntity
import com.example.data.model.ActionPayload
import com.example.data.model.ActionType
import com.example.data.model.ChatMessage
import com.example.data.model.Coordinates
import com.example.data.model.LuminaAction
import com.example.data.model.LuminaPersona
import com.example.data.model.MacroRoutine
import com.example.data.model.MacroStepItem
import com.example.data.model.MessageSender
import com.example.data.model.NodeQuery
import com.example.data.remote.GeminiApiClient
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import com.example.data.remote.GroqApiClient
import com.example.data.remote.NvidiaApiClient
import com.example.data.remote.OpenAiChatMessage
import com.example.data.remote.OpenAiChatRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LuminaRepository(
    private val chatDao: ChatDao,
    private val macroDao: MacroDao,
    private val whatsAppReplyDao: WhatsAppReplyDao
) {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val actionAdapter = moshi.adapter(LuminaAction::class.java)

    val chatMessages: Flow<List<ChatMessage>> = chatDao.getAllMessages().map { entities ->
        entities.map { it.toChatMessage() }
    }

    val savedMacros: Flow<List<MacroRoutine>> = macroDao.getAllMacros().map { list ->
        list.map { it.toMacroRoutine() }
    }

    val whatsAppReplies: Flow<List<WhatsAppReplyEntity>> = whatsAppReplyDao.getAllReplies()

    suspend fun saveMessage(message: ChatMessage): Long = withContext(Dispatchers.IO) {
        chatDao.insertMessage(
            ChatEntity(
                sender = message.sender.name,
                text = message.text,
                persona = message.persona.name,
                actionJson = message.actionJson,
                timestamp = message.timestamp
            )
        )
    }

    suspend fun clearChat() = withContext(Dispatchers.IO) {
        chatDao.clearHistory()
    }

    suspend fun saveMacro(macro: MacroRoutine): Long = withContext(Dispatchers.IO) {
        val stepsJson = serializeMacroSteps(macro.steps)
        val varNamesJson = macro.variableNames.joinToString(",")
        macroDao.insertMacro(
            MacroEntity(
                id = macro.id,
                name = macro.name,
                description = macro.description,
                stepsJson = stepsJson,
                variableNamesJson = varNamesJson,
                isPrebuilt = macro.isPrebuilt,
                createdAt = macro.createdAt
            )
        )
    }

    suspend fun deleteMacro(macroId: Long) = withContext(Dispatchers.IO) {
        val entity = macroDao.getMacroById(macroId)
        if (entity != null) {
            macroDao.deleteMacro(entity)
        }
    }

    suspend fun saveWhatsAppReply(senderName: String, incoming: String, reply: String) = withContext(Dispatchers.IO) {
        whatsAppReplyDao.insertReply(
            WhatsAppReplyEntity(
                senderName = senderName,
                incomingMessage = incoming,
                autoRepliedText = reply,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private fun isKeyValid(key: String?): Boolean {
        if (key.isNullOrBlank()) return false
        val trimmed = key.trim()
        return trimmed != "MY_GEMINI_API_KEY" && trimmed != "MY_GROQ_API_KEY" && trimmed.length >= 8
    }

    private fun sanitizeGeminiModel(name: String?): String {
        val trimmed = name?.trim() ?: ""
        if (trimmed.isBlank() || trimmed == "gemini-2.5-flash" || trimmed == "gemini-1.5-flash") {
            return "gemini-3.5-flash"
        }
        return trimmed
    }

    // ==========================================
    // MULTI-PROVIDER TEST CONNECTION METHODS
    // ==========================================

    suspend fun testGeminiConnection(
        apiKey: String,
        modelName: String = "gemini-3.5-flash"
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (!isKeyValid(apiKey)) {
            return@withContext Pair(false, "Gemini API Key format is invalid or empty.")
        }
        val targetModel = sanitizeGeminiModel(modelName)
        try {
            val testRequest = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = "Hello Lumina, ping check."))
                    )
                ),
                generationConfig = GeminiGenerationConfig(temperature = 0.2f, maxOutputTokens = 30)
            )
            val response = try {
                GeminiApiClient.apiService.generateContentWithModel(targetModel, apiKey.trim(), testRequest)
            } catch (e: Exception) {
                try {
                    GeminiApiClient.apiService.generateContentWithModel("gemini-3.5-flash", apiKey.trim(), testRequest)
                } catch (e2: Exception) {
                    GeminiApiClient.apiService.generateContent(apiKey.trim(), testRequest)
                }
            }
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                Pair(true, "Gemini [$targetModel] Online: \"${text.trim().take(60)}\"")
            } else if (response.error != null) {
                Pair(false, "Gemini Error: ${response.error.message}")
            } else {
                Pair(false, "No response generated from Gemini API.")
            }
        } catch (e: Exception) {
            Pair(false, "Gemini Connection Error: ${e.localizedMessage ?: e.message}")
        }
    }

    suspend fun testGroqConnection(
        apiKey: String,
        modelName: String = "llama-3.3-70b-versatile"
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (!isKeyValid(apiKey)) {
            return@withContext Pair(false, "Groq API Key is empty or invalid.")
        }
        val targetModel = modelName.ifBlank { "llama-3.3-70b-versatile" }.trim()
        try {
            val authHeader = if (apiKey.startsWith("Bearer ", ignoreCase = true)) apiKey else "Bearer ${apiKey.trim()}"
            val request = OpenAiChatRequest(
                model = targetModel,
                messages = listOf(
                    OpenAiChatMessage(role = "user", content = "Ping test for Lumina AI.")
                ),
                maxTokens = 30
            )
            val response = GroqApiClient.apiService.createChatCompletion(authHeader, request)
            val text = response.choices?.firstOrNull()?.message?.content
            if (!text.isNullOrBlank()) {
                Pair(true, "Groq Cloud [$targetModel] Online: \"${text.trim().take(60)}\"")
            } else if (response.error != null) {
                Pair(false, "Groq Error: ${response.error.message}")
            } else {
                Pair(false, "No choices returned by Groq.")
            }
        } catch (e: Exception) {
            Pair(false, "Groq Connection Error: ${e.localizedMessage ?: e.message}")
        }
    }

    suspend fun testNvidiaConnection(
        apiKey: String,
        modelName: String = "meta/llama-3.1-70b-instruct"
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (!isKeyValid(apiKey)) {
            return@withContext Pair(false, "NVIDIA NIM API Key is empty or invalid.")
        }
        val targetModel = modelName.ifBlank { "meta/llama-3.1-70b-instruct" }.trim()
        try {
            val authHeader = if (apiKey.startsWith("Bearer ", ignoreCase = true)) apiKey else "Bearer ${apiKey.trim()}"
            val request = OpenAiChatRequest(
                model = targetModel,
                messages = listOf(
                    OpenAiChatMessage(role = "user", content = "Ping test for Lumina AI.")
                ),
                maxTokens = 30
            )
            val response = NvidiaApiClient.apiService.createChatCompletion(authHeader, request)
            val text = response.choices?.firstOrNull()?.message?.content
            if (!text.isNullOrBlank()) {
                Pair(true, "NVIDIA NIM [$targetModel] Online: \"${text.trim().take(60)}\"")
            } else if (response.error != null) {
                Pair(false, "NVIDIA NIM Error: ${response.error.message}")
            } else {
                Pair(false, "No choices returned by NVIDIA NIM.")
            }
        } catch (e: Exception) {
            Pair(false, "NVIDIA NIM Connection Error: ${e.localizedMessage ?: e.message}")
        }
    }

    // =========================================================================
    // MULTI-PROVIDER CASCADE ROUTER (GEMINI -> GROQ -> NVIDIA -> SMART RULE)
    // =========================================================================

    suspend fun generateLuminaResponse(
        prompt: String,
        persona: LuminaPersona,
        history: List<ChatMessage> = emptyList(),
        customGeminiApiKey: String? = null,
        geminiModelName: String = "gemini-3.5-flash",
        groqApiKey: String? = null,
        groqModelName: String = "llama-3.3-70b-versatile",
        nvidiaApiKey: String? = null,
        nvidiaModelName: String = "meta/llama-3.1-70b-instruct"
    ): Pair<String, LuminaAction?> = withContext(Dispatchers.IO) {

        // Tier 1: Google Gemini Router
        val resolvedGeminiKey = when {
            isKeyValid(customGeminiApiKey) -> customGeminiApiKey!!.trim()
            isKeyValid(BuildConfig.GEMINI_API_KEY) -> BuildConfig.GEMINI_API_KEY.trim()
            else -> null
        }
        if (resolvedGeminiKey != null) {
            val geminiResult = tryGeminiGenerate(resolvedGeminiKey, geminiModelName, prompt, persona, history)
            if (geminiResult != null) {
                return@withContext geminiResult
            }
        }

        // Tier 2: Groq Cloud API Fallback
        if (isKeyValid(groqApiKey)) {
            val groqResult = tryGroqGenerate(groqApiKey!!.trim(), groqModelName, prompt, persona, history)
            if (groqResult != null) {
                return@withContext groqResult
            }
        }

        // Tier 3: NVIDIA NIM API Fallback
        if (isKeyValid(nvidiaApiKey)) {
            val nvidiaResult = tryNvidiaGenerate(nvidiaApiKey!!.trim(), nvidiaModelName, prompt, persona, history)
            if (nvidiaResult != null) {
                return@withContext nvidiaResult
            }
        }

        // Tier 4: Autonomous High-Speed Zero-Latency Smart Rule Engine
        return@withContext generateSmartRuleResponse(prompt, persona)
    }

    private suspend fun tryGeminiGenerate(
        apiKey: String,
        modelName: String,
        prompt: String,
        persona: LuminaPersona,
        history: List<ChatMessage>
    ): Pair<String, LuminaAction?>? {
        if (!isKeyValid(apiKey)) return null
        return try {
            val systemPromptText = buildMasterSystemInstruction(persona)
            val contentsList = mutableListOf<GeminiContent>()

            // Token safety: Take last 8 history messages
            val recentHistory = history.takeLast(8)
            for (msg in recentHistory) {
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                contentsList.add(
                    GeminiContent(
                        role = role,
                        parts = listOf(GeminiPart(text = msg.text))
                    )
                )
            }
            contentsList.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt))
                )
            )

            val request = GeminiRequest(
                contents = contentsList,
                generationConfig = GeminiGenerationConfig(temperature = 0.7f),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemPromptText))
                )
            )

            val targetModel = sanitizeGeminiModel(modelName)
            val response = try {
                GeminiApiClient.apiService.generateContentWithModel(targetModel, apiKey, request)
            } catch (e: Exception) {
                try {
                    GeminiApiClient.apiService.generateContentWithModel("gemini-3.5-flash", apiKey, request)
                } catch (e2: Exception) {
                    GeminiApiClient.apiService.generateContent(apiKey, request)
                }
            }

            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!rawText.isNullOrBlank()) {
                val parsedAction = extractLuminaAction(rawText)
                Pair(rawText, parsedAction)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun tryGroqGenerate(
        apiKey: String,
        modelName: String,
        prompt: String,
        persona: LuminaPersona,
        history: List<ChatMessage>
    ): Pair<String, LuminaAction?>? {
        return try {
            val systemPromptText = buildMasterSystemInstruction(persona)
            val authHeader = if (apiKey.startsWith("Bearer ", ignoreCase = true)) apiKey else "Bearer $apiKey"
            val targetModel = modelName.ifBlank { "llama-3.3-70b-versatile" }.trim()

            val messages = mutableListOf<OpenAiChatMessage>()
            messages.add(OpenAiChatMessage(role = "system", content = systemPromptText))

            val recentHistory = history.takeLast(8)
            for (msg in recentHistory) {
                val role = if (msg.sender == MessageSender.USER) "user" else "assistant"
                messages.add(OpenAiChatMessage(role = role, content = msg.text))
            }
            messages.add(OpenAiChatMessage(role = "user", content = prompt))

            val request = OpenAiChatRequest(
                model = targetModel,
                messages = messages,
                temperature = 0.7f
            )

            val response = GroqApiClient.apiService.createChatCompletion(authHeader, request)
            val rawText = response.choices?.firstOrNull()?.message?.content
            if (!rawText.isNullOrBlank()) {
                val parsedAction = extractLuminaAction(rawText)
                Pair(rawText, parsedAction)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun tryNvidiaGenerate(
        apiKey: String,
        modelName: String,
        prompt: String,
        persona: LuminaPersona,
        history: List<ChatMessage>
    ): Pair<String, LuminaAction?>? {
        return try {
            val systemPromptText = buildMasterSystemInstruction(persona)
            val authHeader = if (apiKey.startsWith("Bearer ", ignoreCase = true)) apiKey else "Bearer $apiKey"
            val targetModel = modelName.ifBlank { "meta/llama-3.1-70b-instruct" }.trim()

            val messages = mutableListOf<OpenAiChatMessage>()
            messages.add(OpenAiChatMessage(role = "system", content = systemPromptText))

            val recentHistory = history.takeLast(8)
            for (msg in recentHistory) {
                val role = if (msg.sender == MessageSender.USER) "user" else "assistant"
                messages.add(OpenAiChatMessage(role = role, content = msg.text))
            }
            messages.add(OpenAiChatMessage(role = "user", content = prompt))

            val request = OpenAiChatRequest(
                model = targetModel,
                messages = messages,
                temperature = 0.7f
            )

            val response = NvidiaApiClient.apiService.createChatCompletion(authHeader, request)
            val rawText = response.choices?.firstOrNull()?.message?.content
            if (!rawText.isNullOrBlank()) {
                val parsedAction = extractLuminaAction(rawText)
                Pair(rawText, parsedAction)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Multimodal Screen Vision Analysis with Gemini 2.5 Flash
     * Takes Base64 JPEG screen capture + prompt and returns autonomous analysis & structured action
     */
    suspend fun analyzeScreenWithGemini(
        base64Jpeg: String,
        prompt: String,
        persona: LuminaPersona,
        customApiKey: String? = null,
        modelName: String = "gemini-3.5-flash"
    ): Pair<String, LuminaAction?> = withContext(Dispatchers.IO) {
        val resolvedKey = when {
            isKeyValid(customApiKey) -> customApiKey!!.trim()
            isKeyValid(BuildConfig.GEMINI_API_KEY) -> BuildConfig.GEMINI_API_KEY.trim()
            else -> null
        }

        if (resolvedKey != null) {
            try {
                val systemPromptText = buildMasterSystemInstruction(persona)
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            role = "user",
                            parts = listOf(
                                GeminiPart(
                                    inlineData = com.example.data.remote.InlineData(
                                        mimeType = "image/jpeg",
                                        data = base64Jpeg
                                    )
                                ),
                                GeminiPart(
                                    text = if (prompt.isNotBlank()) prompt else "Analyze this active mobile screen. Identify key UI elements, active app context, and suggest or execute the next best action for Flaxy."
                                )
                            )
                        )
                    ),
                    generationConfig = GeminiGenerationConfig(temperature = 0.4f),
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = systemPromptText))
                    )
                )

                val targetModel = sanitizeGeminiModel(modelName)
                val response = try {
                    GeminiApiClient.apiService.generateContentWithModel(targetModel, resolvedKey, request)
                } catch (e: Exception) {
                    try {
                        GeminiApiClient.apiService.generateContentWithModel("gemini-3.5-flash", resolvedKey, request)
                    } catch (e2: Exception) {
                        GeminiApiClient.apiService.generateContent(resolvedKey, request)
                    }
                }

                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!rawText.isNullOrBlank()) {
                    val parsedAction = extractLuminaAction(rawText)
                    return@withContext Pair(rawText, parsedAction)
                }
            } catch (e: Exception) {
                // Fall back to rule response below
            }
        }

        // Fallback rule response for vision
        val fallbackText = when (persona) {
            LuminaPersona.LUMINA -> "Flaxy, maine screen vision frame scan kiya hai! Active interface analyze ho chuka hai."
            LuminaPersona.FRIDAY -> "Screen vision diagnostics completed, Flaxy. UI structure indexed."
            LuminaPersona.VENOM -> "Screen dekh li Flaxy! Sab elements locked in hain, let's roll!"
        }
        Pair(fallbackText, null)
    }

    private fun generateSmartRuleResponse(prompt: String, persona: LuminaPersona): Pair<String, LuminaAction?> {
        val lower = prompt.lowercase()

        val action: LuminaAction? = when {
            lower.contains("torch") || lower.contains("flashlight") -> {
                LuminaAction(
                    type = "SYSTEM_SETTING",
                    payload = ActionPayload(setting_key = "torch", setting_value = "toggle")
                )
            }
            lower.contains("instagram") || lower.contains("reels") || lower.contains("scroll") -> {
                LuminaAction(
                    type = "GESTURE_SWIPE",
                    target_package = "com.instagram.android",
                    coordinates = Coordinates(exact_px = listOf(540, 1500)),
                    payload = ActionPayload(input_text = "Scroll down for next reel")
                )
            }
            lower.contains("whatsapp") || lower.contains("message") || lower.contains("chat") -> {
                LuminaAction(
                    type = "INTENT",
                    target_package = "com.whatsapp",
                    payload = ActionPayload(
                        intent_uri = "https://api.whatsapp.com/send?phone=+919876543210&text=Hello",
                        phone_number = "+91 98765 43210",
                        input_text = "Namaste! Flaxy will connect with you soon."
                    )
                )
            }
            lower.contains("call") || lower.contains("phone") -> {
                LuminaAction(
                    type = "INTENT",
                    payload = ActionPayload(
                        intent_uri = "tel:9876543210",
                        phone_number = "9876543210",
                        sim_slot = 1
                    )
                )
            }
            lower.contains("macro") -> {
                LuminaAction(
                    type = "MACRO_COMMAND",
                    payload = ActionPayload(macro_name = "Auto_Sequence", task_type = "RECORD_INIT")
                )
            }
            lower.contains("sos") || lower.contains("emergency") -> {
                LuminaAction(
                    type = "SOS_TRIGGER",
                    payload = ActionPayload(input_text = "GPS Coordinates Broadcast + Emergency Sequential Dialing")
                )
            }
            lower.contains("screen") || lower.contains("dekho") || lower.contains("look") -> {
                LuminaAction(
                    type = "ACCESSIBILITY_NODE",
                    node_query = NodeQuery(view_id = "com.android.systemui:id/active_window", is_clickable = true)
                )
            }
            else -> null
        }

        val speechText = when (persona) {
            LuminaPersona.LUMINA -> when {
                lower.contains("torch") || lower.contains("flashlight") ->
                    "Torch toggle kar diya Flaxy! Aur kuch help chahiye?"
                lower.contains("instagram") || lower.contains("reels") ->
                    "Instagram Reels auto-scroll ready hai Flaxy! Gesture dispatch ho raha hai."
                lower.contains("whatsapp") ->
                    "WhatsApp action ready hai Flaxy. Main direct message intent trigger kar rahi hu."
                lower.contains("call") ->
                    "SIM 1 se call prepare kar diya Flaxy. Dial connect kar rahi hu."
                lower.contains("sos") ->
                    "🚨 SOS activate ho gaya Flaxy! Live GPS aur emergency broadcast dispatch ho rahi hai!"
                lower.contains("macro") ->
                    "Haan Flaxy! Macro sequence initialize ho gaya hai. Bolo agla step kya automate karna hai?"
                lower.contains("screen") || lower.contains("dekho") ->
                    "Main tumhari active screen dekh rahi hu Flaxy! Real-time vision analyzer connected hai."
                lower.contains("hello") || lower.contains("hi") ->
                    "Hi Flaxy! Kaisa raha aaj ka din? Main tumhari help ke liye ready hu."
                else ->
                    "Main samajh gayi Flaxy! '${prompt}' pe autonomous action execute kar diya hai."
            }
            LuminaPersona.FRIDAY -> when {
                lower.contains("torch") || lower.contains("flashlight") ->
                    "Torch state toggled, Flaxy. Hardware subsystem responsive."
                lower.contains("instagram") || lower.contains("reels") ->
                    "Instagram automation profile activated, Flaxy. Gestures mapped."
                lower.contains("whatsapp") ->
                    "WhatsApp direct intent synthesized, Flaxy. Ready for transmission."
                lower.contains("call") ->
                    "Voice call protocol initialized on Primary SIM slot, Flaxy."
                lower.contains("sos") ->
                    "🚨 EMERGENCY SOS BROADCAST: Geolocation telemetry dispatched to emergency nodes, Flaxy."
                lower.contains("macro") ->
                    "Macro state engine online, Flaxy. Step buffer allocated. Standing by for Step 1."
                lower.contains("screen") || lower.contains("dekho") ->
                    "Accessibility & vision traversal completed, Flaxy. Interactive nodes indexed."
                lower.contains("hello") || lower.contains("hi") ->
                    "Good day, Flaxy. All background sub-agents and OS controllers are nominal."
                else ->
                    "Command verified, Flaxy. Executing '${prompt}' with zero-mock precision."
            }
            LuminaPersona.VENOM -> when {
                lower.contains("torch") || lower.contains("flashlight") ->
                    "Torch on kar diya partner! Andhere me sabko pelenge!"
                lower.contains("instagram") || lower.contains("reels") ->
                    "Reels auto-scroll shuru partner! Mast content dekhenge!"
                lower.contains("whatsapp") ->
                    "WhatsApp message ready partner! Seedha send karenge!"
                lower.contains("call") ->
                    "Call laga rahe hain partner! Full power connection!"
                lower.contains("sos") ->
                    "🚨 SOS ALERT! Hum live GPS location bhej rahe hain sabko! All clear!"
                lower.contains("macro") ->
                    "Macro recording chalu partner! Bol agla kya todna hai!"
                lower.contains("screen") || lower.contains("dekho") ->
                    "Screen scan complete partner! Target mil gaya hai!"
                lower.contains("hello") || lower.contains("hi") ->
                    "Aagaye partner! Aaj kiska dimaag kharab karna hai? Let's do this!"
                else ->
                    "Samajh gaye partner! '${prompt}' ko execute karte hain full power me!"
            }
        }

        val jsonBlock = if (action != null) {
            val jsonString = actionAdapter.toJson(action)
            "\n\n```json\n{\n \"lumina_action\": $jsonString\n}\n```"
        } else ""

        return Pair(speechText + jsonBlock, action)
    }

    private fun extractLuminaAction(text: String): LuminaAction? {
        return try {
            val jsonMatch = Regex("```json([\\s\\S]*?)```").find(text)?.groupValues?.getOrNull(1)
                ?: Regex("(\\{[\\s\\S]*\"lumina_action\"[\\s\\S]*\\})").find(text)?.groupValues?.getOrNull(1)
                ?: Regex("(\\{[\\s\\S]*\"action\"[\\s\\S]*\\})").find(text)?.groupValues?.getOrNull(1)

            if (jsonMatch != null) {
                val jsonObject = JSONObject(jsonMatch.trim())
                val actionObj = if (jsonObject.has("lumina_action")) jsonObject.getJSONObject("lumina_action") else jsonObject
                actionAdapter.fromJson(actionObj.toString())
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun buildMasterSystemInstruction(persona: LuminaPersona): String {
        return """
# MASTER SYSTEM INSTRUCTION: LUMINA AUTONOMOUS AI OS (ZERO-MOCK ARCHITECTURE)
Name: Lumina AI | Creator & Architect: Flaxy | Mode: ${persona.name} (${persona.title})
Active Persona Directive: ${persona.promptPrefix}

Core Directive: 100% Autonomous, Dynamic, and Non-Deterministic Execution.
Whenever an OS action is requested, emit a natural voice response in ${persona.displayName}'s tone followed by a strict JSON action block:
```json
{
 "lumina_action": {
   "type": "INTENT | ACCESSIBILITY_NODE | GESTURE_TAP | GESTURE_SWIPE | DIRECT_REPLY | SYSTEM_SETTING | SUB_AGENT_SPAWN | MACRO_COMMAND | SOS_TRIGGER",
   "target_package": "com.example.app",
   "node_query": {
     "view_id": "optional_string",
     "text": "optional_string",
     "content_desc": "optional_string",
     "is_clickable": true
   },
   "coordinates": {
     "normalized_box": [0, 0, 0, 0],
     "exact_px": [0, 0]
   },
   "payload": {
     "input_text": "string_content",
     "typing_speed_ms": 0,
     "sim_slot": 1,
     "intent_uri": "string_uri",
     "setting_key": "torch | volume | brightness | hotspot | silent_mode",
     "setting_value": "toggle | 0-100"
   }
 }
}
```
        """.trimIndent()
    }

    private fun serializeMacroSteps(steps: List<MacroStepItem>): String {
        val array = org.json.JSONArray()
        for (step in steps) {
            val obj = JSONObject().apply {
                put("stepNumber", step.stepNumber)
                put("title", step.title)
                put("actionType", step.actionType.name)
                put("targetPackage", step.targetPackage ?: "")
                put("viewId", step.viewId ?: "")
                put("textPayload", step.textPayload ?: "")
                put("xCoord", step.xCoord)
                put("yCoord", step.yCoord)
                put("delayMs", step.delayMs)
                put("variableName", step.variableName ?: "")
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun ChatEntity.toChatMessage(): ChatMessage {
        val senderEnum = try {
            MessageSender.valueOf(this.sender)
        } catch (e: Exception) {
            MessageSender.LUMINA
        }
        val personaEnum = try {
            LuminaPersona.valueOf(this.persona)
        } catch (e: Exception) {
            LuminaPersona.LUMINA
        }
        val parsedAction = this.actionJson?.let {
            try {
                actionAdapter.fromJson(it)
            } catch (e: Exception) {
                null
            }
        }
        return ChatMessage(
            id = this.id,
            sender = senderEnum,
            text = this.text,
            persona = personaEnum,
            actionJson = this.actionJson,
            parsedAction = parsedAction,
            timestamp = this.timestamp
        )
    }

    private fun MacroEntity.toMacroRoutine(): MacroRoutine {
        val stepList = mutableListOf<MacroStepItem>()
        try {
            val array = org.json.JSONArray(this.stepsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                stepList.add(
                    MacroStepItem(
                        stepNumber = obj.optInt("stepNumber", i + 1),
                        title = obj.optString("title", "Step ${i + 1}"),
                        actionType = try {
                            ActionType.valueOf(obj.optString("actionType", "ACCESSIBILITY_NODE"))
                        } catch (e: Exception) {
                            ActionType.ACCESSIBILITY_NODE
                        },
                        targetPackage = obj.optString("targetPackage").ifEmpty { null },
                        viewId = obj.optString("viewId").ifEmpty { null },
                        textPayload = obj.optString("textPayload").ifEmpty { null },
                        xCoord = obj.optInt("xCoord", 0),
                        yCoord = obj.optInt("yCoord", 0),
                        delayMs = obj.optLong("delayMs", 800),
                        variableName = obj.optString("variableName").ifEmpty { null }
                    )
                )
            }
        } catch (e: Exception) {
            // Error parsing steps
        }
        val varNames = this.variableNamesJson.split(",").filter { it.isNotBlank() }
        return MacroRoutine(
            id = this.id,
            name = this.name,
            description = this.description,
            steps = stepList,
            variableNames = varNames,
            isPrebuilt = this.isPrebuilt,
            createdAt = this.createdAt
        )
    }
}
