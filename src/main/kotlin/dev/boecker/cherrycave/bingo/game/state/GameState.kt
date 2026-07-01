package dev.boecker.cherrycave.bingo.game.state

import dev.boecker.cherrycave.bingo.game.BingoGameManager
import org.bukkit.event.Listener

sealed class GameState(val gameManager: BingoGameManager) : Listener {

    var isActive: Boolean = false

    open fun startState() {
        isActive = true
    }

    open fun endState() {
        isActive = false
    }

}