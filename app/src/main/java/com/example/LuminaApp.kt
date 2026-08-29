package com.example

import android.app.Application
import com.example.data.local.LuminaDatabase
import com.example.data.model.ActionType
import com.example.data.model.MacroRoutine
import com.example.data.model.MacroStepItem
import com.example.data.repository.LuminaRepository
import com.example.service.SystemController
import com.example.service.VoiceSpeechEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LuminaApp : Application() {

    lateinit var database: LuminaDatabase
        private set

    lateinit var repository: LuminaRepository
        private set

    lateinit var speechEngine: VoiceSpeechEngine
        private set

    lateinit var systemController: SystemController
        private set

    lateinit var mqttManager: com.example.service.MqttManager
        private set

    lateinit var voiceBiometricsEngine: com.example.service.VoiceBiometricsEngine
        private set

    private val appScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        database = LuminaDatabase.getDatabase(this)
        repository = LuminaRepository(
            chatDao = database.chatDao(),
            macroDao = database.macroDao(),
            whatsAppReplyDao = database.whatsAppReplyDao()
        )
        speechEngine = VoiceSpeechEngine(this)
        systemController = SystemController(this)
        mqttManager = com.example.service.MqttManager(this)
        voiceBiometricsEngine = com.example.service.VoiceBiometricsEngine(this)

        seedPrebuiltMacrosIfEmpty()
    }

    private fun seedPrebuiltMacrosIfEmpty() {
        appScope.launch {
            val existing = repository.savedMacros.first()
            if (existing.isEmpty()) {
                val prebuilts = listOf(
                    MacroRoutine(
                        name = "Morning Master Routine",
                        description = "Checks weather, syncs calendar, adjusts audio, and greets with active persona",
                        steps = listOf(
                            MacroStepItem(1, "Turn off silent mode", ActionType.SYSTEM_SETTING, textPayload = "volume:70"),
                            MacroStepItem(2, "Fetch morning briefing", ActionType.SUB_AGENT_SPAWN, textPayload = "Summarize top tech news & weather"),
                            MacroStepItem(3, "Send good morning broadcast", ActionType.INTENT, targetPackage = "com.whatsapp", textPayload = "Good morning! Hope you have an awesome day.", variableName = "MORNING_GREETING")
                        ),
                        variableNames = listOf("MORNING_GREETING"),
                        isPrebuilt = true
                    ),
                    MacroRoutine(
                        name = "Instagram Reels Auto-Scroller",
                        description = "Automates swipe up gestures on short video feeds hands-free",
                        steps = listOf(
                            MacroStepItem(1, "Launch Instagram", ActionType.INTENT, targetPackage = "com.instagram.android"),
                            MacroStepItem(2, "Navigate to Reels Tab", ActionType.ACCESSIBILITY_NODE, viewId = "com.instagram.android:id/reels_tab"),
                            MacroStepItem(3, "Swipe Up (Next Reel)", ActionType.GESTURE_SWIPE, xCoord = 540, yCoord = 1500, delayMs = 3000),
                            MacroStepItem(4, "Swipe Up (Next Reel)", ActionType.GESTURE_SWIPE, xCoord = 540, yCoord = 1500, delayMs = 3000)
                        ),
                        isPrebuilt = true
                    ),
                    MacroRoutine(
                        name = "BGMI Gaming Turbo Mode",
                        description = "Sets screen brightness, engages Game Co-Caster, and clears background noise",
                        steps = listOf(
                            MacroStepItem(1, "Optimize Game Volume", ActionType.SYSTEM_SETTING, textPayload = "volume:100"),
                            MacroStepItem(2, "Activate Live Co-Caster", ActionType.SCREEN_CAPTURE, textPayload = "BGMI Real-time Commentary"),
                            MacroStepItem(3, "Engage Venom Bro Persona", ActionType.SYSTEM_SETTING, textPayload = "persona:VENOM")
                        ),
                        isPrebuilt = true
                    ),
                    MacroRoutine(
                        name = "WhatsApp Status Broadcaster",
                        description = "Formats text status with custom prompt and sends dynamically",
                        steps = listOf(
                            MacroStepItem(1, "Open WhatsApp", ActionType.INTENT, targetPackage = "com.whatsapp"),
                            MacroStepItem(2, "Navigate to Status Tab", ActionType.ACCESSIBILITY_NODE, textPayload = "Updates"),
                            MacroStepItem(3, "Type Status Message", ActionType.ACCESSIBILITY_NODE, viewId = "com.whatsapp:id/status_entry", textPayload = "\${STATUS_MESSAGE}", variableName = "STATUS_MESSAGE")
                        ),
                        variableNames = listOf("STATUS_MESSAGE"),
                        isPrebuilt = true
                    ),
                    MacroRoutine(
                        name = "Emergency SOS Protocol",
                        description = "Transmits GPS coordinates to emergency services and triggers alarms",
                        steps = listOf(
                            MacroStepItem(1, "Lockdown Device", ActionType.SYSTEM_SETTING, textPayload = "lock_screen"),
                            MacroStepItem(2, "Broadcast SOS Beacon", ActionType.SOS_TRIGGER, textPayload = "GPS Telemetry 28.6139N 77.2090E")
                        ),
                        isPrebuilt = true
                    )
                )

                for (routine in prebuilts) {
                    repository.saveMacro(routine)
                }
            }
        }
    }
}
