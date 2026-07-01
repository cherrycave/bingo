package dev.boecker.cherrycave.bingo.game.team

import dev.boecker.cherrycave.bingo.game.BingoGameManager
import org.bukkit.entity.Player

fun Player.getBingoTeam(gameManager: BingoGameManager): BingoTeams? {
    gameManager.teams.forEach { (team, uuids) ->
        if (uuids.any { it == this.uniqueId }) {
            return team
        }
    }

    return null
}