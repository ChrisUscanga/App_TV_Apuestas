package com.example.tv_app.model

data class Bet(
    val eventId: String,
    val homeTeam: String,
    val awayTeam: String,
    val selectedTeam: String,
    val betAmount: Double,
    val odds: Double,
    val potentialWinnings: Double,
    val isCompleted: Boolean = false,
    val result: BetResult? = null
)

enum class BetResult {
    WIN,
    LOSS
} 