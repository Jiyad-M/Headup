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
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
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
            tts?.setSpeechRate(0.95f)
            tts?.setPitch(1.05f)
            isTtsReady = true
        }
    }

    /**
     * Plays the high-clarity notification chime tone followed by the voice announcement.
     * "{Tone}{ payment amount} received"
     */
    fun announcePayment(
        amount: Double,
        currencySymbol: String = preferences.currency,
        onComplete: ((PaymentRecord) -> Unit)? = null
    ) {
        coroutineScope.launch {
            _isAnnouncing.value = true

            // 1. Play Tone (Crisp dual-frequency chime)
            playChimeTone()

            delay(380)

            // 2. Format speech message
            val currencyWord = getCurrencyWord(currencySymbol, amount)
            val formattedAmount = if (amount % 1.0 == 0.0) {
                "${amount.toInt()}"
            } else {
                String.format(Locale.US, "%.2f", amount)
            }

            val speechText = "$formattedAmount $currencyWord received"
            val displayAnnouncement = "$currencySymbol$formattedAmount received"
            _lastAnnouncedText.value = displayAnnouncement

            // 3. Speak via TextToSpeech
            if (isTtsReady && tts != null) {
                tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "PAYMENT_ANNOUNCE_${System.currentTimeMillis()}")
            }

            // 4. Record to preferences history
            val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
            val record = PaymentRecord(
                id = UUID.randomUUID().toString(),
                amount = amount,
                currency = currencySymbol,
                timestamp = System.currentTimeMillis(),
                formattedTime = timeFormat.format(Date()),
                announcementText = displayAnnouncement
            )
            preferences.addPaymentRecord(record)
            onComplete?.invoke(record)

            delay(2200)
            _isAnnouncing.value = false
        }
    }

    private fun playChimeTone() {
        try {
            // Primary tone attempt using ToneGenerator
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 280)
            mainHandler.postDelayed({
                try {
                    toneGen.release()
                } catch (_: Exception) {}
            }, 500)
        } catch (_: Exception) {
            // High fidelity synthesized audio fallback
            playSynthesizedTone()
        }
    }

    private fun playSynthesizedTone() {
        Thread {
            try {
                val sampleRate = 44100
                val durationMs = 280
                val numSamples = (durationMs * sampleRate) / 1000
                val samples = ShortArray(numSamples)
                val freq1 = 880.0 // A5
                val freq2 = 1318.51 // E6 (cheerful high chime)

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    val envelope = 1.0 - (i.toDouble() / numSamples)
                    val sampleVal = ((sin(2.0 * Math.PI * freq1 * time) + sin(2.0 * Math.PI * freq2 * time)) / 2.0) * envelope
                    samples[i] = (sampleVal * Short.MAX_VALUE * 0.85).toInt().toShort()
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
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()
                Thread.sleep(durationMs.toLong() + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                Log.w("Soundbox", "AudioTrack tone fallback failed", e)
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
