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
                .deserialize("<${team.teamColor}>${player.name} <gray>(<${team.teamColor}>${team.teamName}<gray>) <white>collected</white> <blue><lang:${material.translationKey()}><white>! <gray>(${gameManager.ingameState.collectedItems[team]!!.size}/${gameManager.bingoBoard!!.size})")
        )
        val teamUUIDs = gameManager.teams[team]!!
        gameManager.plugin.server.onlinePlayers.forEach { player ->
            if (player.uniqueId in teamUUIDs) {
                player.playSound(successCollectSound, Sound.Emitter.self())
            } else {
                player.playSound(othersCollectSound, Sound.Emitter.self())
            }
        }

        val (bestRowSize, missingItems) = checkForBingo(team, gameManager)
        if (bestRowSize == gameManager.bingoConfiguration.boardSize) {
            gameManager.winnerTeam = team
            gameManager.nextState()
        } else if (bestRowSize == gameManager.bingoConfiguration.boardSize - 1) {
            gameManager.plugin.server.broadcast(
                MiniMessage.miniMessage()
                    .deserialize(
                        "<${team.teamColor}>${team.teamName} <white>only needs one ${if (missingItems.size > 1) "of the following items" else "item"} to win: <blue>${
                            missingItems.joinToString(
                                "<white>, <blue>"
                            ) { "<lang:${it.translationKey()}>" }
                        }"
                    )
            )
        }
    }
}

fun checkForBingo(team: BingoTeams, gameManager: BingoGameManager): Pair<Int, Set<Material>> {
    val board = gameManager.bingoBoard!!

    val boardSize = gameManager.bingoConfiguration.boardSize

    val twoDimensionalBoard = board.chunked(boardSize)

    val bestRows =
        twoDimensionalBoard.map { row -> row.intersect(gameManager.ingameState.collectedItems[team]!!.toSet()).size to (row - gameManager.ingameState.collectedItems[team]!!.toSet()) }

    val bestColumns =
        (0 until boardSize).map { x -> (0 until boardSize).map { y -> board[y * boardSize + x] } }
            .map { row -> row.intersect(gameManager.ingameState.collectedItems[team]!!.toSet()).size to (row - gameManager.ingameState.collectedItems[team]!!.toSet()) }

    val diagonal1Intersect = (0 until boardSize).map { i -> twoDimensionalBoard[i][i] }
        .let { row -> row.intersect(gameManager.ingameState.collectedItems[team]!!.toSet()).size to (row - gameManager.ingameState.collectedItems[team]!!.toSet()) }

    val diagonal2Intersect = ((boardSize - 1) downTo 0).map { i -> twoDimensionalBoard[i][i] }
        .let { row -> row.intersect(gameManager.ingameState.collectedItems[team]!!.toSet()).size to (row - gameManager.ingameState.collectedItems[team]!!.toSet()) }

    val bestBingos = (bestRows + bestColumns + diagonal1Intersect + diagonal2Intersect).groupBy { it.first }.maxBy { it.key }.value

    return bestBingos.first().first to bestBingos.flatMap { it.second }.toSet()
}