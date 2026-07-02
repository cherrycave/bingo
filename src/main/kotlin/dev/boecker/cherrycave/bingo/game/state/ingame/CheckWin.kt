package dev.boecker.cherrycave.bingo.game.state.ingame

import dev.boecker.cherrycave.bingo.game.BingoGameManager
import dev.boecker.cherrycave.bingo.game.team.BingoTeams
import dev.boecker.cherrycave.bingo.game.team.getBingoTeam
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player

fun checkIfBingoItem(material: Material, player: Player, gameManager: BingoGameManager) {
    if (!gameManager.ingameState.isActive) return
    if (gameManager.bingoBoard == null) return

    val team = player.getBingoTeam(gameManager) ?: return

    val successCollectSound = Sound.sound(Key.key("block.note_block.bell"), Sound.Source.PLAYER, 1f, 1f)
    val othersCollectSound = Sound.sound(Key.key("block.note_block.bass"), Sound.Source.PLAYER, 1f, 1f)

    if (gameManager.bingoBoard!!.contains(material) && !gameManager.ingameState.collectedItems[team]!!.contains(material)) {
        gameManager.ingameState.collectedItems[team]!!.add(material)
        gameManager.plugin.server.broadcast(
            MiniMessage.miniMessage()
                .deserialize("<${team.teamColor}>${team.teamName} <white>collected</white> <blue><lang:${material.translationKey()}><white>! <gray>(${gameManager.ingameState.collectedItems[team]!!.size}/${gameManager.bingoBoard!!.size})")
        )
        val teamUUIDs = gameManager.teams[team]!!
        gameManager.plugin.server.onlinePlayers.forEach { player ->
            if (player.uniqueId in teamUUIDs) {
                player.playSound(successCollectSound, Sound.Emitter.self())
            } else {
                player.playSound(othersCollectSound, Sound.Emitter.self())
            }
        }

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
    if (fromLeftIterator.all { i ->
            twoDimensionalBoard[i][i] in gameManager.ingameState.collectedItems[team]!!
        }) return true


    val fromRightIterator = ((boardSize - 1) downTo 0)
    return fromRightIterator.all { i ->
        twoDimensionalBoard[i][i] in gameManager.ingameState.collectedItems[team]!!
    }
}