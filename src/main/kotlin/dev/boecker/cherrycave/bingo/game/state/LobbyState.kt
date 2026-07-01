package dev.boecker.cherrycave.bingo.game.state

import dev.boecker.cherrycave.bingo.game.BingoGameManager
import dev.boecker.cherrycave.bingo.game.state.lobby.gameConfigurationGUI
import dev.boecker.cherrycave.bingo.game.state.lobby.teamSelectionGUI
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class LobbyState(manager: BingoGameManager) : GameState(manager), Listener {

    val teamSelectionInventory = teamSelectionGUI(gameManager)
    val gameConfigInventory = gameConfigurationGUI(gameManager)

    lateinit var teamSelectionItem: ItemStack
    lateinit var gameConfigItem: ItemStack

    var starting = false
    var paused = false
    var timer = 30

    var timerSchedule: Int? = null

    val lobbyWorld = gameManager.plugin.server.getWorld(NamespacedKey.minecraft("overworld"))
    val lobbySpawn = Location(lobbyWorld, 0.0, 1.0, 0.0, 180f, 0f)

    override fun startState() {

        lobbyWorld?.setGameRule(GameRules.PVP, false)
        gameManager.winnerTeam = null
        gameManager.bingoBoard = null
        starting = false
        timer = 30
        timerSchedule = null

        val teamItem = ItemStack(Material.PURPLE_WOOL)
        val teamSelectItemMeta = teamItem.itemMeta
        teamSelectItemMeta.itemName(Component.text("Team Selection", NamedTextColor.BLUE))
        teamItem.itemMeta = teamSelectItemMeta
        teamSelectionItem = teamItem

        val configItem = ItemStack(Material.REPEATER)
        val configItemMeta = configItem.itemMeta
        configItemMeta.itemName(Component.text("Game Configuration", NamedTextColor.BLUE))
        configItem.itemMeta = configItemMeta
        gameConfigItem = configItem

        timerSchedule = gameManager.plugin.server.scheduler.scheduleSyncRepeatingTask(gameManager.plugin, {
            if (!isActive) return@scheduleSyncRepeatingTask
            checkStartRequirements()
            if (starting) {
                gameManager.plugin.server.onlinePlayers.forEach { player ->
                    if (!paused) {
                        player.sendActionBar(
                            MiniMessage.miniMessage().deserialize("<white>Game starting in <blue>$timer")
                        )
                    } else {
                        player.sendActionBar(
                            MiniMessage.miniMessage().deserialize("<red>Game start timer is paused")
                        )
                    }
                }
                if (timer <= 0) {
                    gameManager.nextState()
                }
                if (!paused) {
                    timer--
                }
            } else {
                timer = 30
                gameManager.plugin.server.onlinePlayers.forEach { player ->
                    player.sendActionBar(
                        MiniMessage.miniMessage().deserialize("<red>Waiting for players...")
                    )
                }
            }
        }, 0, 20)

        super.startState()

        gameManager.plugin.server.onlinePlayers.forEach { player ->
            giveConfigItems(player)
        }

        checkStartRequirements()
    }

    override fun endState() {
        super.endState()

        gameManager.plugin.server.scheduler.cancelTask(timerSchedule!!)
        timerSchedule = null
    }

    fun checkStartRequirements() {
        if (!isActive) return

        starting = gameManager.teams.values.filter { it.isNotEmpty() }.size >= 2
    }

    @EventHandler
    fun onPlayerSpawn(event: AsyncPlayerSpawnLocationEvent) {
        if (!isActive) return

        event.spawnLocation = lobbySpawn
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!isActive) return

        event.player.inventory.clear()

        event.player.gameMode = GameMode.ADVENTURE

        val smallestTeam = gameManager.teams.values.minByOrNull { it.size }!!

        smallestTeam.add(event.player.uniqueId)

        giveConfigItems(event.player)

        checkStartRequirements()
    }

    fun giveConfigItems(player: Player) {
        player.inventory.setItem(0, teamSelectionItem)

        if (player.hasPermission("bingo.configure")) {
            player.inventory.setItem(8, gameConfigItem)
        }
    }

    @EventHandler
    fun onPlayerDisconnect(event: PlayerQuitEvent) {
        if (!isActive) return

        gameManager.teams.values.find { teamUUIDs -> teamUUIDs.any { it == event.player.uniqueId } }
            ?.remove(event.player.uniqueId)

        checkStartRequirements()
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!isActive) return
        event.isCancelled = true
        if (event.hand != EquipmentSlot.HAND) return

        if (event.item?.isSimilar(teamSelectionItem) ?: false) {
            teamSelectionInventory.open(event.player)
        } else if (event.item?.isSimilar(gameConfigItem) ?: false) {
            gameConfigInventory.open(event.player)
        }


    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (!isActive) return

        checkStartRequirements()

        if (event.clickedInventory != event.whoClicked.inventory) return

        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerSwapHandItems(event: PlayerSwapHandItemsEvent) {
        if (!isActive) return

        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerDamage(event: EntityDamageEvent) {
        if (!isActive) return

        event.isCancelled = true
    }


}