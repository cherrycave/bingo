package dev.boecker.cherrycave.bingo.game.state

import dev.boecker.cherrycave.bingo.game.BingoGameManager
import dev.boecker.cherrycave.bingo.game.state.ingame.backPackInventory
import dev.boecker.cherrycave.bingo.game.state.ingame.bingoBoardInventory
import dev.boecker.cherrycave.bingo.game.state.ingame.checkIfBingoItem
import dev.boecker.cherrycave.bingo.game.state.ingame.formatBingoTime
import dev.boecker.cherrycave.bingo.game.state.prepare.initializeTeams
import dev.boecker.cherrycave.bingo.game.state.prepare.setBingoResourcePack
import dev.boecker.cherrycave.bingo.game.state.prepare.setBossbar
import dev.boecker.cherrycave.bingo.game.team.BingoTeams
import dev.boecker.cherrycave.bingo.game.team.getBingoTeam
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.event.entity.EntityPortalReadyEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.apache.commons.lang3.time.DateUtils
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.PortalType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.window.Window
import java.time.format.DateTimeFormatter
import java.util.function.BiConsumer
import java.util.function.Consumer
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class IngameState(manager: BingoGameManager) : GameState(manager) {

    lateinit var bingoBoardInventory: Window.Builder.Normal.Split

    lateinit var collectedItems: Map<BingoTeams, MutableMap<Material, Long>>

    lateinit var backpacks: Map<BingoTeams, Window.Builder.Normal.Split>

    lateinit var backPackItem: ItemStack

    lateinit var collectItemDispatch: BiConsumer<BingoTeams, Material>

    var startTime: Long = 0
    var timerScheduler: Int = -1

    override fun startState() {
        super.startState()

        startTime = Clock.System.now().epochSeconds

        collectedItems = BingoTeams.entries.associateWith { team -> mutableMapOf() }
        backpacks = BingoTeams.entries.associateWith { team -> backPackInventory(gameManager) }

        val backPackItemstack = ItemStack(Material.BUNDLE)
        val backPackItemMeta = backPackItemstack.itemMeta
        backPackItemMeta.itemName(Component.text("Backpack", NamedTextColor.GOLD))
        backPackItemMeta.lore(listOf(Component.text("Right-click to open", NamedTextColor.GRAY)))
        backPackItemstack.itemMeta = backPackItemMeta
        backPackItemstack.unsetData(DataComponentTypes.BUNDLE_CONTENTS)
        backPackItem = backPackItemstack

        bingoBoardInventory =
            bingoBoardInventory(gameManager.bingoConfiguration.boardSize, gameManager.bingoBoard!!, gameManager)

        gameManager.plugin.server.onlinePlayers.forEach { player ->
            player.gameMode = GameMode.SURVIVAL
            player.inventory.setItem(17, backPackItem)
            bingoBoardInventory.open(player)
            gameManager.gamePreperationState.sideBarLayout?.apply(gameManager.gamePreperationState.sideBar!!)
            gameManager.gamePreperationState.sideBar?.addPlayer(player)
        }

        timerScheduler = gameManager.plugin.server.scheduler.scheduleSyncRepeatingTask(gameManager.plugin, {
            val currentTime = Clock.System.now().epochSeconds

            val timeDifference = currentTime - startTime

            gameManager.plugin.server.onlinePlayers.forEach { player ->
                player.sendActionBar(
                    Component.text(
                        timeDifference.formatBingoTime(),
                        NamedTextColor.BLUE,
                        TextDecoration.BOLD
                    )
                )
            }
        }, 0L, 20L)

        collectItemDispatch = { team, item ->
            gameManager.gamePreperationState.sideBarLayout?.apply(gameManager.gamePreperationState.sideBar!!)
        }
    }

    override fun endState() {
        gameManager.plugin.server.scheduler.cancelTask(timerScheduler)

        super.endState()
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (!isActive) return
        val clickedItem = event.getCurrentItem()
        if (clickedItem != null && clickedItem.isSimilar(backPackItem) && event.click.isRightClick) {
            event.isCancelled = true
            backpacks[(event.whoClicked as Player).getBingoTeam(gameManager)!!]!!.open(event.whoClicked as Player)
        }

        checkIfBingoItem(event.cursor.type, event.whoClicked as Player, gameManager)
        event.whoClicked.inventory.forEach {
            if (it != null && !it.isEmpty) {
                checkIfBingoItem(it.type, event.whoClicked as Player, gameManager)
            }
        }
    }

    @EventHandler
    fun onInteractEvent(event: PlayerInteractEvent) {
        if (!isActive) return
        if (event.hand != EquipmentSlot.HAND && (event.action != Action.RIGHT_CLICK_AIR || event.action != Action.RIGHT_CLICK_BLOCK)) {
            return
        }

        if (event.player.inventory.itemInMainHand.isSimilar(backPackItem)) {
            event.isCancelled = true
            backpacks[event.player.getBingoTeam(gameManager)!!]!!.open(event.player)
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
        if (gameManager.teams.values.any { it.contains(event.player.uniqueId) }) {
            gameManager.gamePreperationState.initializeTeams(event.player)
            event.player.setBossbar()
            gameManager.gamePreperationState.sideBar?.removePlayer(event.player)
            gameManager.gamePreperationState.sideBar?.addPlayer(event.player)
            val (downloadPath, hash) = gameManager.gamePreperationState.resourcePack!!
            event.player.setBingoResourcePack(downloadPath, hash)
            return
        }

        event.joinMessage(null)
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

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        if (!isActive) return
        if (event.itemDrop.itemStack.isSimilar(backPackItem)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        if (!isActive) return
        if (gameManager.bingoConfiguration.hunger) return

        event.isCancelled = true
    }

}