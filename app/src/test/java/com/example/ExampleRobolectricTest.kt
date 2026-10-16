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

  @Test
  fun `test stats persistence across ViewModel recreations`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm1 = GameViewModel(app)

    vm1.rollSingle()
    val rollStats1 = vm1.stats.value
    assertTrue("Total rolls should be at least 1", rollStats1.totalRolls >= 1)
    assertTrue("Rarity counts should be tracked", rollStats1.rarityCounts.isNotEmpty())
    assertNotNull("Best number should be tracked", rollStats1.bestNumber)

    // Sell a non-vaulted number to increase totalEarned
    val numToSell = vm1.inventory.value.firstOrNull { !it.isInVault }
    if (numToSell != null) {
      vm1.sellFromInventory(numToSell)
    }

    val earned1 = vm1.stats.value.totalEarned
    val best1 = vm1.stats.value.bestNumber

    // Recreate ViewModel to simulate app restart
    val vm2 = GameViewModel(app)
    val stats2 = vm2.stats.value
    assertEquals(rollStats1.totalRolls, stats2.totalRolls)
    assertEquals(earned1, stats2.totalEarned)
    assertEquals(best1?.id, stats2.bestNumber?.id)
    assertEquals(rollStats1.rarityCounts, stats2.rarityCounts)
  }

  @Test
  fun `test protected SIM cannot be sold and active SIM updates on sale`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = GameViewModel(app)

    // Roll and vault current number
    vm.rollSingle()
    val current = vm.currentNumber.value
    assertNotNull(current)
    assertTrue(vm.saveCurrentToVault())

    val balanceBefore = vm.balance.value
    // Attempt to sell protected number directly
    vm.sellCurrent()
    assertEquals("Balance must NOT change when selling protected SIM", balanceBefore, vm.balance.value)
    assertTrue("Protected SIM must remain in inventory", vm.inventory.value.any { it.id == current?.id })

    // Also attempt sellFromInventory on protected SIM
    val vaultedItem = vm.inventory.value.first { it.id == current?.id }
    vm.sellFromInventory(vaultedItem)
    assertEquals("Balance must NOT change when sellFromInventory called on protected SIM", balanceBefore, vm.balance.value)

    // Now unvault it, set as active SIM, and verify active SIM updates on sale
    vm.toggleVaultStatus(vaultedItem)
    val unvaultedItem = vm.inventory.value.first { it.id == current?.id }
    vm.setActiveSim(unvaultedItem)
    assertEquals(unvaultedItem.id, vm.activeSim.value?.id)

    // Sell unvaulted active SIM
    vm.sellFromInventory(unvaultedItem)
    assertTrue("Balance should increase on valid sale", vm.balance.value > balanceBefore)
    assertTrue("Sold SIM must no longer be active SIM", vm.activeSim.value?.id != unvaultedItem.id)
  }

  @Test
  fun `test inventory limit enforcement`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = com.example.data.GamePreferences(app)

    // Pre-populate inventory backup beyond limit
    val list = mutableListOf<PhoneNumber>()
    for (i in 0 until 120) {
      list.add(
        PhoneNumber(
          id = "test_item_$i",
          prefix = "916",
          digits = String.format("%07d", i),
          formatted = "+7 (916) 000-00-${String.format("%02d", i % 100)}",
          operatorName = "МТС",
          rarity = RarityTier.COMMON,
          categoryDescription = "Тест $i",
          value = 50L,
          isInVault = (i == 0) // First item protected in vault
        )
      )
    }
    prefs.saveInventoryBackup(list)

    val vm = GameViewModel(app)
    assertEquals(120, vm.inventory.value.size)

    // Now roll single which triggers trimInventoryToLimit
    vm.rollSingle()
    assertTrue(
      "Non-vault items must be <= inventoryCapacity (was ${vm.inventory.value.count { !it.isInVault }})",
      vm.inventory.value.count { !it.isInVault } <= vm.inventoryCapacity
    )
    assertTrue("Protected vaulted item must not be trimmed", vm.inventory.value.any { it.isInVault })
  }

  @Test
  fun `test auto roll settings persistence`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm1 = GameViewModel(app)

    val customConfig = com.example.model.AutoSellConfig(
      isAutoRolling = false,
      autoSellBelowRarity = RarityTier.GOLD,
      minKeepPrice = 45_000L,
      autoSaveKeptToInventory = false,
      stopOnUltraRare = true
    )
    vm1.updateAutoSellConfig(customConfig)

    // Recreate ViewModel
    val vm2 = GameViewModel(app)
    val loaded = vm2.autoSellConfig.value
    assertEquals(RarityTier.GOLD, loaded.autoSellBelowRarity)
    assertEquals(45_000L, loaded.minKeepPrice)
    assertEquals(false, loaded.autoSaveKeptToInventory)
    assertEquals(true, loaded.stopOnUltraRare)
  }

  @Test
  fun `test pricing calculation boundary values and scaling`() {
    for (tier in RarityTier.values()) {
      val base = PhoneGenerator.getTierBasePrice(tier)
      assertTrue("Tier base price should be positive", base > 0)
    }

    // Test multiple generations to verify prices stay within bounds
    repeat(100) {
      val num = PhoneGenerator.generate(mvdInsiderLevel = 1, goldenDbLevel = 1, playerLevel = 5)
      assertTrue("Price must be >= minPrice", num.value >= num.rarity.minPrice)
      assertTrue("Price must be <= maxPrice", num.value <= num.rarity.maxPrice)
    }
  }

  @Test
  fun `test diagnostic room database persistence tracking for bestNumber and rarityCounts across restarts`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm1 = GameViewModel(app)

    repeat(5) {
      vm1.rollSingle()
    }

    val statsBeforeRestart = vm1.stats.value
    assertNotNull("bestNumber must be recorded", statsBeforeRestart.bestNumber)
    assertTrue("rarityCounts must have recorded items", statsBeforeRestart.rarityCounts.isNotEmpty())

    // Check Room DB raw tables
    val db = com.example.data.AppDatabase.getInstance(app)
    val rawStatsRows = db.getRawTableContent("game_stats_log")
    assertTrue("game_stats_log in Room DB must not be empty", rawStatsRows.isNotEmpty())
    val roomStats = rawStatsRows.first()
    assertEquals(statsBeforeRestart.bestNumber?.formatted, roomStats["bestNumberFormatted"])

    // Recreate ViewModel to simulate restart
    val vm2 = GameViewModel(app)
    assertEquals(statsBeforeRestart.bestNumber?.formatted, vm2.stats.value.bestNumber?.formatted)
    assertEquals(statsBeforeRestart.rarityCounts, vm2.stats.value.rarityCounts)
  }

  @Test
  fun `test legacy phone number parsing and recovery from older version`() {
    // Legacy JSON format from old versions that did not have all modern fields
    val legacyJson = """
      {
        "id": "legacy_sim_100",
        "formatted": "+7 (999) 777-88-99",
        "operatorName": "МегаФон",
        "rarity": "GOLD",
        "value": 25000,
        "isInVault": true
      }
    """.trimIndent()

    val parsed = PhoneNumber.fromJson(legacyJson)
    assertNotNull("Legacy number must be successfully parsed", parsed)
    assertEquals("legacy_sim_100", parsed?.id)
    assertEquals("+7 (999) 777-88-99", parsed?.formatted)
    assertEquals("999", parsed?.prefix)
    assertEquals(RarityTier.GOLD, parsed?.rarity)
    assertEquals(25000L, parsed?.value)
    assertTrue("isInVault flag must be preserved", parsed?.isInVault == true)
  }

  @Test
  fun `test user numbers and data synchronization from older version backup`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val prefs = com.example.data.GamePreferences(app)

    // Simulate user having data from an old version in preferences
    val oldSim1 = PhoneNumber(
        id = "old_sim_01",
        prefix = "999",
        digits = "1234567",
        formatted = "+7 (999) 123-45-67",
        operatorName = "МТС",
        rarity = RarityTier.PLATINUM,
        categoryDescription = "Платиновый номер",
        value = 150_000L,
        isInVault = true
    )
    val oldSim2 = PhoneNumber(
        id = "old_sim_02",
        prefix = "903",
        digits = "9990000",
        formatted = "+7 (903) 999-00-00",
        operatorName = "Билайн",
        rarity = RarityTier.GOLD,
        categoryDescription = "Золотой номер",
        value = 25_000L,
        isInVault = false
    )

    prefs.saveInventoryBackup(listOf(oldSim1, oldSim2))
    prefs.saveActiveSim(oldSim1)
    prefs.saveBalance(100_000L)
    prefs.saveStats(
        rolls = 50L,
        earned = 200_000L,
        bestNumber = oldSim1,
        rarityCounts = mapOf(RarityTier.PLATINUM to 1, RarityTier.GOLD to 2)
    )

    // Launch ViewModel
    val vm = GameViewModel(app)

    // Verify numbers are synchronized into ViewModel inventory
    assertEquals(2, vm.inventory.value.size)
    assertTrue("Inventory must contain oldSim1", vm.inventory.value.any { it.id == "old_sim_01" })
    assertTrue("Inventory must contain oldSim2", vm.inventory.value.any { it.id == "old_sim_02" })
    assertEquals(oldSim1.id, vm.activeSim.value?.id)
    assertEquals(100_000L, vm.balance.value)
    assertEquals(50L, vm.stats.value.totalRolls)
    assertEquals(oldSim1.formatted, vm.stats.value.bestNumber?.formatted)
  }

  @Test
  fun `test alternative field names in legacy json format`() {
    // Testing legacy formats where fields were named 'price', 'number', 'operator', 'tier', 'category', 'isVault'
    val jsonWithAlternateKeys = """
      {
        "id": "alt_key_sim_1",
        "number": "+7 (925) 555-44-33",
        "price": 88000,
        "operator": "МегаФон",
        "tier": "PLATINUM",
        "category": "Три топора",
        "isVault": true
      }
    """.trimIndent()

    val parsed = PhoneNumber.fromJson(jsonWithAlternateKeys)
    assertNotNull("Should parse JSON with alternative key names", parsed)
    assertEquals("+7 (925) 555-44-33", parsed?.formatted)
    assertEquals(88000L, parsed?.value)
    assertEquals("МегаФон", parsed?.operatorName)
    assertEquals(RarityTier.PLATINUM, parsed?.rarity)
    assertEquals("Три топора", parsed?.categoryDescription)
    assertTrue("isVault flag should map to isInVault", parsed?.isInVault == true)

    // Testing plain string phone fallback
    val plainPhone = "+7 (916) 123-45-67"
    val parsedPlain = PhoneNumber.fromJson(plainPhone)
    assertNotNull("Should parse plain phone string into a PhoneNumber", parsedPlain)
    assertEquals("+7 (916) 123-45-67", parsedPlain?.formatted)
    assertEquals("916", parsedPlain?.prefix)
  }

  @Test
  fun `test migration from alternate SharedPreferences files and legacy keys`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // Simulate old app saving into default shared preferences
    val defaultPrefs = context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)
    defaultPrefs.edit()
        .putInt("balance", 77_777)
        .putInt("player_xp", 1_500)
        .putString("key_active_sim", """{"id":"active_legacy","formatted":"+7 (999) 777-77-77","rarity":"GOLD","value":50000}""")
        .putString("inventory", """[{"id":"inv_1","formatted":"+7 (999) 111-11-11","rarity":"SILVER","value":5000}]""")
        .apply()

    val gamePrefs = com.example.data.GamePreferences(context)
    assertEquals(77_777L, gamePrefs.loadBalance())
    assertEquals(1_500L, gamePrefs.loadPlayerXp())
    val activeSim = gamePrefs.loadActiveSim()
    assertNotNull(activeSim)
    assertEquals("+7 (999) 777-77-77", activeSim?.formatted)

    val inventory = gamePrefs.loadInventoryBackup()
    assertEquals(1, inventory.size)
    assertEquals("+7 (999) 111-11-11", inventory[0].formatted)
  }

  @Test
  fun `test varied pricing spread and multipliers scaling beyond fixed 7500`() {
    val goldPrices = mutableListOf<Long>()
    val commonPrices = mutableListOf<Long>()

    repeat(300) {
      val num = PhoneGenerator.generate(mvdInsiderLevel = 2, goldenDbLevel = 3, playerLevel = 5)
      if (num.rarity == RarityTier.GOLD) {
        goldPrices.add(num.value)
      } else if (num.rarity == RarityTier.COMMON) {
        commonPrices.add(num.value)
      }
      assertTrue("Value must be >= tier min", num.value >= num.rarity.minPrice)
      assertTrue("Value must be <= tier max", num.value <= num.rarity.maxPrice)
      assertTrue("Total multiplier must be >= 1.0", num.totalMultiplier >= 1.0)
    }

    if (goldPrices.isNotEmpty()) {
      // Verify Gold numbers are properly valued in their new dynamic bracket (18,000 - 95,000)
      assertTrue("Gold numbers should exceed 15,000 rubles", goldPrices.all { it >= 15_000L })
    }
  }
}
