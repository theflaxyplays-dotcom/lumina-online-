package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.LuminaPersona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceSpeechEngine(private val context: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "VoiceSpeechEngine"
    }

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isContinuousLoopActive = MutableStateFlow(false)
    val isContinuousLoopActive: StateFlow<Boolean> = _isContinuousLoopActive.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var lastResultCallback: ((String) -> Unit)? = null
    private var onSpeechDoneCallback: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("hi", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    mainHandler.post {
                        onSpeechDoneCallback?.invoke()
                        if (_isContinuousLoopActive.value) {
                            // After TTS speech finishes, wait 350ms and seamlessly resume listening
                            mainHandler.postDelayed({
                                if (_isContinuousLoopActive.value && !_isSpeaking.value) {
                                    lastResultCallback?.let { startListening(it) }
                                }
                            }, 350)
                        }
                    }
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
            isTtsReady = true
        }
    }

    fun setContinuousLoop(enabled: Boolean) {
        _isContinuousLoopActive.value = enabled
        if (!enabled) {
            stopListening()
        }
    }

    /**
     * Speaks text tailored to the selected Persona voice tone and pitch
     */
    fun speak(text: String, persona: LuminaPersona, onComplete: (() -> Unit)? = null) {
        if (!isTtsReady || tts == null) return

        onSpeechDoneCallback = onComplete

        // Clean any raw json markup, code fences, or XML tags before speaking aloud
        val cleanSpeech = text
            .replace(Regex("```[\\s\\S]*?```"), "")
            .replace(Regex("\\{.*?\\}"), "")
            .replace(Regex("<.*?>"), "")
            .trim()

        if (cleanSpeech.isEmpty()) {
            onComplete?.invoke()
            return
        }

        tts?.setPitch(persona.ttsPitch)
        tts?.setSpeechRate(persona.ttsSpeechRate)

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "LuminaSpeech_${System.currentTimeMillis()}")
        }

        tts?.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, params, "LuminaSpeech_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    /**
     * Checks if input is an interrupt phrase ("stop", "ruko", "chup", "pause")
     */
    fun isInterruptPhrase(input: String): Boolean {
        val lower = input.lowercase().trim()
        return lower in listOf("stop", "ruko", "chup", "pause", "stop lumina", "arrey ruko", "bas", "wait")
    }

    /**
     * Checks if input is a sleep phrase ("so jao", "rest karo", "sleep", "bye")
     */
    fun isSleepPhrase(input: String): Boolean {
        val lower = input.lowercase().trim()
        return lower.contains("so jao") || lower.contains("rest karo") || lower.contains("bye lumina") || lower.contains("sleep lumina") || lower == "sleep"
    }

    /**
     * Checks if input is a wake phrase ("wake up lumina", "hello lumina", "wake up")
     */
    fun isWakePhrase(input: String): Boolean {
        val lower = input.lowercase().trim()
        return lower.contains("wake up") || lower.contains("hello lumina") || lower.contains("hii lumina") || lower.contains("friday") || lower.contains("venom")
    }

    fun startListening(onResult: (String) -> Unit) {
        lastResultCallback = onResult

        mainHandler.post {
            val hasMicPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!hasMicPermission) {
                Log.w(TAG, "RECORD_AUDIO permission not granted, skipping SpeechRecognizer start")
                _isListening.value = false
                return@post
            }

            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                Log.w(TAG, "Speech recognition unavailable")
                return@post
            }

            try {
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
                speechRecognizer = null

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _isListening.value = true
                        }

                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            _isListening.value = false
                        }

                        override fun onError(error: Int) {
                            _isListening.value = false
                            Log.w(TAG, "SpeechRecognizer error code: $error")
                            if (_isContinuousLoopActive.value && error != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                                mainHandler.postDelayed({
                                    if (_isContinuousLoopActive.value && !_isSpeaking.value) {
                                        startListening(onResult)
                                    }
                                }, 1000)
                            }
                        }

                        override fun onResults(results: Bundle?) {
                            _isListening.value = false
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull() ?: ""
                            if (text.isNotEmpty()) {
                                _recognizedText.value = text

                                // Check for instantaneous interrupt
                                if (isInterruptPhrase(text)) {
                                    stopSpeaking()
                                }

                                onResult(text)
                            } else if (_isContinuousLoopActive.value) {
                                mainHandler.postDelayed({
                                    if (_isContinuousLoopActive.value && !_isSpeaking.value) {
                                        startListening(onResult)
                                    }
                                }, 500)
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull() ?: ""
                            if (text.isNotEmpty()) {
                                _recognizedText.value = text
                                if (isInterruptPhrase(text)) {
                                    stopSpeaking()
                                }
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Lumina...")
                }

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting speech recognition", e)
                _isListening.value = false
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping listening", e)
            }
            _isListening.value = false
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        mainHandler.post {
            speechRecognizer?.destroy()
            speechRecognizer = null
        }
    }
}
