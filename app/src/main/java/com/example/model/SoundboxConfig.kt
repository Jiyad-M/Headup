package com.example.model

enum class VoiceGender(
    val displayName: String,
    val subtitle: String
) {
    MALE(
        displayName = "Google Voice (Male)",
        subtitle = "Deep authoritative Google merchant soundbox voice"
    ),
    FEMALE(
        displayName = "Google Voice (Female)",
        subtitle = "Clear melodious Google assistant voice"
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
    BHIM_UPI(
        id = "BHIM_UPI",
        displayName = "BHIM UPI Symphony",
        subtitle = "Resonant 4-tone banking chime (D5 → F#5 → A5 → D6)"
    ),
    AMAZON_PAY(
        id = "AMAZON_PAY",
        displayName = "Amazon Pay Dual Chime",
        subtitle = "Crisp high-octave alert (A5 → E6)"
    ),
    AIRTEL_PAY(
        id = "AIRTEL_PAY",
        displayName = "Airtel Payments Tone",
        subtitle = "Triple rhythmic pulse chime (E5 → G5 → C6)"
    ),
    TATA_EV_CHIME(
        id = "TATA_EV_CHIME",
        displayName = "Tata EV Futuristic Chime",
        subtitle = "Luxury electric vehicle chord (A4 → C#5 → E5 → A5)"
    ),
    CRYSTAL_HARP(
        id = "CRYSTAL_HARP",
        displayName = "Crystal 4-Tone Harp",
        subtitle = "Ascending luxury EV chime (C5 → G5 → C6 → E6)"
    ),
    SONIC_RADAR(
        id = "SONIC_RADAR",
        displayName = "Sonic Pulse Alert",
        subtitle = "Penetrating cockpit sonar sweep (1200 Hz → 1500 Hz)"
    ),
    CLASSIC_BEEP(
        id = "CLASSIC_BEEP",
        displayName = "Soundbox Sharp Beep",
        subtitle = "High-contrast punchy alert beep (980 Hz)"
    ),
    CASH_REGISTER(
        id = "CASH_REGISTER",
        displayName = "Cash Register Cha-Ching",
        subtitle = "Metallic dual register bell (1400 Hz → 2100 Hz)"
    );

    companion object {
        fun fromId(id: String): SoundboxTone {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: PAYTM_TRIAD
        }
    }
}

/**
 * Speech Engine: Google Voice held exclusively (non-Google engines removed as requested).
 * High-definition Google Neural Studio speech with automatic fallback to Google Speech Services.
 */
enum class SpeechEngine(
    val id: String,
    val displayName: String,
    val subtitle: String
) {
    GOOGLE_VOICE(
        id = "GOOGLE_VOICE",
        displayName = "Google Voice (HD)",
        subtitle = "Google Neural Speech Engine with high-fidelity studio pronunciation"
    );

    companion object {
        fun fromId(id: String): SpeechEngine {
            return GOOGLE_VOICE
        }
    }
}
