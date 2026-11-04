package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.KioskPreferences
import com.example.model.PaymentRecord
import com.example.security.KioskSecurityManager
import com.example.security.SmsRecoveryReceiver
import com.example.soundbox.PaymentNotificationListenerService
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
@Config(sdk = [35])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kiosk Launcher", appName)
  }

  @Test
  fun `test GPay payment notification amount parsing`() {
    // Exact notifications from user's screenshot
    val text1 = "SURANYA G paid you ₹110.00 Tap to view."
    val text2 = "SHIJIMOL NELLENGARA paid you ₹182.00 Aysha"
    val text3 = "TANAY SARKAR paid you ₹68.00 Payment from PhonePe"
    val text4 = "Received ₹500.00 on Paytm"

    val amt1 = PaymentNotificationListenerService.parsePaymentAmount(text1)
    val amt2 = PaymentNotificationListenerService.parsePaymentAmount(text2)
    val amt3 = PaymentNotificationListenerService.parsePaymentAmount(text3)
    val amt4 = PaymentNotificationListenerService.parsePaymentAmount(text4)

    assertEquals(110.0, amt1 ?: 0.0, 0.01)
    assertEquals(182.0, amt2 ?: 0.0, 0.01)
    assertEquals(68.0, amt3 ?: 0.0, 0.01)
    assertEquals(500.0, amt4 ?: 0.0, 0.01)
  }

  @Test
  fun `test strictly ignore promotional advertisements and debits with currency symbol`() {
    // Advertisement / marketing notifications that must NEVER be announced
    val adText1 = "Flat ₹50 cashback on your mobile recharge!"
    val adText2 = "Special offer! Save ₹100 on your next grocery order with coupon code"
    val adText3 = "Pre-approved personal loan of ₹5,00,000 at low interest rate"
    val adText4 = "Earn up to ₹200 scratch card on bill pay"
    val debitText1 = "₹250.00 debited from your A/C XX1234 for Swiggy"
    val debitText2 = "Paid to Merchant Store ₹75.00"

    org.junit.Assert.assertNull(PaymentNotificationListenerService.parsePaymentAmount(adText1))
    org.junit.Assert.assertNull(PaymentNotificationListenerService.parsePaymentAmount(adText2))
    org.junit.Assert.assertNull(PaymentNotificationListenerService.parsePaymentAmount(adText3))
    org.junit.Assert.assertNull(PaymentNotificationListenerService.parsePaymentAmount(adText4))
    org.junit.Assert.assertNull(PaymentNotificationListenerService.parsePaymentAmount(debitText1))
    org.junit.Assert.assertNull(PaymentNotificationListenerService.parsePaymentAmount(debitText2))

    // Genuine bank credit must be announced
    val bankCredit = "Your A/C XX9876 is credited with ₹450.00 on 04-Nov-26 by UPI ref 123456"
    val creditAmt = PaymentNotificationListenerService.parsePaymentAmount(bankCredit)
    assertEquals(450.0, creditAmt ?: 0.0, 0.01)
  }

  @Test
  fun `test account activation and preferences`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = KioskPreferences(context)

    prefs.accountName = "Jiyad"
    prefs.primaryPhone = "+91 9876543210"
    prefs.altPhone = "+91 9123456789"
    prefs.pin = "88776655"
    prefs.isActivated = true

    assertEquals("Jiyad", prefs.accountName)
    assertEquals("+91 9876543210", prefs.primaryPhone)
    assertEquals("+91 9123456789", prefs.altPhone)
    assertEquals("88776655", prefs.pin)
    assertTrue(prefs.isActivated)
  }

  @Test
  fun `test two random words recovery and 3 hour timelock`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = KioskPreferences(context)
    val securityManager = KioskSecurityManager(context, prefs)

    prefs.altPhone = "+91 9123456789"

    // Generate 2 random words
    val (w1, w2) = securityManager.generateTwoRandomWords()
    assertNotNull(w1)
    assertNotNull(w2)
    assertTrue(w1.isNotEmpty() && w2.isNotEmpty())
    assertFalse(w1 == w2)

    // Initially recovery is not verified and cannot change password
    assertFalse(securityManager.isRecoveryVerified())
    assertFalse(securityManager.canChangePasswordNow())

    // Invalid sender should fail
    val wrongSenderResult = securityManager.verifyTwoWordsFromAlt("+91 0000000000", "$w1 $w2")
    assertFalse(wrongSenderResult)

    // Valid sender with matching words should succeed
    val validResult = securityManager.verifyTwoWordsFromAlt("+91 9123456789", "Here are the words $w1 and $w2")
    assertTrue(validResult)
    assertTrue(securityManager.isRecoveryVerified())

    // 3-hour timelock is now active; cannot change immediately
    assertFalse(securityManager.canChangePasswordNow())
    val remaining = securityManager.getRemainingLockTimeMillis()
    assertTrue(remaining > 0L)
    assertTrue(remaining <= 3 * 3600 * 1000L)
  }

  @Test
  fun `test phone number matching helper`() {
    assertTrue(SmsRecoveryReceiver.matchesPhoneNumber("+91 9123456789", "9123456789"))
    assertTrue(SmsRecoveryReceiver.matchesPhoneNumber("9123456789", "+91-91234-56789"))
    assertFalse(SmsRecoveryReceiver.matchesPhoneNumber("9876543210", "9123456789"))
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
  fun `test Tata Punch EV default theme selection`() {
    val defaultTheme = AutomotiveBrandTheme.TATA_PUNCH_EV
    assertEquals("Tata Punch EV 2026", defaultTheme.displayName)
    assertNotNull(defaultTheme.primaryColor)
    assertNotNull(defaultTheme.accentColor)
  }

  @Test
  fun `test power unplug policy configuration`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = KioskPreferences(context)

    prefs.sleepOnUnplugged = true
    prefs.autoDisplayOnPlugged = true
    assertTrue(prefs.sleepOnUnplugged)
    assertTrue(prefs.autoDisplayOnPlugged)
  }

  @Test
  fun `test blacklist enforcement denies blacklisted apps when activated`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = KioskPreferences(context)
    val appManager = com.example.apps.AppManager(context, prefs)

    val testPkg = "com.unwanted.game"
    prefs.isActivated = true
    prefs.allowedPackages = setOf("com.google.android.apps.maps", "com.ubercab.driver")
    prefs.blacklistedPackages = setOf(testPkg)

    // Allowed apps return true
    assertTrue(appManager.isPackageAllowed("com.google.android.apps.maps"))
    assertTrue(appManager.isPackageAllowed(context.packageName)) // self is always allowed

    // Blacklisted app is strictly denied
    assertFalse(appManager.isPackageAllowed(testPkg))

    // Non-whitelisted app is also denied
    assertFalse(appManager.isPackageAllowed("com.random.unapproved"))

    // Toggling blacklist updates preferences
    appManager.toggleAppBlacklisted("com.random.unapproved", true)
    assertTrue(prefs.blacklistedPackages.contains("com.random.unapproved"))
    assertFalse(appManager.isPackageAllowed("com.random.unapproved"))
  }

  @Test
  fun `test Soundbox voice gender and tone configurations`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = KioskPreferences(context)

    // Check default voice gender and tone
    assertEquals("MALE", prefs.voiceGender)
    assertEquals("PAYTM_TRIAD", prefs.soundboxTone)

    // Verify all VoiceGender entries
    assertEquals(2, com.example.model.VoiceGender.entries.size)
    val male = com.example.model.VoiceGender.MALE
    val female = com.example.model.VoiceGender.FEMALE
    assertEquals("Male Voice", male.displayName)
    assertEquals("Female Voice", female.displayName)

    // Verify all SoundboxTone entries
    assertEquals(5, com.example.model.SoundboxTone.entries.size)
    val paytm = com.example.model.SoundboxTone.PAYTM_TRIAD
    val phonepe = com.example.model.SoundboxTone.PHONEPE_DING
    val gpay = com.example.model.SoundboxTone.GPAY_MARIMBA
    val beep = com.example.model.SoundboxTone.CLASSIC_BEEP
    val harp = com.example.model.SoundboxTone.CRYSTAL_HARP

    assertEquals("Paytm / UPI Triad", paytm.displayName)
    assertEquals("PhonePe Double Ding", phonepe.displayName)
    assertEquals("Google Pay Warm Chime", gpay.displayName)
    assertEquals("Soundbox Sharp Beep", beep.displayName)
    assertEquals("Crystal 4-Tone Harp", harp.displayName)

    // Verify SoundboxTone.fromId lookup
    assertEquals(paytm, com.example.model.SoundboxTone.fromId("PAYTM_TRIAD"))
    assertEquals(phonepe, com.example.model.SoundboxTone.fromId("PHONEPE_DING"))
    assertEquals(gpay, com.example.model.SoundboxTone.fromId("GPAY_MARIMBA"))
    assertEquals(beep, com.example.model.SoundboxTone.fromId("CLASSIC_BEEP"))
    assertEquals(harp, com.example.model.SoundboxTone.fromId("CRYSTAL_HARP"))
    assertEquals(paytm, com.example.model.SoundboxTone.fromId("UNKNOWN_TONE"))

    // Test saving preferences
    prefs.voiceGender = female.name
    prefs.soundboxTone = phonepe.id
    assertEquals("FEMALE", prefs.voiceGender)
    assertEquals("PHONEPE_DING", prefs.soundboxTone)

    // Verify SpeechEngine entries
    assertEquals(3, com.example.model.SpeechEngine.entries.size)
    val naturalCloud = com.example.model.SpeechEngine.NATURAL_CLOUD
    val googleServices = com.example.model.SpeechEngine.GOOGLE_SPEECH_SERVICES
    val systemTts = com.example.model.SpeechEngine.SYSTEM_TTS

    assertEquals("NATURAL_CLOUD", prefs.speechEngine)
    assertEquals(naturalCloud, com.example.model.SpeechEngine.fromId("NATURAL_CLOUD"))
    assertEquals(googleServices, com.example.model.SpeechEngine.fromId("GOOGLE_SPEECH_SERVICES"))
    assertEquals(systemTts, com.example.model.SpeechEngine.fromId("SYSTEM_TTS"))
    assertEquals(naturalCloud, com.example.model.SpeechEngine.fromId("INVALID"))

    prefs.speechEngine = googleServices.id
    assertEquals("GOOGLE_SPEECH_SERVICES", prefs.speechEngine)
  }
}
