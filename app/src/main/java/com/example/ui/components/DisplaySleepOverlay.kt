package com.example.ui.components

import androidx.compose.runtime.Composable

/**
 * Deprecated / removed in favor of real hardware display shutoff via DevicePolicyManager.lockNow().
 * Kept as empty composable for API backward compatibility.
 */
@Composable
fun DisplaySleepOverlay(
    isCompletelyOff: Boolean,
    countdownSec: Int?,
    onWakeDisplay: () -> Unit = {}
) {
    // No-op: Hardware display turns off directly via Device Admin
}
