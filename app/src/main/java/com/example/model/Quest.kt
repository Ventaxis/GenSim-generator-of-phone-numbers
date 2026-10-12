package com.example.model

enum class QuestPeriod(val title: String) {
    DAILY("Ежедневные"),
    WEEKLY("Еженедельные"),
    MONTHLY("Сезон (Месяц)")
}

data class Quest(
    val id: String,
    val period: QuestPeriod,
    val title: String,
    val description: String,
    val currentProgress: Int = 0,
    val targetProgress: Int,
    val rewardAmount: Long,
    val isClaimed: Boolean = false,
    val categoryIcon: String = "roll"
) {
    val isCompleted: Boolean
        get() = currentProgress >= targetProgress

    val progressFraction: Float
        get() = (currentProgress.toFloat() / targetProgress.toFloat()).coerceIn(0f, 1f)
}
