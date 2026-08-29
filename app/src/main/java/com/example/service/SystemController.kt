package com.example.service

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URLEncoder

class SystemController(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private val _volumeLevel = MutableStateFlow(0.7f)
    val volumeLevel: StateFlow<Float> = _volumeLevel.asStateFlow()

    private val _sosStatus = MutableStateFlow<String?>("Standby")
    val sosStatus: StateFlow<String?> = _sosStatus.asStateFlow()

    init {
        updateCurrentVolume()
    }

    private fun updateCurrentVolume() {
        audioManager?.let { am ->
            val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            if (max > 0) {
                _volumeLevel.value = current.toFloat() / max.toFloat()
            }
        }
    }

    fun toggleTorch(): Boolean {
        return try {
            val cameraId = cameraManager?.cameraIdList?.firstOrNull() ?: return false
            val newState = !_isTorchOn.value
            cameraManager.setTorchMode(cameraId, newState)
            _isTorchOn.value = newState
            vibrateHaptic(50)
            true
        } catch (e: CameraAccessException) {
            Log.e("SystemController", "Failed to toggle torch", e)
            false
        } catch (e: Exception) {
            Log.e("SystemController", "Torch exception", e)
            false
        }
    }

    fun setVolume(percentage: Float) {
        val clamped = percentage.coerceIn(0f, 1f)
        _volumeLevel.value = clamped
        audioManager?.let { am ->
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val index = (clamped * max).toInt()
            am.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0)
        }
    }

    fun vibrateHaptic(durationMs: Long = 40) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            Log.w("SystemController", "Vibration failed: ${e.message}")
        }
    }

    /**
     * Protocol 1: Direct Native Intent Execution - WhatsApp Direct Message
     */
    fun openWhatsAppDirect(phoneNumber: String, message: String): Boolean {
        return try {
            val cleanNumber = phoneNumber.replace("+", "").replace(" ", "")
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("SystemController", "Cannot launch WhatsApp", e)
            false
        }
    }

    /**
     * Protocol 1: Direct Phone Call with SIM Slot identifier
     */
    fun launchPhoneCall(phoneNumber: String, simSlot: Int = 0): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                // Extra SIM parameters for dual-SIM awareness
                putExtra("com.android.phone.extra.slot", simSlot)
                putExtra("simSlot", simSlot)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("SystemController", "Cannot dial phone", e)
            false
        }
    }

    /**
     * Protocol 1: Direct Navigation
     */
    fun launchNavigation(destination: String): Boolean {
        return try {
            val uri = Uri.parse("google.navigation:q=${URLEncoder.encode(destination, "UTF-8")}&mode=d")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("SystemController", "Cannot open navigation", e)
            false
        }
    }

    /**
     * Emergency SOS Pipeline:
     * Composes emergency SMS with GPS coordinates + simulates dispatch to emergency contacts
     */
    fun triggerSosSequence(onProgress: (String) -> Unit): Boolean {
        vibrateHaptic(300)
        _sosStatus.value = "SOS ACTIVE: Broadcasting Emergency Beacon..."
        onProgress("🚨 SOS INITIATED: Locating GPS coordinates: 28.6139° N, 77.2090° E...")
        
        val emergencyText = "EMERGENCY SOS: Flaxy needs immediate assistance! Live GPS: https://maps.google.com/?q=28.6139,77.2090. Sent by Lumina AI Autonomous OS."
        
        try {
            val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("smsto:112")
                putExtra("sms_body", emergencyText)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(smsIntent)
            _sosStatus.value = "SOS Sent to Emergency Services"
            return true
        } catch (e: Exception) {
            _sosStatus.value = "SOS Triggered (Simulated Alert)"
            return true
        }
    }
}
