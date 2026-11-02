package com.example.model

enum class VoiceGender(
    val displayName: String,
    val subtitle: String
) {
    MALE(
        displayName = "Male Voice",
        subtitle = "Deep authoritative merchant soundbox voice"
    ),
    FEMALE(
        displayName = "Female Voice",
        subtitle = "Clear, melodic assistant voice"
    )
}

enum class SoundboxTone(
    val id: String,
    val displayName: String,
    val subtitle: String
) {
    PAYTM_TRIAD(
        id = "PAYTM_TRIAD",
        displayName = "Paytm / UPI Triad",
        subtitle = "3-tone melodic bell chime (C5 → E5 → G5)"
    ),
    PHONEPE_DING(
        id = "PHONEPE_DING",
        displayName = "PhonePe Double Ding",
        subtitle = "Crisp high dual-bell tone (E5 → B5)"
    ),
    GPAY_MARIMBA(
        id = "GPAY_MARIMBA",
        displayName = "Google Pay Warm Chime",
        subtitle = "Bright harmonious chord chime (G5 → C6)"
    ),
    CLASSIC_BEEP(
        id = "CLASSIC_BEEP",
        displayName = "Soundbox Sharp Beep",
        subtitle = "High-contrast punchy alert beep (980 Hz)"
    ),
    CRYSTAL_HARP(
        id = "CRYSTAL_HARP",
        displayName = "Crystal 4-Tone Harp",
        subtitle = "Ascending luxury EV chime (C5 → G5 → C6 → E6)"
    );

    companion object {
        fun fromId(id: String): SoundboxTone {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: PAYTM_TRIAD
        }
    }
}

enum class SpeechEngine(
    val id: String,
    val displayName: String,
    val subtitle: String
) {
    NATURAL_CLOUD(
        id = "NATURAL_CLOUD",
        displayName = "Google Natural Soundbox API",
        subtitle = "Ultra-crisp human studio speech (Natural neural voice with automatic offline fallback)"
    ),
    GOOGLE_SPEECH_SERVICES(
        id = "GOOGLE_SPEECH_SERVICES",
        displayName = "Google Speech Services (On-Device)",
        subtitle = "Explicit high-quality Google TTS engine (com.google.android.tts)"
    ),
    SYSTEM_TTS(
        id = "SYSTEM_TTS",
        displayName = "System Default Engine",
        subtitle = "Standard device text-to-speech engine"
    );

    companion object {
        fun fromId(id: String): SpeechEngine {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: NATURAL_CLOUD
        }
    }
}

