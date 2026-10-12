package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.generator.PhoneGenerator
import com.example.model.PhoneNumber
import com.example.model.RarityTier
import com.example.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("СимГен: Блатные Номера", appName)
  }

  @Test
  fun `test numbers and state persistence across game restarts`() {
    val app = ApplicationProvider.getApplicationContext<Application>()

    // First session: player plays, rolls a high value number and saves to vault
    val vmSession1 = GameViewModel(app)
    val startingBalance = vmSession1.balance.value

    // Perform a roll
    vmSession1.rollSingle()
    val rolledNumber = vmSession1.currentNumber.value
    assertNotNull(rolledNumber)

    // Save to Vault
    val saved = vmSession1.saveCurrentToVault()
    assertTrue("Number should be successfully saved to vault", saved)

    val session1Vault = vmSession1.inventory.value
    assertTrue("Vault must contain the saved number", session1Vault.any { it.formatted == rolledNumber?.formatted })
    val session1Xp = vmSession1.playerXp.value
    val session1Level = vmSession1.levelInfo.value.level
    val lastCurrentFormatted = vmSession1.currentNumber.value?.formatted

    // Simulating app restart / process recreation:
    // Create a new ViewModel instance from the same Application context
    val vmSession2 = GameViewModel(app)

    // Verify inventory persistence across restart
    val session2Vault = vmSession2.inventory.value
    assertTrue(
        "Vault in new session must restore numbers saved in previous session! Expected: ${rolledNumber?.formatted}",
        session2Vault.any { it.formatted == rolledNumber?.formatted }
    )

    val restoredNumber = session2Vault.first { it.formatted == rolledNumber?.formatted }
    assertEquals(rolledNumber?.digits, restoredNumber.digits)
    assertEquals(rolledNumber?.rarity, restoredNumber.rarity)
    assertEquals(rolledNumber?.value, restoredNumber.value)

    // Verify current screen number is also preserved across restart
    assertEquals(lastCurrentFormatted, vmSession2.currentNumber.value?.formatted)

    // Verify player XP and level are preserved across restart
    assertEquals(session1Xp, vmSession2.playerXp.value)
    assertEquals(session1Level, vmSession2.levelInfo.value.level)
  }

  @Test
  fun `test phone number JSON serialization in Android runtime`() {
    val original = PhoneGenerator.generate(mvdInsiderLevel = 1, goldenDbLevel = 1, playerLevel = 3)
    val json = original.toJson()
    assertNotNull(json)
    assertTrue(json.contains(original.digits))

    val parsed = PhoneNumber.fromJson(json)
    assertNotNull(parsed)
    assertEquals(original.id, parsed?.id)
    assertEquals(original.formatted, parsed?.formatted)
    assertEquals(original.rarity, parsed?.rarity)
    assertEquals(original.value, parsed?.value)
    assertEquals(original.totalMultiplier, parsed?.totalMultiplier ?: 0.0, 0.001)
  }
}
