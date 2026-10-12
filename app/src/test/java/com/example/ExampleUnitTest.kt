package com.example

import com.example.generator.PhoneGenerator
import com.example.model.PhoneNumber
import com.example.model.PlayerProgression
import com.example.model.RarityTier
import com.example.model.UpgradeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testPhoneNumberFormatting() {
    val formatted = PhoneGenerator.formatNumber("999", "7777777")
    assertEquals("+7 (999) 777-77-77", formatted)
  }

  @Test
  fun testPhoneGenerationReturnsValidNumber() {
    val number = PhoneGenerator.generate(mvdInsiderLevel = 0, goldenDbLevel = 0, playerLevel = 1)
    assertNotNull(number)
    assertEquals(7, number.digits.length)
    assertTrue(number.value >= number.rarity.minPrice)
    assertTrue(number.value <= number.rarity.maxPrice)
  }

  @Test
  fun testDropRateBalanceRareNumbersAreActuallyRare() {
    var commonCount = 0
    var ultraRareCount = 0
    val totalRolls = 1_000

    repeat(totalRolls) {
      val num = PhoneGenerator.generate(mvdInsiderLevel = 0, goldenDbLevel = 0, playerLevel = 1)
      if (num.rarity == RarityTier.COMMON) {
        commonCount++
      }
      if (num.rarity == RarityTier.DIAMOND || num.rarity.isGovOrSecret) {
        ultraRareCount++
      }
    }

    // Common numbers must be the dominant majority (~80-90%)
    assertTrue(
        "Common numbers should be the majority (expected >= 750 of 1000, got $commonCount)",
        commonCount >= 750
    )
    // Ultra-rare numbers must NOT drop frequently
    assertTrue(
        "Ultra rare numbers should be rare (expected <= 10 of 1000, got $ultraRareCount)",
        ultraRareCount <= 10
    )
  }

  @Test
  fun testUpgradePricingCalculation() {
    val upgrade = UpgradeType.PASSIVE_INCOME
    val level0Price = upgrade.getPrice(0)
    val level1Price = upgrade.getPrice(1)
    assertEquals(upgrade.basePrice, level0Price)
    assertEquals((upgrade.basePrice * upgrade.priceMultiplier).toLong(), level1Price)
  }

  @Test
  fun testHighValueNumbersAwardHighExperience() {
    val commonNumber = PhoneNumber(
        prefix = "916",
        digits = "1234567",
        formatted = "+7 (916) 123-45-67",
        operatorName = "МТС",
        rarity = RarityTier.COMMON,
        categoryDescription = "Обычный сотовый",
        basePrice = 50L,
        totalMultiplier = 1.0,
        value = 50L
    )

    val goldVipNumber = PhoneNumber(
        prefix = "999",
        digits = "7778888",
        formatted = "+7 (999) 777-88-88",
        operatorName = "YOTA VIP",
        rarity = RarityTier.GOLD,
        categoryDescription = "Золотая пара",
        basePrice = 50L,
        totalMultiplier = 120.0,
        value = 150_000L
    )

    val kremlinNumber = PhoneNumber(
        prefix = "495",
        digits = "6060001",
        formatted = "+7 (495) 606-00-01",
        operatorName = "Кремль АТС-1",
        rarity = RarityTier.FSB_KREMLIN,
        categoryDescription = "Администрация Президента",
        basePrice = 50L,
        totalMultiplier = 850.0,
        value = 1_500_000L
    )

    val commonXp = PlayerProgression.getXpRewardForNumber(commonNumber)
    val goldXp = PlayerProgression.getXpRewardForNumber(goldVipNumber)
    val kremlinXp = PlayerProgression.getXpRewardForNumber(kremlinNumber)

    assertTrue("Common XP should be reasonable", commonXp in 5..10)
    assertTrue("Gold number should award substantially more XP ($goldXp)", goldXp >= 1_000L)
    assertTrue("Kremlin number should award massive XP ($kremlinXp)", kremlinXp >= 15_000L)
    assertTrue("Higher value must give strictly higher XP", kremlinXp > goldXp && goldXp > commonXp)
  }

  @Test
  fun testPlayerLevelCurveAndPerks() {
    val level1 = PlayerProgression.getLevelInfo(0L)
    assertEquals(1, level1.level)
    assertEquals("Стажёр связи", level1.title)
    assertEquals(0, level1.bonusVaultSlots)

    val level2 = PlayerProgression.getLevelInfo(500L)
    assertTrue("Should reach level 2 with 500 XP", level2.level >= 2)
    assertTrue("Should have bonus slots", level2.bonusVaultSlots > 0)
    assertTrue("Should have passive income bonus", level2.passiveIncomeBonusPercent > 0)

    val roadmap = PlayerProgression.getRoadmap(2)
    assertEquals(20, roadmap.size)
    assertTrue("Level 1 should be unlocked", roadmap[0].isUnlocked)
    assertTrue("Level 2 should be unlocked", roadmap[1].isUnlocked)
  }
}
