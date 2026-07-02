package dev.boecker.cherrycave.bingo.game.state.prepare

import dev.boecker.cherrycave.bingo.game.state.GamePreperationState

fun GamePreperationState.initializeTeams() {
    gameManager.plugin.server.onlinePlayers.forEach { player ->
        player.inventory.clear()

        player.scoreboard = gameManager.plugin.server.scoreboardManager.newScoreboard

        filledTeams.forEach { (team, teamUUIDs) ->
            val scoreboardTeam = player.scoreboard.registerNewTeam(team.teamName)
            scoreboardTeam.prefix(gameManager.mm.deserialize("<${team.teamColor}>${team.teamName} <gray>| <${team.teamColor}>"))
            gameManager.plugin.server.onlinePlayers.filter { it.uniqueId in teamUUIDs }.forEach {
                scoreboardTeam.addPlayer(it)
            }
        }
    }
}