package com.example.model

data class AutoSellConfig(
    val isAutoRolling: Boolean = false,
    val autoSellBelowRarity: RarityTier = RarityTier.SILVER, // Sell COMMON and BRONZE by default
    val minKeepPrice: Long = 25_000,
    val autoSaveKeptToInventory: Boolean = true,
    val stopOnUltraRare: Boolean = true
)
