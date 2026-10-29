package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.KioskPreferences
import com.example.model.PaymentRecord
import com.example.security.KioskSecurityManager
import com.example.ui.PinPurpose
import com.example.ui.components.AutomotiveBrandTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kiosk Launcher", appName)
  }

  @Test
  fun `test 8-digit PIN verification and update`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = KioskPreferences(context)
    val securityManager = KioskSecurityManager(context, prefs)

    // Default PIN is 12345678
    assertTrue(securityManager.verifyPin("12345678"))
    assertFalse(securityManager.verifyPin("00000000"))
    assertFalse(securityManager.verifyPin("1234"))

    // Update PIN
    val updated = securityManager.updatePin("87654321")
    assertTrue(updated)
    assertTrue(securityManager.verifyPin("87654321"))
    assertFalse(securityManager.verifyPin("12345678"))

    // Must be 8 digits
    val invalidShort = securityManager.updatePin("123")
    assertFalse(invalidShort)
  }

  @Test
  fun `test OTP challenge with random words`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = KioskPreferences(context)
    val securityManager = KioskSecurityManager(context, prefs)

    val challenge = securityManager.createOtpChallenge()
    assertNotNull(challenge)
    assertTrue(challenge.challengeWords.contains("-"))
    assertEquals(6, challenge.otpCode.length)
    assertTrue(challenge.otpCode.all { it.isDigit() })

    // Verify OTP
    assertTrue(securityManager.verifyOtp(challenge.otpCode))
    assertFalse(securityManager.verifyOtp("000000"))
  }

  @Test
  fun `test payment record persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = KioskPreferences(context)

    val record = PaymentRecord(
      id = "test-1",
      amount = 25.0,
      currency = "$",
      timestamp = System.currentTimeMillis(),
      formattedTime = "12:00:00 PM",
      announcementText = "$25.00 received"
    )
    prefs.addPaymentRecord(record)

    val history = prefs.getPaymentHistory()
    assertTrue(history.isNotEmpty())
    assertEquals(25.0, history[0].amount, 0.01)
    assertEquals("$25.00 received", history[0].announcementText)
  }

  @Test
  fun `test HUD automotive themes and exit uninstall password protection`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = KioskPreferences(context)

    // Verify HUD theme saving
    prefs.hudTheme = AutomotiveBrandTheme.TATA_EV.name
    assertEquals("TATA_EV", prefs.hudTheme)

    prefs.hudTheme = AutomotiveBrandTheme.TESLA.name
    assertEquals("TESLA", prefs.hudTheme)

    // Verify PinPurpose values
    assertEquals(PinPurpose.ADMIN_ACCESS, PinPurpose.valueOf("ADMIN_ACCESS"))
    assertEquals(PinPurpose.EXIT_KIOSK, PinPurpose.valueOf("EXIT_KIOSK"))
    assertEquals(PinPurpose.UNINSTALL_APP, PinPurpose.valueOf("UNINSTALL_APP"))
  }
}
