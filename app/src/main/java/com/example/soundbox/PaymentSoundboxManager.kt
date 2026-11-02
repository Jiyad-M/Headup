package com.example.soundbox

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.KioskPreferences
import com.example.model.PaymentRecord
import com.example.model.SoundboxTone
import com.example.model.SpeechEngine
import com.example.model.VoiceGender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
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
    private var activeMediaPlayer: MediaPlayer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val _isAnnouncing = MutableStateFlow(false)
    val isAnnouncing: StateFlow<Boolean> = _isAnnouncing.asStateFlow()

    private val _lastAnnouncedText = MutableStateFlow("")
    val lastAnnouncedText: StateFlow<String> = _lastAnnouncedText.asStateFlow()

    private val _activePaymentAlert = MutableStateFlow<PaymentRecord?>(null)
    val activePaymentAlert: StateFlow<PaymentRecord?> = _activePaymentAlert.asStateFlow()

    init {
        initTtsEngine(SpeechEngine.fromId(preferences.speechEngine))
    }

    fun initTtsEngine(engine: SpeechEngine) {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        isTtsReady = false

        try {
            when (engine) {
                SpeechEngine.GOOGLE_SPEECH_SERVICES -> {
                    tts = TextToSpeech(context.applicationContext, this, "com.google.android.tts")
                }
                else -> {
                    tts = TextToSpeech(context.applicationContext, this)
                }
            }
        } catch (e: Exception) {
            Log.e("Soundbox", "Error initializing TTS with engine $engine", e)
            try {
                tts = TextToSpeech(context.applicationContext, this)
            } catch (_: Exception) {}
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("en", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            applyConfiguredVoice()
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
     * Applies voice configuration based on user preference (Male vs Female).
     */
    fun applyConfiguredVoice() {
        val gender = try {
            VoiceGender.valueOf(preferences.voiceGender)
        } catch (_: Exception) {
            VoiceGender.MALE
        }
        configureVoice(gender)
    }

    fun configureVoice(gender: VoiceGender) {
        try {
            val voices = tts?.voices
            if (!voices.isNullOrEmpty()) {
                val currentLocale = tts?.language ?: Locale.getDefault()
                val matchingLocaleVoices = voices.filter { it.locale.language == currentLocale.language }

                val selectedVoice = if (gender == VoiceGender.MALE) {
                    matchingLocaleVoices.find { voice ->
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
                } else {
                    matchingLocaleVoices.find { voice ->
                        val name = voice.name.lowercase(Locale.ROOT)
                        name.contains("female") ||
                                name.contains("-f-") ||
                                name.contains("woman") ||
                                name.contains("#female")
                    } ?: voices.find { voice ->
                        val name = voice.name.lowercase(Locale.ROOT)
                        name.contains("female") ||
                                name.contains("-f-") ||
                                name.contains("#female")
                    } ?: matchingLocaleVoices.firstOrNull { !it.isNetworkConnectionRequired }
                }

                if (selectedVoice != null) {
                    tts?.voice = selectedVoice
                    Log.d("Soundbox", "Selected ${gender.name} voice: ${selectedVoice.name}")
                }
            }
        } catch (e: Exception) {
            Log.w("Soundbox", "Error configuring voice for $gender", e)
        }

        if (gender == VoiceGender.MALE) {
            tts?.setPitch(0.78f) // Deep authoritative masculine pitch
            tts?.setSpeechRate(0.92f)
        } else {
            tts?.setPitch(1.15f) // Clear melodic feminine pitch
            tts?.setSpeechRate(0.98f)
        }
    }

    /**
     * Announces received payment with the selected chime tone followed by the selected voice API:
     * e.g. [Selected Tone] -> "110 rupees received on Paytm"
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

            val appSuffix = when {
                appName.contains("paytm", ignoreCase = true) -> " on Paytm"
                appName.contains("phonepe", ignoreCase = true) -> " on PhonePe"
                appName.contains("gpay", ignoreCase = true) || appName.contains("google", ignoreCase = true) -> " on Google Pay"
                else -> ""
            }

            val displayAnnouncement = "$currencySymbol$formattedAmount received$appSuffix"
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

            // 1. Play Configured Chime Tone
            val tone = SoundboxTone.fromId(preferences.soundboxTone)
            playTone(tone)

            // Pause between chime and voice
            delay(520)

            // 2. Speak amount with selected Speech API (Cloud Natural Voice or On-Device TTS)
            val speechText = "$formattedAmount $currencyWord received$appSuffix"
            val gender = try {
                VoiceGender.valueOf(preferences.voiceGender)
            } catch (_: Exception) {
                VoiceGender.MALE
            }
            val engine = SpeechEngine.fromId(preferences.speechEngine)

            speakWithEngine(
                text = speechText,
                gender = gender,
                engine = engine,
                onDone = {
                    coroutineScope.launch {
                        delay(1200)
                        _isAnnouncing.value = false
                        delay(2500)
                        _activePaymentAlert.value = null
                    }
                }
            )
        }
    }

    /**
     * Speaks text using the chosen speech engine:
     * - NATURAL_CLOUD: Google Natural High-Definition Studio Audio via stream & MediaPlayer
     * - GOOGLE_SPEECH_SERVICES / SYSTEM_TTS: On-device TextToSpeech with configured gender
     */
    fun speakWithEngine(
        text: String,
        gender: VoiceGender,
        engine: SpeechEngine = SpeechEngine.fromId(preferences.speechEngine),
        onDone: (() -> Unit)? = null
    ) {
        if (engine == SpeechEngine.NATURAL_CLOUD) {
            coroutineScope.launch {
                val success = playNaturalCloudVoice(text, gender, onDone)
                if (!success) {
                    Log.w("Soundbox", "Natural Cloud Speech failed, falling back to on-device TTS")
                    speakWithLocalTts(text, gender, onDone)
                }
            }
        } else {
            speakWithLocalTts(text, gender, onDone)
        }
    }

    private suspend fun playNaturalCloudVoice(
        text: String,
        gender: VoiceGender,
        onDone: (() -> Unit)?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val cacheKey = md5("$text-$gender")
            val cacheFile = File(context.cacheDir, "tts_voice_$cacheKey.mp3")

            if (!cacheFile.exists() || cacheFile.length() < 100) {
                val encodedText = URLEncoder.encode(text, "UTF-8")
                val langParam = "en-IN" // Authentic Indian soundbox accent
                val ttsUrl = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=$langParam&q=$encodedText"

                val connection = URL(ttsUrl).openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8)")
                connection.connectTimeout = 4000
                connection.readTimeout = 5000
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.use { input ->
                        FileOutputStream(cacheFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                } else {
                    return@withContext false
                }
            }

            if (!cacheFile.exists() || cacheFile.length() < 100) {
                return@withContext false
            }

            withContext(Dispatchers.Main) {
                try {
                    activeMediaPlayer?.stop()
                    activeMediaPlayer?.release()
                } catch (_: Exception) {}

                val mp = MediaPlayer()
                activeMediaPlayer = mp

                mp.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                mp.setDataSource(cacheFile.absolutePath)
                mp.prepare()

                // Apply pitch adjustments for Male vs Female studio voice
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        val params = PlaybackParams()
                        if (gender == VoiceGender.MALE) {
                            params.pitch = 0.84f // Deep authoritative merchant resonance
                            params.speed = 0.94f
                        } else {
                            params.pitch = 1.05f // Crisp melodic feminine clarity
                            params.speed = 1.00f
                        }
                        mp.playbackParams = params
                    } catch (e: Exception) {
                        Log.w("Soundbox", "Error setting playback params", e)
                    }
                }

                mp.setOnCompletionListener {
                    try {
                        it.release()
                        if (activeMediaPlayer == it) activeMediaPlayer = null
                    } catch (_: Exception) {}
                    onDone?.invoke()
                }

                mp.setOnErrorListener { _, _, _ ->
                    onDone?.invoke()
                    true
                }

                mp.start()
            }
            true
        } catch (e: Exception) {
            Log.e("Soundbox", "Error playing Natural Cloud voice", e)
            false
        }
    }

    private fun speakWithLocalTts(
        text: String,
        gender: VoiceGender,
        onDone: (() -> Unit)?
    ) {
        configureVoice(gender)
        if (isTtsReady && tts != null) {
            val utteranceId = "SPEECH_${System.currentTimeMillis()}"
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            mainHandler.postDelayed({ onDone?.invoke() }, 2000)
        }
    }

    /**
     * Previews/speaks a short voice test sample for the selected gender using active speech engine.
     */
    fun testVoicePreview(gender: VoiceGender) {
        val sampleText = if (gender == VoiceGender.MALE) {
            "Male voice active. 150 rupees received on Paytm."
        } else {
            "Female voice active. 150 rupees received on Google Pay."
        }
        val engine = SpeechEngine.fromId(preferences.speechEngine)
        speakWithEngine(sampleText, gender, engine)
    }

    fun dismissActiveAlert() {
        _activePaymentAlert.value = null
    }

    /**
     * Plays a specific Soundbox tone using synthesized PCM sine waves with exponential decay.
     */
    fun playTone(tone: SoundboxTone) {
        Thread {
            try {
                val sampleRate = 44100
                val notes: List<Pair<Double, Double>> = when (tone) {
                    SoundboxTone.PAYTM_TRIAD -> listOf(
                        Pair(523.25, 0.13), // C5
                        Pair(659.25, 0.13), // E5
                        Pair(783.99, 0.25)  // G5
                    )
                    SoundboxTone.PHONEPE_DING -> listOf(
                        Pair(659.25, 0.11), // E5
                        Pair(987.77, 0.28)  // B5
                    )
                    SoundboxTone.GPAY_MARIMBA -> listOf(
                        Pair(783.99, 0.12), // G5
                        Pair(1046.50, 0.26) // C6
                    )
                    SoundboxTone.CLASSIC_BEEP -> listOf(
                        Pair(980.0, 0.22)   // Crisp 980Hz
                    )
                    SoundboxTone.CRYSTAL_HARP -> listOf(
                        Pair(523.25, 0.08), // C5
                        Pair(783.99, 0.08), // G5
                        Pair(1046.50, 0.09), // C6
                        Pair(1318.51, 0.24) // E6
                    )
                }

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
                        val envelope = exp(-3.8 * (t / durSec))
                        val sample = (sin(2.0 * Math.PI * freq * t) * 0.82 +
                                sin(4.0 * Math.PI * freq * t) * 0.18) * envelope

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
                val totalDurationMs = (notes.sumOf { it.second } * 1000).toLong() + 50
                Thread.sleep(totalDurationMs)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                Log.w("Soundbox", "AudioTrack tone playback failed, falling back to tone gen", e)
                try {
                    val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
                    mainHandler.postDelayed({ toneGen.release() }, 400)
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

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digested = md.digest(input.toByteArray())
        return digested.joinToString("") { "%02x".format(it) }
    }

    fun release() {
        try {
            activeMediaPlayer?.stop()
            activeMediaPlayer?.release()
            activeMediaPlayer = null
        } catch (_: Exception) {}

        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {}
    }
}
