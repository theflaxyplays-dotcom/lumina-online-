package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream

class ScreenCaptureService : Service() {

    companion object {
        private const val TAG = "ScreenCaptureService"
        const val CHANNEL_ID = "lumina_screen_vision_channel"
        const val NOTIFICATION_ID = 2002
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"

        private var instance: ScreenCaptureService? = null

        private val _isVisionActive = MutableStateFlow(false)
        val isVisionActive: StateFlow<Boolean> = _isVisionActive.asStateFlow()

        private val _lastCapturedBitmap = MutableStateFlow<Bitmap?>(null)
        val lastCapturedBitmap: StateFlow<Bitmap?> = _lastCapturedBitmap.asStateFlow()

        fun isServiceRunning(): Boolean = instance != null

        fun captureCurrentScreen(): Bitmap? {
            return instance?.captureBitmap()
        }

        fun captureCurrentScreenBase64(): String? {
            val bmp = captureCurrentScreen() ?: return null
            return bitmapToBase64(bmp)
        }

        fun bitmapToBase64(bitmap: Bitmap): String {
            val outputStream = ByteArrayOutputStream()
            // Compress to JPEG with 80% quality for optimal size & Gemini vision clarity
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.NO_WRAP)
        }
    }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var screenWidth = 720
    private var screenHeight = 1280
    private var screenDensity = 320

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildForegroundNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        if (intent != null && intent.hasExtra(EXTRA_RESULT_CODE) && intent.hasExtra(EXTRA_RESULT_DATA)) {
            val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
            val resultData = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
            if (resultData != null) {
                setupMediaProjection(resultCode, resultData)
            }
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lumina Live Screen Vision",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Captures screen frames for real-time Gemini Multimodal AI vision analysis"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Lumina Screen Vision Active")
            .setContentText("Real-time Multimodal Gemini 2.5 Flash Vision Connected")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun setupMediaProjection(resultCode: Int, resultData: Intent) {
        val mpManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mpManager.getMediaProjection(resultCode, resultData)

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)

        // Scale down slightly to 720p width maintaining aspect ratio for ultra-fast Gemini upload
        val rawWidth = metrics.widthPixels
        val rawHeight = metrics.heightPixels
        val scale = 720f / rawWidth.coerceAtLeast(1)
        screenWidth = 720
        screenHeight = (rawHeight * scale).toInt().coerceAtLeast(1280)
        screenDensity = metrics.densityDpi

        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "LuminaScreenCapture",
            screenWidth,
            screenHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )

        _isVisionActive.value = true
        Log.d(TAG, "MediaProjection VirtualDisplay created ($screenWidth x $screenHeight)")
    }

    fun captureBitmap(): Bitmap? {
        val reader = imageReader ?: return null
        return try {
            val image = reader.acquireLatestImage() ?: reader.acquireNextImage()
            if (image != null) {
                val planes = image.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * screenWidth

                val bitmap = Bitmap.createBitmap(
                    screenWidth + rowPadding / pixelStride,
                    screenHeight,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)
                image.close()

                val cleanBitmap = if (rowPadding == 0) {
                    bitmap
                } else {
                    Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight)
                }

                _lastCapturedBitmap.value = cleanBitmap
                cleanBitmap
            } else {
                _lastCapturedBitmap.value
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error capturing bitmap from ImageReader", e)
            _lastCapturedBitmap.value
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _isVisionActive.value = false
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        instance = null
    }
}
