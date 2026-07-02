package dev.boecker.cherrycave.bingo.game.state

import dev.boecker.cherrycave.bingo.game.BingoGameManager
import dev.boecker.cherrycave.bingo.game.team.getBingoTeam
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.properties.Delegates

class GameOverState(manager: BingoGameManager) : GameState(manager) {

    var endTimer by Delegates.notNull<Int>()

    var scheduler: Int = 0

    override fun startState() {
        super.startState()

        endTimer = 20

        val winSound = Sound.sound(Key.key("entity.wither.death"), Sound.Source.AMBIENT, 1f, 1f)
        val loseSound = Sound.sound(Key.key("entity.elder_guardian.curse"), Sound.Source.AMBIENT, 1f, 1f)


        gameManager.plugin.server.onlinePlayers.forEach { player ->
            player.gameMode = GameMode.SPECTATOR

            player.closeInventory()

            player.sendTitlePart(
                TitlePart.TITLE,
                Component.text("${gameManager.winnerTeam?.teamName} wins!", gameManager.winnerTeam?.teamColor)
            )
            player.sendTitlePart(
                TitlePart.SUBTITLE,
                Component.text(gameManager.teams[gameManager.winnerTeam]!!.map { uuid -> gameManager.plugin.server.onlinePlayers.find { it.uniqueId == uuid } }
                    .joinToString(", ") { it?.name ?: "??" }, NamedTextColor.WHITE)
            )
            player.sendTitlePart(
                TitlePart.TIMES,
                Title.DEFAULT_TIMES
            )

            if (player.getBingoTeam(gameManager) == gameManager.winnerTeam) {
                player.playSound(winSound, Sound.Emitter.self())
            } else {
                player.playSound(loseSound, Sound.Emitter.self())
            }
        }

        scheduler = gameManager.plugin.server.scheduler.scheduleSyncRepeatingTask(gameManager.plugin, {
            if (endTimer == 0) {
                gameManager.nextState()
            }
            gameManager.plugin.server.onlinePlayers.forEach { player ->
                player.sendActionBar(gameManager.mm.deserialize("<white>Game will end in <blue>$endTimer</blue>"))
            }
            endTimer--
        }, 0, 20)
    }

    @OptIn(ExperimentalPathApi::class)
    override fun endState() {
        gameManager.plugin.server.scheduler.cancelTask(scheduler)

        gameManager.plugin.server.onlinePlayers.forEach { player ->
            player.inventory.clear()
            gameManager.lobbyState.deletePlayerData(player)
            player.teleport(gameManager.lobbyState.lobbySpawn)
            player.gameMode = GameMode.ADVENTURE
        }

        gameManager.teamWorlds.values.filterNotNull().forEach {
            gameManager.plugin.server.scheduler.runTask(gameManager.plugin, Runnable {
                gameManager.plugin.server.unloadWorld(it.first, false)
                gameManager.plugin.server.unloadWorld(it.second, false)
                it.first.worldPath.deleteRecursively()
                it.second.worldPath.deleteRecursively()
            })
        }

        gameManager.winnerTeam = null

        super.endState()
    }

    @EventHandler
    fun onPlayerDisconnect(event: PlayerQuitEvent) {
        if (!isActive) return

        gameManager.teams.values.find { teamUUIDs -> teamUUIDs.any { it == event.player.uniqueId } }
            ?.remove(event.player.uniqueId)
    }

}