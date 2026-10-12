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
            val trimmed = jsonStr.trim()
            if (trimmed.isEmpty()) return null

            // If string is not a JSON object but a plain phone string, construct a valid PhoneNumber
            if (!trimmed.startsWith("{")) {
                val digitsOnly = trimmed.filter { it.isDigit() }
                if (digitsOnly.length >= 7) {
                    val prefix = if (digitsOnly.length >= 10) digitsOnly.substring(digitsOnly.length - 10, digitsOnly.length - 7) else "999"
                    val last7 = digitsOnly.takeLast(7)
                    val formatted = "+7 ($prefix) ${last7.take(3)}-${last7.substring(3, 5)}-${last7.takeLast(2)}"
                    return PhoneNumber(
                        id = UUID.randomUUID().toString(),
                        countryCode = "+7",
                        prefix = prefix,
                        digits = last7,
                        formatted = formatted,
                        operatorName = "МТС",
                        rarity = RarityTier.COMMON,
                        categoryDescription = "Обычный номер",
                        basePrice = 50L,
                        totalMultiplier = 1.0,
                        value = 50L,
                        timestamp = System.currentTimeMillis(),
                        isInVault = false
                    )
                }
                return null
            }

            return try {
                val obj = JSONObject(trimmed)
                val mults = mutableListOf<MultiplierDetail>()
                val arr = obj.optJSONArray("multipliers")
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val mo = arr.optJSONObject(i) ?: continue
                        mults.add(
                            MultiplierDetail(
                                title = mo.optString("title", ""),
                                factor = mo.optDouble("factor", 1.0),
                                isHighlighted = mo.optBoolean("isHighlighted", false)
                            )
                        )
                    }
                }

                val countryCode = obj.optString("countryCode", "+7").ifBlank { "+7" }
                var rawFormatted = obj.optString("formatted", "").ifBlank {
                    obj.optString("number", "").ifBlank {
                        obj.optString("phone", "").ifBlank {
                            obj.optString("phoneNumber", "")
                        }
                    }
                }
                var rawPrefix = obj.optString("prefix", "")
                var rawDigits = obj.optString("digits", "")

                if (rawDigits.isEmpty() && rawFormatted.isNotEmpty()) {
                    val digitsOnly = rawFormatted.filter { it.isDigit() }
                    rawDigits = if (digitsOnly.length >= 10) digitsOnly.takeLast(7) else digitsOnly.padEnd(7, '0')
                }

                if (rawPrefix.isEmpty()) {
                    val regex = Regex("""\+7\s*\((\d{3})\)""")
                    val match = regex.find(rawFormatted)
                    rawPrefix = match?.groupValues?.get(1) ?: run {
                        val digitsOnly = rawFormatted.filter { it.isDigit() }
                        if (digitsOnly.length >= 10) digitsOnly.substring(digitsOnly.length - 10, digitsOnly.length - 7) else "999"
                    }
                }

                if (rawFormatted.isEmpty()) {
                    val cleanDigits = rawDigits.padEnd(7, '0')
                    rawFormatted = "$countryCode ($rawPrefix) ${cleanDigits.take(3)}-${cleanDigits.substring(3, 5)}-${cleanDigits.takeLast(2)}"
                }

                val operatorName = obj.optString("operatorName", "").ifBlank {
                    obj.optString("operator", "").ifBlank {
                        obj.optString("carrier", "МТС")
                    }
                }

                val rarityStr = obj.optString("rarity", "").ifBlank {
                    obj.optString("tier", "").ifBlank {
                        obj.optString("rarityTier", "COMMON")
                    }
                }
                val rarity = try {
                    RarityTier.valueOf(rarityStr.uppercase())
                } catch (_: Exception) {
                    RarityTier.COMMON
                }

                val parsedValue = when {
                    obj.has("value") -> obj.optLong("value", 50L)
                    obj.has("price") -> obj.optLong("price", 50L)
                    obj.has("cost") -> obj.optLong("cost", 50L)
                    else -> 50L
                }

                val parsedBasePrice = when {
                    obj.has("basePrice") -> obj.optLong("basePrice", 50L)
                    obj.has("base_price") -> obj.optLong("base_price", 50L)
                    obj.has("base") -> obj.optLong("base", 50L)
                    else -> minOf(parsedValue, 50L)
                }

                val categoryDesc = obj.optString("categoryDescription", "").ifBlank {
                    obj.optString("category", "").ifBlank {
                        obj.optString("description", "").ifBlank {
                            obj.optString("desc", "")
                        }
                    }
                }

                val isInVault = obj.optBoolean("isInVault", false) ||
                                obj.optBoolean("isVault", false) ||
                                obj.optBoolean("vault", false) ||
                                obj.optBoolean("isSaved", false) ||
                                obj.optBoolean("saved", false)

                val totalMultiplier = when {
                    obj.has("totalMultiplier") -> obj.optDouble("totalMultiplier", 1.0)
                    obj.has("multiplier") -> obj.optDouble("multiplier", 1.0)
                    obj.has("mult") -> obj.optDouble("mult", 1.0)
                    else -> 1.0
                }

                val id = obj.optString("id", "").ifBlank {
                    obj.optString("uuid", UUID.randomUUID().toString())
                }

                val timestamp = obj.optLong("timestamp", System.currentTimeMillis())

                PhoneNumber(
                    id = id,
                    countryCode = countryCode,
                    prefix = rawPrefix,
                    digits = rawDigits,
                    formatted = rawFormatted,
                    operatorName = operatorName,
                    rarity = rarity,
                    categoryDescription = categoryDesc,
                    basePrice = parsedBasePrice,
                    multipliers = mults,
                    totalMultiplier = totalMultiplier,
                    value = parsedValue,
                    timestamp = timestamp,
                    isInVault = isInVault
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
