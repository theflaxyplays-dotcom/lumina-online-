package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sqrt

class VoiceBiometricsEngine(private val context: Context) {

    companion object {
        private const val TAG = "VoiceBiometricsEngine"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val FRAME_SIZE = 512
        private const val HOP_SIZE = 256
        private const val NUM_MEL_BANKS = 20
        private const val VECTOR_DIM = 20
        private const val PREFS_NAME = "lumina_biometrics_prefs"
        private const val KEY_ENROLLED_VECTOR = "enrolled_voice_vector"
        private const val KEY_IS_ENROLLED = "is_voice_enrolled"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isEnrolled = MutableStateFlow(prefs.getBoolean(KEY_IS_ENROLLED, false))
    val isEnrolled: StateFlow<Boolean> = _isEnrolled.asStateFlow()

    private val _enrollmentStep = MutableStateFlow(0) // 0: Idle, 1: Step 1, 2: Step 2, 3: Step 3, 4: Complete
    val enrollmentStep: StateFlow<Int> = _enrollmentStep.asStateFlow()

    private val _lastSimilarity = MutableStateFlow(0.88f)
    val lastSimilarity: StateFlow<Float> = _lastSimilarity.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _biometricsLog = MutableStateFlow("Voice Biometrics Engine Ready")
    val biometricsLog: StateFlow<String> = _biometricsLog.asStateFlow()

    private var enrolledProfileVector: FloatArray? = null
    private val calibrationSamples = mutableListOf<FloatArray>()

    init {
        loadEnrolledVector()
    }

    private fun loadEnrolledVector() {
        val saved = prefs.getString(KEY_ENROLLED_VECTOR, null)
        if (!saved.isNullOrBlank()) {
            try {
                val values = saved.split(",").map { it.toFloat() }.toFloatArray()
                if (values.size == VECTOR_DIM) {
                    enrolledProfileVector = values
                    _isEnrolled.value = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing enrolled vector", e)
            }
        }
    }

    private fun saveEnrolledVector(vector: FloatArray) {
        val str = vector.joinToString(",") { it.toString() }
        prefs.edit()
            .putString(KEY_ENROLLED_VECTOR, str)
            .putBoolean(KEY_IS_ENROLLED, true)
            .apply()
        enrolledProfileVector = vector
        _isEnrolled.value = true
    }

    /**
     * Records audio PCM for [durationMs] and extracts acoustic MFCC/Mel-scale frequency feature vector.
     */
    @SuppressLint("MissingPermission")
    suspend fun recordAndExtractFeatureVector(durationMs: Long = 1800): FloatArray? = withContext(Dispatchers.IO) {
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            .coerceAtLeast(SAMPLE_RATE * 2)

        var recorder: AudioRecord? = null
        val recordedShorts = mutableListOf<Short>()

        try {
            _isRecording.value = true
            recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                _biometricsLog.value = "AudioRecord initialization failed."
                return@withContext null
            }

            recorder.startRecording()
            val tempBuffer = ShortArray(1024)
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < durationMs) {
                val read = recorder.read(tempBuffer, 0, tempBuffer.size)
                if (read > 0) {
                    for (i in 0 until read) {
                        recordedShorts.add(tempBuffer[i])
                    }
                }
            }

            recorder.stop()
            _isRecording.value = false

            if (recordedShorts.size < FRAME_SIZE) {
                _biometricsLog.value = "Insufficient audio captured."
                return@withContext null
            }

            // Convert to Float normalized [-1.0, 1.0]
            val floatAudio = FloatArray(recordedShorts.size) { i ->
                recordedShorts[i].toFloat() / 32768.0f
            }

            // Compute Mel-frequency feature vector
            return@withContext extractMelFeatureVector(floatAudio)

        } catch (e: Exception) {
            _biometricsLog.value = "Recording error: ${e.localizedMessage}"
            return@withContext null
        } finally {
            _isRecording.value = false
            try {
                recorder?.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /**
     * 3-Step Guided Voice Biometric Calibration for Flaxy
     */
    fun startEnrollmentCalibration(onStepDone: (step: Int, success: Boolean) -> Unit) {
        scope.launch {
            val currentStep = _enrollmentStep.value
            val targetStep = if (currentStep >= 3) 1 else currentStep + 1
            _enrollmentStep.value = targetStep
            _biometricsLog.value = "Calibrating Step $targetStep/3: Speak phrase clearly..."

            val featureVector = recordAndExtractFeatureVector(2000)
            if (featureVector != null) {
                if (targetStep == 1) calibrationSamples.clear()
                calibrationSamples.add(featureVector)
                _biometricsLog.value = "Step $targetStep/3 Calibrated Successfully."

                if (targetStep == 3) {
                    // Average the 3 vectors to construct Gold Standard speaker embedding
                    val masterVector = FloatArray(VECTOR_DIM)
                    for (i in 0 until VECTOR_DIM) {
                        var sum = 0f
                        for (sample in calibrationSamples) {
                            sum += sample[i]
                        }
                        masterVector[i] = sum / calibrationSamples.size
                    }
                    saveEnrolledVector(masterVector)
                    _enrollmentStep.value = 4
                    _biometricsLog.value = "Voice Profile Enrolled for Flaxy! Biometrics Active."
                    onStepDone(3, true)
                } else {
                    onStepDone(targetStep, true)
                }
            } else {
                _biometricsLog.value = "Calibration Step $targetStep Failed. Retry."
                onStepDone(targetStep, false)
            }
        }
    }

    /**
     * Verifies if input audio matches enrolled user profile using Cosine Similarity.
     */
    suspend fun verifyLiveSpeaker(durationMs: Long = 1500, threshold: Float = 0.75f): Pair<Boolean, Float> = withContext(Dispatchers.IO) {
        val enrolled = enrolledProfileVector
        if (enrolled == null) {
            // Default allow if not yet enrolled
            return@withContext Pair(true, 0.90f)
        }

        val testVector = recordAndExtractFeatureVector(durationMs)
        if (testVector == null) {
            return@withContext Pair(false, 0.0f)
        }

        val similarity = computeCosineSimilarity(enrolled, testVector)
        _lastSimilarity.value = similarity
        val isVerified = similarity >= threshold
        _biometricsLog.value = "Speaker match: ${(similarity * 100).toInt()}% (Threshold: ${(threshold * 100).toInt()}%)"

        return@withContext Pair(isVerified, similarity)
    }

    /**
     * Mathematical Cosine Similarity calculation between vectors A and B
     */
    fun computeCosineSimilarity(vectorA: FloatArray, vectorB: FloatArray): Float {
        if (vectorA.size != vectorB.size || vectorA.isEmpty()) return 0f
        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0

        for (i in vectorA.indices) {
            dotProduct += vectorA[i] * vectorB[i]
            normA += vectorA[i] * vectorA[i]
            normB += vectorB[i] * vectorB[i]
        }

        val denominator = (sqrt(normA) * sqrt(normB))
        if (denominator <= 0.000001) return 0f
        val cosSim = (dotProduct / denominator).toFloat()
        return cosSim.coerceIn(0.0f, 1.0f)
    }

    /**
     * Extracts a 20-dimensional Mel-Frequency spectral acoustic energy vector from raw PCM
     */
    private fun extractMelFeatureVector(audio: FloatArray): FloatArray {
        val numFrames = (audio.size - FRAME_SIZE) / HOP_SIZE
        if (numFrames <= 0) return FloatArray(VECTOR_DIM) { 0.1f }

        val bankEnergies = FloatArray(NUM_MEL_BANKS)
        val hamming = FloatArray(FRAME_SIZE) { i ->
            (0.54 - 0.46 * cos(2 * PI * i / (FRAME_SIZE - 1))).toFloat()
        }

        for (f in 0 until numFrames) {
            val offset = f * HOP_SIZE
            val frame = FloatArray(FRAME_SIZE) { i -> audio[offset + i] * hamming[i] }

            // FFT power spectrum calculation
            val powerSpectrum = computePowerSpectrum(frame)

            // Accumulate filter bank energy
            val bankSize = powerSpectrum.size / NUM_MEL_BANKS
            for (b in 0 until NUM_MEL_BANKS) {
                var bandSum = 0f
                val start = b * bankSize
                val end = ((b + 1) * bankSize).coerceAtMost(powerSpectrum.size)
                for (k in start until end) {
                    bandSum += powerSpectrum[k]
                }
                bankEnergies[b] += ln(bandSum.coerceAtLeast(0.0001f))
            }
        }

        // Normalize across frames
        val featureVector = FloatArray(VECTOR_DIM)
        for (i in 0 until VECTOR_DIM) {
            featureVector[i] = bankEnergies[i] / numFrames
        }

        // L2 Unit Normalization for robust Cosine Comparison
        var sumSq = 0f
        for (v in featureVector) sumSq += v * v
        val norm = sqrt(sumSq).coerceAtLeast(0.0001f)
        for (i in featureVector.indices) {
            featureVector[i] /= norm
        }

        return featureVector
    }

    private fun computePowerSpectrum(frame: FloatArray): FloatArray {
        val n = frame.size
        val half = n / 2
        val power = FloatArray(half)

        // Real Discrete Fourier Transform approximation for frame
        for (k in 0 until half) {
            var real = 0.0
            var imag = 0.0
            val omega = 2.0 * PI * k / n
            for (t in 0 until n) {
                real += frame[t] * cos(omega * t)
                imag -= frame[t] * sin(omega * t)
            }
            power[k] = ((real * real + imag * imag) / n).toFloat()
        }
        return power
    }

    fun resetEnrollment() {
        prefs.edit().clear().apply()
        enrolledProfileVector = null
        calibrationSamples.clear()
        _isEnrolled.value = false
        _enrollmentStep.value = 0
        _biometricsLog.value = "Voice profile reset."
    }
}
