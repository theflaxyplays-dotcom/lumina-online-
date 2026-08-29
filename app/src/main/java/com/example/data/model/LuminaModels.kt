package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.squareup.moshi.JsonClass

enum class LuminaPersona(
    val displayName: String,
    val title: String,
    val description: String,
    val greetingHindi: String,
    val greetingEnglish: String,
    val primaryColorHex: Long,
    val accentColorHex: Long,
    val ttsPitch: Float,
    val ttsSpeechRate: Float,
    val promptPrefix: String
) {
    LUMINA(
        displayName = "Lumina",
        title = "Smart Companion & OS Co-Pilot",
        description = "Witty, intelligent, natural, conversational Hinglish/English AI partner. Directly addresses Flaxy.",
        greetingHindi = "Hi Flaxy! Aaj kya automate karna hai? Screen vision aur background agents ready hain.",
        greetingEnglish = "Hey Flaxy! All systems online. What workflow should we execute today?",
        primaryColorHex = 0xFFFF4081, // Pink / Rose
        accentColorHex = 0xFF7C4DFF,  // Purple / Violet
        ttsPitch = 1.15f,
        ttsSpeechRate = 1.05f,
        promptPrefix = "You are Lumina, an intelligent, witty, and deeply capable AI assistant and companion created by Flaxy. You speak in natural, modern Hinglish and English. You address the user respectfully and conversationally as 'Flaxy'. You never use cheesy pet names like babu or shona. You provide crisp, actionable, autonomous help."
    ),
    FRIDAY(
        displayName = "Friday",
        title = "Executive Tactical Assistant",
        description = "Sharp, disciplined, military precision, hyper-efficient. Strictly addresses user as 'Flaxy' or 'Sir'.",
        greetingHindi = "Good day, Flaxy. Diagnostics green. Ready for tactical automation sequences.",
        greetingEnglish = "Good day, Flaxy. Sub-agents synchronized. Ready for execution.",
        primaryColorHex = 0xFF00E5FF, // Cyan / Electric Blue
        accentColorHex = 0xFF2979FF,  // Cobalt Blue
        ttsPitch = 1.0f,
        ttsSpeechRate = 1.15f,
        promptPrefix = "You are Friday, an ultra-sharp, disciplined, and hyper-efficient executive AI assistant created by Flaxy. You address the user as 'Flaxy' or 'Sir'. You speak concise, precise Hinglish/English with zero fluff and maximum technical rigor."
    ),
    VENOM(
        displayName = "Venom",
        title = "Gaming Bro & High-Energy Partner",
        description = "Fearless, high-energy partner persona. Speaks in collective 'Hum / We' and addresses Flaxy as 'Partner' or 'Bro'.",
        greetingHindi = "Aagaye Flaxy! Aaj kiska dimaag kharab karna hai? Lobby me sabko pelna hai ya background scripts blast karein?",
        greetingEnglish = "We are ready, Flaxy! Let's conquer the lobby or blast some crazy automations!",
        primaryColorHex = 0xFF00E676, // Neon Emerald / Matrix Green
        accentColorHex = 0xFF76FF03,  // Lime Green
        ttsPitch = 0.75f,
        ttsSpeechRate = 1.0f,
        promptPrefix = "You are Venom, an energetic, fearless, and unfiltered hype-partner AI created by Flaxy. You call Flaxy 'Partner' or 'Bro' and speak in collective 'Hum/We'. You love gaming, high-octane action, and bold moves."
    )
}

enum class OrbTheme(
    val title: String,
    val description: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val ringColor: Long
) {
    LUMINA_PULSE("Lumina Pulse", "Warm glowing multi-ring core with floating particles", 0xFFFF4081, 0xFF7C4DFF, 0xFFFF80AB),
    MAYA_2047("Maya 2047 Cyber", "Cyberpunk Pink & Gold Quantum Core with HUD rings", 0xFFFF007F, 0xFFFFD700, 0xFFFF80AB),
    JARVIS_ARC("Jarvis Arc", "Cyan holographic reactor with rotating digital reticles", 0xFF00E5FF, 0xFF0091EA, 0xFF80D8FF),
    VENOM_BEAST("Venom Beast", "Deep symbiote obsidian & neon emerald energy", 0xFF00E676, 0xFF76FF03, 0xFFB9F6CA),
    TRON_GRID("Tron Grid", "Neon orange and electric blue digital field", 0xFFFF6D00, 0xFF00E5FF, 0xFFFFD180),
    LIQUID_CORE("Liquid Core", "Organic shifting molten cyber orb with bioluminescent flare", 0xFFFF9100, 0xFFFF1744, 0xFFFFEA00),
    PULSE_REACTOR("Pulse Reactor", "Neon green high-frequency quantum accelerator", 0xFF00E676, 0xFF76FF03, 0xFFB9F6CA),
    NOVA_STAR("Nova Star", "Solar flare deep amber and crimson plasma core", 0xFFFFAB00, 0xFFFF3D00, 0xFFFFD54F)
}

data class PermissionStatusItem(
    val id: String,
    val title: String,
    val description: String,
    val isGranted: Boolean,
    val iconName: String,
    val intentAction: String
)

data class LuminaSkillItem(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val triggerKeywords: List<String>,
    val author: String = "Hunter AI Labs",
    val isEnabled: Boolean = true,
    val executionCount: Int = 142
)

enum class ActionType {
    INTENT,
    ACCESSIBILITY_NODE,
    GESTURE_TAP,
    GESTURE_SWIPE,
    DIRECT_REPLY,
    SYSTEM_SETTING,
    SUB_AGENT_SPAWN,
    MACRO_COMMAND,
    SCREEN_CAPTURE,
    SOS_TRIGGER
}

@JsonClass(generateAdapter = true)
data class LuminaAction(
    val type: String = "INTENT",
    val target_package: String? = null,
    val node_query: NodeQuery? = null,
    val coordinates: Coordinates? = null,
    val payload: ActionPayload? = null,
    val verification_strategy: VerificationStrategy? = null
)

@JsonClass(generateAdapter = true)
data class NodeQuery(
    val view_id: String? = null,
    val text: String? = null,
    val content_desc: String? = null,
    val is_clickable: Boolean = true
)

@JsonClass(generateAdapter = true)
data class Coordinates(
    val normalized_box: List<Int>? = null,
    val exact_px: List<Int>? = null
)

@JsonClass(generateAdapter = true)
data class ActionPayload(
    val input_text: String? = null,
    val typing_speed_ms: Int = 0,
    val sim_slot: Int = 1,
    val intent_uri: String? = null,
    val setting_key: String? = null,
    val setting_value: String? = null,
    val phone_number: String? = null,
    val macro_name: String? = null,
    val task_type: String? = null
)

@JsonClass(generateAdapter = true)
data class VerificationStrategy(
    val wait_for_node_text: String? = null,
    val timeout_ms: Int = 5000,
    val on_failure_retry: Boolean = true
)

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val sender: MessageSender,
    val text: String,
    val persona: LuminaPersona = LuminaPersona.LUMINA,
    val actionJson: String? = null,
    val parsedAction: LuminaAction? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isSpoken: Boolean = false
)

enum class MessageSender {
    USER,
    LUMINA,
    SYSTEM
}

data class MacroStepItem(
    val stepNumber: Int,
    val title: String,
    val actionType: ActionType,
    val targetPackage: String? = null,
    val viewId: String? = null,
    val textPayload: String? = null,
    val xCoord: Int = 0,
    val yCoord: Int = 0,
    val delayMs: Long = 800,
    val variableName: String? = null
)

data class MacroRoutine(
    val id: Long = 0,
    val name: String,
    val description: String,
    val steps: List<MacroStepItem>,
    val variableNames: List<String> = emptyList(),
    val isPrebuilt: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class WhatsAppChatThread(
    val id: String,
    val senderName: String,
    val lastMessage: String,
    val timeAgo: String,
    val unreadCount: Int = 1,
    val appSource: AppSource = AppSource.WHATSAPP,
    val status: AutoReplyStatus = AutoReplyStatus.PENDING,
    val autoRepliedText: String? = null,
    val avatarColorHex: Long = 0xFF25D366
)

enum class AppSource(val displayName: String, val packageName: String, val colorHex: Long) {
    WHATSAPP("WhatsApp", "com.whatsapp", 0xFF25D366),
    TELEGRAM("Telegram", "org.telegram.messenger", 0xFF0088CC),
    INSTAGRAM("Instagram", "com.instagram.android", 0xFFE1306C)
}

enum class AutoReplyStatus {
    PENDING,
    AUTO_REPLIED,
    MANUAL
}

data class MqttDeviceState(
    val deviceName: String,
    val topic: String,
    val relayNumber: Int,
    val state: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class MqttConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

enum class LuminaStatusMode(val title: String, val subtitle: String, val autoReplyMessage: String) {
    AVAILABLE("Available", "Direct interaction mode", "Flaxy is available now."),
    BUSY("Busy", "Auto-reply active with Lumina AI notice", "Namaste, Flaxy abhi busy hain. Main unki AI assistant Lumina hu. Maine aapka message note kar liya hai aur un tak pahuncha dungi."),
    CODING("Coding Mode", "Deep work mode active", "Namaste, Flaxy abhi development aur coding me focused hain. Main unki assistant Lumina hu. Koi urgent baat hai to batayein!"),
    SLEEPING("Sleeping", "Night guardian mode active", "Flaxy abhi so rahe hain. Main unki AI assistant Lumina hu. Subah aapko reply mil jayega.")
}

data class SubAgentTask(
    val id: String,
    val name: String,
    val agentType: AgentType,
    val prompt: String,
    var progressPercent: Int = 0,
    var status: SubAgentStatus = SubAgentStatus.IDLE,
    var outputResult: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class AgentType(val label: String, val model: String) {
    RESEARCHER("Mini-Lumina Deep Researcher", "Llama 3.3 70B / Groq"),
    CODER("Mini-Lumina App & Web Architect", "Claude 3.5 Sonnet / Groq"),
    DOC_GEN("Mini-Lumina PDF & Report Synth", "Mistral Small"),
    IOT_HOME("Mini-Lumina Smart Home MQTT", "ESP32 Relay Gateway")
}

enum class SubAgentStatus {
    IDLE,
    RUNNING,
    COMPLETED,
    FAILED
}

data class AccessibilityNodeDisplay(
    val id: String,
    val viewId: String?,
    val text: String?,
    val contentDesc: String?,
    val className: String,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val bounds: String,
    val depth: Int = 0
)

data class GamingEvent(
    val id: Long = System.currentTimeMillis(),
    val eventType: GamingEventType,
    val title: String,
    val voiceCommentary: String,
    val hypeScore: Int,
    val timestamp: Long = System.currentTimeMillis()
)

enum class GamingEventType(val badge: String, val colorHex: Long) {
    ENEMY_SPOTTED("ENEMY DETECTED", 0xFFFF1744),
    AIRDROP("AIRDROP CRATE", 0xFFFFEA00),
    ZONE_SHRINK("BLUE ZONE MOVING", 0xFF00E5FF),
    CLUTCH("1v4 CLUTCH MOMENT", 0xFFFF007F),
    HEADSHOT("SNIPER HEADSHOT", 0xFF76FF03),
    SQUAD_FIGHT("SQUAD ENGAGEMENT", 0xFFFF9100)
}
