package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.model.AutoReplyStatus
import com.example.data.model.WhatsAppChatThread
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LuminaNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "LuminaNotification"
        var instance: LuminaNotificationListener? = null
            private set

        private val _isListenerConnected = MutableStateFlow(false)
        val isListenerConnected: StateFlow<Boolean> = _isListenerConnected.asStateFlow()

        private val _incomingThreads = MutableStateFlow<List<WhatsAppChatThread>>(emptyList())
        val incomingThreads: StateFlow<List<WhatsAppChatThread>> = _incomingThreads.asStateFlow()

        private val _lastReplyLog = MutableStateFlow<String>("DirectReply listener initialized")
        val lastReplyLog: StateFlow<String> = _lastReplyLog.asStateFlow()

        var onMessageIntercepted: ((sender: String, message: String, sbn: StatusBarNotification) -> Unit)? = null
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        _isListenerConnected.value = true
        _lastReplyLog.value = "Notification Listener Service Connected"
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        _isListenerConnected.value = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val packageName = sbn.packageName

        // Intercept WhatsApp / Telegram / Instagram notifications
        val appSource = when (packageName) {
            "com.whatsapp", "com.whatsapp.w4b" -> com.example.data.model.AppSource.WHATSAPP
            "org.telegram.messenger", "org.telegram.plus" -> com.example.data.model.AppSource.TELEGRAM
            "com.instagram.android" -> com.example.data.model.AppSource.INSTAGRAM
            else -> if (packageName.contains("whatsapp") || packageName.contains("telegram")) com.example.data.model.AppSource.WHATSAPP else null
        }

        if (appSource != null) {
            val extras = sbn.notification.extras
            val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

            if (title.isNotEmpty() && text.isNotEmpty()) {
                val newThread = WhatsAppChatThread(
                    id = "${sbn.id}_${System.currentTimeMillis()}",
                    senderName = title,
                    lastMessage = text,
                    timeAgo = "Just now",
                    appSource = appSource,
                    status = AutoReplyStatus.PENDING,
                    avatarColorHex = appSource.colorHex
                )
                val current = _incomingThreads.value.toMutableList()
                current.removeAll { it.senderName == title }
                current.add(0, newThread)
                _incomingThreads.value = current

                onMessageIntercepted?.invoke(title, text, sbn)
            }
        }
    }

    /**
     * Sends background response directly through Android RemoteInput without waking the screen
     */
    fun sendDirectReply(sbn: StatusBarNotification, replyText: String): Boolean {
        val actions = NotificationCompat.getActionCount(sbn.notification).let { count ->
            (0 until count).mapNotNull { NotificationCompat.getAction(sbn.notification, it) }
        }

        val replyAction = actions.firstOrNull { action ->
            val inputs = action.remoteInputs
            inputs != null && inputs.isNotEmpty()
        } ?: return false

        val remoteInputs = replyAction.remoteInputs ?: return false
        val intent = Intent()
        val bundle = Bundle()

        for (input in remoteInputs) {
            bundle.putCharSequence(input.resultKey, replyText)
        }
        androidx.core.app.RemoteInput.addResultsToIntent(remoteInputs, intent, bundle)

        val pendingIntent = replyAction.actionIntent ?: return false

        return try {
            pendingIntent.send(this, 0, intent)
            _lastReplyLog.value = "Autonomous DirectReply sent to WhatsApp: '$replyText'"
            true
        } catch (e: PendingIntent.CanceledException) {
            Log.e(TAG, "DirectReply failed", e)
            _lastReplyLog.value = "DirectReply failed: ${e.message}"
            false
        }
    }
}
