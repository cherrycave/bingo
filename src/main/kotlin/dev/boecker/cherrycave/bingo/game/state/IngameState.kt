package dev.boecker.cherrycave.bingo.game.state

import dev.boecker.cherrycave.bingo.game.BingoGameManager
import dev.boecker.cherrycave.bingo.game.state.ingame.bingoBoardInventory
import dev.boecker.cherrycave.bingo.game.state.ingame.checkIfBingoItem
import dev.boecker.cherrycave.bingo.game.team.BingoTeams
import dev.boecker.cherrycave.bingo.game.team.getBingoTeam
import io.papermc.paper.event.entity.EntityPortalReadyEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.PortalType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.window.Window
import java.util.function.BiConsumer

class IngameState(manager: BingoGameManager) : GameState(manager) {

    lateinit var bingoBoardInventory: Window.Builder.Normal.Split

    lateinit var collectedItems: Map<BingoTeams, MutableList<Material>>

    lateinit var backpacks: Map<BingoTeams, MutableList<ItemStack>>

    override fun startState() {
        super.startState()

        collectedItems = BingoTeams.entries.associateWith { team -> mutableListOf() }
        backpacks = BingoTeams.entries.associateWith { team -> mutableListOf() }

        bingoBoardInventory =
            bingoBoardInventory(gameManager.bingoConfiguration.boardSize, gameManager.bingoBoard!!, gameManager)

        gameManager.plugin.server.onlinePlayers.forEach { player ->
            player.gameMode = GameMode.SURVIVAL
            bingoBoardInventory.open(player)
        }
    }

    override fun endState() {
        super.endState()
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (!isActive) return
        checkIfBingoItem(event.cursor.type, event.whoClicked as Player, gameManager)
        event.whoClicked.inventory.forEach {
            if (it != null && !it.isEmpty) {
                checkIfBingoItem(it.type, event.whoClicked as Player, gameManager)
            }
        }
    }

    @EventHandler
    fun onItemPickup(event: EntityPickupItemEvent) {
        if (!isActive) return
        if (event.entity !is Player) return

        checkIfBingoItem(event.item.itemStack.type, event.entity as Player, gameManager)
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (!isActive) return

        event.player.inventory.forEach {
            if (it != null && !it.isEmpty) {
                checkIfBingoItem(it.type, event.player as Player, gameManager)
            }
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!isActive) return
        if (gameManager.teams.values.any { it == event.player.uniqueId }) return

        event.player.kick(Component.text("A game is running", NamedTextColor.RED))
    }

    @EventHandler
    fun onPlayerDisconnect(event: PlayerQuitEvent) {
        if (!isActive) return
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        if (!isActive) return
        if (!event.isMissingRespawnBlock) return

        val teamWorld = gameManager.teamWorlds[event.player.getBingoTeam(gameManager)]?.first
        event.respawnLocation = teamWorld?.spawnLocation ?: event.respawnLocation
    }

    @EventHandler
    fun onPlayerPortalReady(event: EntityPortalReadyEvent) {
        if (!isActive) return
        if (event.portalType == PortalType.ENDER || event.portalType == PortalType.END_GATEWAY) {
            event.isCancelled = true
            return
        }

        event.targetWorld = gameManager.teamWorlds.values.filterNotNull().find { (overworld, _) ->
            overworld == event.entity.world
        }?.second ?: gameManager.teamWorlds.values.filterNotNull().find { (_, nether) ->
            nether == event.entity.world
        }?.first ?: event.targetWorld
    }

    @EventHandler
    fun onPlayerPortalReady(event: PlayerPortalEvent) {
        if (!isActive) return
        if (event.cause == PlayerTeleportEvent.TeleportCause.END_PORTAL || event.cause == PlayerTeleportEvent.TeleportCause.END_GATEWAY) {
            event.isCancelled = true
            return
        }
    }

}