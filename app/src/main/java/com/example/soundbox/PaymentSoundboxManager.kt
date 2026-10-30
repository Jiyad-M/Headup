package com.example.soundbox

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.KioskPreferences
import com.example.model.PaymentRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.exp
import kotlin.math.sin

class PaymentSoundboxManager(
    private val context: Context,
    private val preferences: KioskPreferences
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val _isAnnouncing = MutableStateFlow(false)
    val isAnnouncing: StateFlow<Boolean> = _isAnnouncing.asStateFlow()

    private val _lastAnnouncedText = MutableStateFlow("")
    val lastAnnouncedText: StateFlow<String> = _lastAnnouncedText.asStateFlow()

    private val _activePaymentAlert = MutableStateFlow<PaymentRecord?>(null)
    val activePaymentAlert: StateFlow<PaymentRecord?> = _activePaymentAlert.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("Soundbox", "Error initializing TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            configureMaleVoice()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    coroutineScope.launch {
                        delay(1200)
                        _isAnnouncing.value = false
                        delay(2500)
                        _activePaymentAlert.value = null
                    }
                }
                override fun onError(utteranceId: String?) {
                    _isAnnouncing.value = false
                }
            })
            isTtsReady = true
        }
    }

    /**
     * Configures a distinct, authoritative male voice for realistic Soundbox behavior.
     */
    private fun configureMaleVoice() {
        try {
            val voices = tts?.voices
            if (!voices.isNullOrEmpty()) {
                val currentLocale = tts?.language ?: Locale.getDefault()
                val matchingLocaleVoices = voices.filter { it.locale.language == currentLocale.language }

                val maleVoice = matchingLocaleVoices.find { voice ->
                    val name = voice.name.lowercase(Locale.ROOT)
                    (name.contains("male") && !name.contains("female")) ||
                            name.contains("-m-") ||
                            name.contains("man") ||
                            name.contains("#male")
                } ?: voices.find { voice ->
                    val name = voice.name.lowercase(Locale.ROOT)
                    (name.contains("male") && !name.contains("female")) ||
                            name.contains("-m-") ||
                            name.contains("#male")
                } ?: matchingLocaleVoices.firstOrNull { !it.isNetworkConnectionRequired }

                if (maleVoice != null) {
                    tts?.voice = maleVoice
                    Log.d("Soundbox", "Selected male voice: ${maleVoice.name}")
                }
            }
        } catch (e: Exception) {
            Log.w("Soundbox", "Error selecting male voice", e)
        }

        // Deep authoritative male pitch (0.78f) and natural pacing (0.92f)
        tts?.setPitch(0.78f)
        tts?.setSpeechRate(0.92f)
    }

    /**
     * Announces received payment with realistic Soundbox chime followed by clear male voice announcement:
     * e.g. [Realistic Melodic Chime] -> "One hundred ten rupees received"
     */
    fun announcePayment(
        amount: Double,
        currencySymbol: String = preferences.currency,
        appName: String = "Google Pay",
        onComplete: ((PaymentRecord) -> Unit)? = null
    ) {
        coroutineScope.launch {
            _isAnnouncing.value = true

            val currencyWord = getCurrencyWord(currencySymbol, amount)
            val formattedAmount = if (amount % 1.0 == 0.0) {
                "${amount.toInt()}"
            } else {
                String.format(Locale.US, "%.2f", amount)
            }

            val displayAnnouncement = "$currencySymbol$formattedAmount received"
            _lastAnnouncedText.value = displayAnnouncement

            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val record = PaymentRecord(
                id = UUID.randomUUID().toString(),
                amount = amount,
                currency = currencySymbol,
                timestamp = System.currentTimeMillis(),
                formattedTime = timeFormat.format(Date()),
                announcementText = displayAnnouncement
            )
            preferences.addPaymentRecord(record)
            _activePaymentAlert.value = record
            onComplete?.invoke(record)

            // 1. Play Realistic Soundbox 3-Tone Melodic Chime (C5 -> E5 -> G5)
            playRealisticSoundboxChime()

            // Short pause between chime and speech
            delay(550)

            // 2. Speak the amount in deep male voice
            configureMaleVoice()
            val speechText = "$formattedAmount $currencyWord received"

            if (isTtsReady && tts != null) {
                tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "PAYMENT_${System.currentTimeMillis()}")
            } else {
                delay(3000)
                _isAnnouncing.value = false
                delay(2000)
                _activePaymentAlert.value = null
            }
        }
    }

    fun dismissActiveAlert() {
        _activePaymentAlert.value = null
    }

    /**
     * Generates a high-fidelity 3-tone chime (523Hz C5 -> 659Hz E5 -> 784Hz G5)
     * with exponential bell decay curve, mimicking official merchant soundboxes.
     */
    private fun playRealisticSoundboxChime() {
        Thread {
            try {
                val sampleRate = 44100
                // Chime notes: C5 (523.25Hz, 140ms), E5 (659.25Hz, 140ms), G5 (783.99Hz, 260ms)
                val notes = listOf(
                    Pair(523.25, 0.14),
                    Pair(659.25, 0.14),
                    Pair(783.99, 0.26)
                )

                var totalSamples = 0
                for (note in notes) {
                    totalSamples += (note.second * sampleRate).toInt()
                }

                val pcmSamples = ShortArray(totalSamples)
                var offset = 0

                for (note in notes) {
                    val freq = note.first
                    val durSec = note.second
                    val noteSamples = (durSec * sampleRate).toInt()

                    for (i in 0 until noteSamples) {
                        val t = i.toDouble() / sampleRate
                        // Bell decay envelope
                        val envelope = exp(-4.0 * (t / durSec))
                        // Harmonic richness (fundamental + soft 2nd harmonic)
                        val sample = (sin(2.0 * Math.PI * freq * t) * 0.8 +
                                sin(4.0 * Math.PI * freq * t) * 0.2) * envelope

                        pcmSamples[offset + i] = (sample * Short.MAX_VALUE * 0.95).toInt().toShort()
                    }
                    offset += noteSamples
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcmSamples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(pcmSamples, 0, pcmSamples.size)
                audioTrack.play()
                Thread.sleep(580)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                Log.w("Soundbox", "AudioTrack chime failed, falling back to tone gen", e)
                try {
                    val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                    tone.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
                    mainHandler.postDelayed({ tone.release() }, 400)
                } catch (_: Exception) {}
            }
        }.start()
    }

    private fun getCurrencyWord(symbol: String, amount: Double): String {
        return when (symbol) {
            "$" -> if (amount == 1.0) "dollar" else "dollars"
            "₹" -> if (amount == 1.0) "rupee" else "rupees"
            "€" -> if (amount == 1.0) "euro" else "euros"
            "£" -> if (amount == 1.0) "pound" else "pounds"
            "¥" -> "yen"
            "₩" -> "won"
            else -> symbol
        }
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
    }
}
