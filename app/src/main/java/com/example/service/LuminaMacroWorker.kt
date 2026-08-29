package com.example.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.LuminaDatabase
import com.example.data.model.ActionType
import com.example.data.model.MacroRoutine
import com.example.data.model.MacroStepItem
import kotlinx.coroutines.delay
import org.json.JSONArray
import java.util.concurrent.TimeUnit

class LuminaMacroWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "LuminaMacroWorker"
        const val KEY_MACRO_ID = "key_macro_id"
        const val KEY_MACRO_NAME = "key_macro_name"

        fun scheduleMacro(context: Context, macroId: Long, macroName: String, delaySeconds: Long) {
            val inputData = Data.Builder()
                .putLong(KEY_MACRO_ID, macroId)
                .putString(KEY_MACRO_NAME, macroName)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<LuminaMacroWorker>()
                .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                .setInputData(inputData)
                .addTag("LuminaMacro_$macroId")
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
            Log.d(TAG, "Scheduled macro '$macroName' with initial delay $delaySeconds seconds")
        }

        fun cancelScheduledMacro(context: Context, macroId: Long) {
            WorkManager.getInstance(context).cancelAllWorkByTag("LuminaMacro_$macroId")
        }
    }

    override suspend fun doWork(): Result {
        val macroId = inputData.getLong(KEY_MACRO_ID, -1L)
        val macroName = inputData.getString(KEY_MACRO_NAME) ?: "Automated Routine"
        Log.d(TAG, "Executing scheduled macro: ID=$macroId Name=$macroName")

        val database = LuminaDatabase.getDatabase(applicationContext)
        val macroEntity = database.macroDao().getMacroById(macroId) ?: return Result.failure()

        val steps = parseSteps(macroEntity.stepsJson)
        val systemController = SystemController(applicationContext)

        for (step in steps) {
            delay(step.delayMs.coerceAtLeast(600))
            systemController.vibrateHaptic(30)

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
                    if (!step.textPayload.isNullOrEmpty()) {
                        LuminaAccessibilityService.instance?.performDynamicSetText(step.textPayload)
                    } else {
                        LuminaAccessibilityService.instance?.performDynamicClick(step.viewId, step.textPayload, null)
                    }
                }
                ActionType.INTENT -> {
                    if (step.targetPackage == "com.whatsapp") {
                        systemController.openWhatsAppDirect("+919876543210", step.textPayload ?: "Hello")
                    }
                }
                ActionType.SOS_TRIGGER -> {
                    systemController.triggerSosSequence {}
                }
                else -> {}
            }
        }

        Log.d(TAG, "Completed scheduled macro '$macroName' successfully")
        return Result.success()
    }

    private fun parseSteps(stepsJson: String): List<MacroStepItem> {
        val list = mutableListOf<MacroStepItem>()
        try {
            val array = JSONArray(stepsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
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
            Log.e(TAG, "Error parsing macro steps in worker", e)
        }
        return list
    }
}
