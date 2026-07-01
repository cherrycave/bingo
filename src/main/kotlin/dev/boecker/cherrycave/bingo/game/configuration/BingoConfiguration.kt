package dev.boecker.cherrycave.bingo.game.configuration

import org.bukkit.Difficulty

data class BingoConfiguration(
    val boardSize: Int = 5,
    val minecraftDifficulty: Difficulty = Difficulty.NORMAL,
)