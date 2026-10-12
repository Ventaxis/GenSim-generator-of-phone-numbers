package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.MultiplierDetail
import com.example.model.PhoneNumber
import com.example.model.RarityTier

@Entity(tableName = "saved_sims")
data class SavedSimEntity(
    @PrimaryKey val id: String,
    val countryCode: String,
    val prefix: String,
    val digits: String,
    val formatted: String,
    val operatorName: String,
    val rarityName: String,
    val categoryDescription: String,
    val basePrice: Long,
    val multipliersEncoded: String, // serialized labels and factors
    val totalMultiplier: Double,
    val value: Long,
    val timestamp: Long,
    val isInVault: Boolean = false
) {
    fun toDomain(): PhoneNumber {
        val rarity = try {
            RarityTier.valueOf(rarityName)
        } catch (_: Exception) {
            RarityTier.COMMON
        }

        val parsedMultipliers = if (multipliersEncoded.isBlank()) {
            emptyList()
        } else {
            multipliersEncoded.split(";").mapNotNull { part ->
                val tokens = part.split("|")
                if (tokens.size >= 3) {
                    MultiplierDetail(
                        title = tokens[0],
                        factor = tokens[1].toDoubleOrNull() ?: 1.0,
                        isHighlighted = tokens[2].toBoolean()
                    )
                } else null
            }
        }

        return PhoneNumber(
            id = id,
            countryCode = countryCode,
            prefix = prefix,
            digits = digits,
            formatted = formatted,
            operatorName = operatorName,
            rarity = rarity,
            categoryDescription = categoryDescription,
            basePrice = basePrice,
            multipliers = parsedMultipliers,
            totalMultiplier = totalMultiplier,
            value = value,
            timestamp = timestamp,
            isInVault = isInVault
        )
    }

    companion object {
        fun fromDomain(model: PhoneNumber): SavedSimEntity {
            val encodedMults = model.multipliers.joinToString(";") {
                "${it.title}|${it.factor}|${it.isHighlighted}"
            }

            return SavedSimEntity(
                id = model.id,
                countryCode = model.countryCode,
                prefix = model.prefix,
                digits = model.digits,
                formatted = model.formatted,
                operatorName = model.operatorName,
                rarityName = model.rarity.name,
                categoryDescription = model.categoryDescription,
                basePrice = model.basePrice,
                multipliersEncoded = encodedMults,
                totalMultiplier = model.totalMultiplier,
                value = model.value,
                timestamp = model.timestamp,
                isInVault = model.isInVault
            )
        }
    }
}
