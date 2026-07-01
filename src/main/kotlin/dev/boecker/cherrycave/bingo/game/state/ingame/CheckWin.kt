package dev.boecker.cherrycave.bingo.game.state.ingame

import dev.boecker.cherrycave.bingo.game.BingoGameManager
import dev.boecker.cherrycave.bingo.game.team.BingoTeams
import dev.boecker.cherrycave.bingo.game.team.getBingoTeam
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player

fun checkIfBingoItem(material: Material, player: Player, gameManager: BingoGameManager) {
    if (!gameManager.ingameState.isActive) return
    if (gameManager.bingoBoard == null) return

    val team = player.getBingoTeam(gameManager) ?: return

    if (gameManager.bingoBoard!!.contains(material) && !gameManager.ingameState.collectedItems[team]!!.contains(material)) {
        gameManager.plugin.server.broadcast(
            MiniMessage.miniMessage()
                .deserialize("<${team.teamColor}>${team.teamName} <white>collected</white> <blue><lang:${material.translationKey()}><white>!")
        )
        gameManager.ingameState.collectedItems[team]!!.add(material)

        if (checkForBingo(team, gameManager)) {
            gameManager.winnerTeam = team
            gameManager.nextState()
        }
    }
}

fun checkForBingo(team: BingoTeams, gameManager: BingoGameManager): Boolean {
    val board = gameManager.bingoBoard!!

    val boardSize = gameManager.bingoConfiguration.boardSize

    val twoDimensionalBoard = board.windowed(boardSize, boardSize)

    if (twoDimensionalBoard.any { row -> row.all { it in gameManager.ingameState.collectedItems[team]!! } }) {
        return true
    }

    for (i in 0 until boardSize) {
        if (twoDimensionalBoard.all { row -> row[i] in gameManager.ingameState.collectedItems[team]!! }) return true
    }


    val fromLeftIterator = (0 until boardSize)
    println("from left iterator: $fromLeftIterator")
    if (fromLeftIterator.all { i ->
            twoDimensionalBoard[i][i] in gameManager.ingameState.collectedItems[team]!!
        }) return true


    val fromRightIterator = ((boardSize - 1) downTo 0)
    println("from right iterator: $fromRightIterator")
    if (fromRightIterator.all { i ->
            twoDimensionalBoard[i][i] in gameManager.ingameState.collectedItems[team]!!
        }) return true

    return false
}