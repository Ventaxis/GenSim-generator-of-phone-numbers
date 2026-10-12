package com.example.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class MultiplierDetail(
    val title: String,
    val factor: Double,
    val isHighlighted: Boolean = false
)

data class PhoneNumber(
    val id: String = UUID.randomUUID().toString(),
    val countryCode: String = "+7",
    val prefix: String, // e.g. "999", "495"
    val digits: String, // 7 digits, e.g. "7777777"
    val formatted: String, // e.g. "+7 (999) 777-77-77"
    val operatorName: String,
    val rarity: RarityTier,
    val categoryDescription: String,
    val basePrice: Long = 50L,
    val multipliers: List<MultiplierDetail> = emptyList(),
    val totalMultiplier: Double = 1.0,
    val value: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val isInVault: Boolean = false
) {
    fun toJson(): String {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("countryCode", countryCode)
        obj.put("prefix", prefix)
        obj.put("digits", digits)
        obj.put("formatted", formatted)
        obj.put("operatorName", operatorName)
        obj.put("rarity", rarity.name)
        obj.put("categoryDescription", categoryDescription)
        obj.put("basePrice", basePrice)
        obj.put("totalMultiplier", totalMultiplier)
        obj.put("value", value)
        obj.put("timestamp", timestamp)
        obj.put("isInVault", isInVault)

        val arr = JSONArray()
        for (m in multipliers) {
            val item = JSONObject()
            item.put("title", m.title)
            item.put("factor", m.factor)
            item.put("isHighlighted", m.isHighlighted)
            arr.put(item)
        }
        obj.put("multipliers", arr)

        return obj.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): PhoneNumber? {
            return try {
                val obj = JSONObject(jsonStr)
                val mults = mutableListOf<MultiplierDetail>()
                val arr = obj.optJSONArray("multipliers")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val mo = arr.getJSONObject(i)
                        mults.add(
                            MultiplierDetail(
                                title = mo.getString("title"),
                                factor = mo.getDouble("factor"),
                                isHighlighted = mo.optBoolean("isHighlighted", false)
                            )
                        )
                    }
                }
                PhoneNumber(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    countryCode = obj.optString("countryCode", "+7"),
                    prefix = obj.getString("prefix"),
                    digits = obj.getString("digits"),
                    formatted = obj.getString("formatted"),
                    operatorName = obj.getString("operatorName"),
                    rarity = try {
                        RarityTier.valueOf(obj.getString("rarity"))
                    } catch (_: Exception) {
                        RarityTier.COMMON
                    },
                    categoryDescription = obj.optString("categoryDescription", ""),
                    basePrice = obj.optLong("basePrice", 50L),
                    multipliers = mults,
                    totalMultiplier = obj.optDouble("totalMultiplier", 1.0),
                    value = obj.getLong("value"),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    isInVault = obj.optBoolean("isInVault", false)
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
