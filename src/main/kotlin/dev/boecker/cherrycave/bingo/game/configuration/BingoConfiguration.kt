package dev.boecker.cherrycave.bingo.game.configuration

import dev.boecker.cherrycave.bingo.game.item.BoardDifficulty
import org.bukkit.Difficulty

data class BingoConfiguration(
    val boardSize: Int = 5,
    val minecraftDifficulty: Difficulty = Difficulty.NORMAL,
    val keepInventory: Boolean = true,
    val allowTeleportCommands: Boolean = true,
    val backPackSize: Int = 3,
    val hunger: Boolean = false,
    val boardDifficulty: BoardDifficulty = BoardDifficulty.NORMAL,
)