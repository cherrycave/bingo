package dev.boecker.cherrycave.bingo.game.state

import com.destroystokyo.paper.MaterialTags
import dev.boecker.cherrycave.bingo.game.BingoGameManager
import dev.boecker.cherrycave.bingo.game.item.ItemDifficulty
import dev.boecker.cherrycave.bingo.game.state.prepare.generateWorlds
import dev.boecker.cherrycave.bingo.game.state.prepare.getResourcePackForMaterials
import dev.boecker.cherrycave.bingo.game.state.prepare.initializeSidebar
import dev.boecker.cherrycave.bingo.game.state.prepare.initializeTeams
import dev.boecker.cherrycave.bingo.game.state.prepare.resourcePackGenUrl
import dev.boecker.cherrycave.bingo.game.state.prepare.setBossbar
import dev.boecker.cherrycave.bingo.game.state.prepare.setResourcePack
import dev.boecker.cherrycave.bingo.game.team.BingoTeams
import dev.boecker.cherrycave.bingo.game.team.getBingoTeam
import dev.boecker.cherrycave.bingo.scoreboard.TeamScoreboardComponent
import io.papermc.paper.math.Position
import kotlinx.coroutines.launch
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar
import net.megavex.scoreboardlibrary.api.sidebar.component.ComponentSidebarLayout
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent
import org.bukkit.*
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.UUID
import kotlin.math.floor
import kotlin.random.Random

class GamePreperationState(manager: BingoGameManager) : GameState(manager) {

    val bingoMaterials = Material.entries.filter {
        !it.isLegacy && (it.isItem) &&
                // End Items
                !MaterialTags.PURPUR.isTagged(it) &&
                !it.name.contains("END_STONE") &&
                it != Material.END_ROD &&
                it != Material.ELYTRA &&
                it != Material.DRAGON_HEAD &&
                it != Material.CHORUS_FRUIT &&
                it != Material.SHULKER_SHELL &&
                it != Material.DRAGON_BREATH &&
                it != Material.DRAGON_EGG &&
                !Tag<Material>.SHULKER_BOXES.isTagged(it) &&
                // Oxidized Copper
                !MaterialTags.OXIDIZED_COPPER_BLOCKS.isTagged(it) &&
                !MaterialTags.WEATHERED_COPPER_BLOCKS.isTagged(it) &&
                // Not obtainable in survival or too difficult
                !MaterialTags.INFESTED_BLOCKS.isTagged(it) &&
                !MaterialTags.SKULLS.isTagged(it) &&
                !MaterialTags.SPAWN_EGGS.isTagged(it) &&
                !MaterialTags.COMMAND_BLOCKS.isTagged(it) &&
                it != Material.END_PORTAL_FRAME &&
                it != Material.END_PORTAL &&
                it != Material.BEACON &&
                it != Material.TRIAL_SPAWNER &&
                it != Material.VAULT &&
                it != Material.SUSPICIOUS_SAND &&
                it != Material.SUSPICIOUS_GRAVEL &&
                it != Material.AIR &&
                it != Material.BARRIER &&
                it != Material.SPAWNER &&
                it != Material.KNOWLEDGE_BOOK &&
                it != Material.DEBUG_STICK &&
                it != Material.REINFORCED_DEEPSLATE &&
                it != Material.BUDDING_AMETHYST &&
                it != Material.CHORUS_PLANT &&
                it != Material.DIRT_PATH &&
                it != Material.FROSTED_ICE &&
                it != Material.STRUCTURE_BLOCK &&
                it != Material.BEDROCK &&
                it != Material.STRUCTURE_VOID &&
                it != Material.LIGHT &&
                it != Material.JIGSAW &&
                it != Material.FROGSPAWN &&
                it != Material.PETRIFIED_OAK_SLAB &&
                it != Material.FARMLAND &&
                it != Material.TEST_INSTANCE_BLOCK &&
                it != Material.TEST_BLOCK
    }

    var scheduler: Int? = null

    var finishedPlayerInits: Int = 0
    var finishedTeamInits: Int = 0

    var sideBar: Sidebar? = null
    var sideBarLayout: ComponentSidebarLayout? = null
    var teamSideBarComponents: MutableMap<BingoTeams, TeamScoreboardComponent> = mutableMapOf()

    var resourcePack: Pair<String, String>? = null

    var filledTeams: Map<BingoTeams, MutableList<UUID>> = mapOf()

    override fun startState() {
        super.startState()

        finishedPlayerInits = 0
        finishedTeamInits = 0

        teamSideBarComponents = mutableMapOf()

        scheduler = gameManager.plugin.server.scheduler.scheduleSyncRepeatingTask(gameManager.plugin, {
            gameManager.plugin.server.sendActionBar(Component.text("Preparing game...", NamedTextColor.GREEN))
        }, 0L, 20)

        val newBoard = mutableListOf<Material>()

        val difficulty = gameManager.bingoConfiguration.boardDifficulty

        val boardSize = gameManager.bingoConfiguration.boardSize * gameManager.bingoConfiguration.boardSize
        difficulty.itemDifficulties.forEach { (percentage, itemDifficulty) ->
            val items = floor((percentage.toFloat() / 100.0) * boardSize).toInt()
            (0 until items).forEach { _ ->
                var item =
                    Material.entries.filter { !it.isLegacy && it.isItem && itemDifficulty.itemTag.contains(it.key()) }
                        .random()

                while (newBoard.contains(item)) {
                    item = Material.entries.filter {
                        !it.isLegacy && it.isItem && itemDifficulty.itemTag.contains(it.key())
                    }.random()
                }
                newBoard.add(item)
            }
        }
        while (newBoard.size < boardSize) {
            val itemDifficulty = difficulty.itemDifficulties.maxBy { it.first }.second
            var item =
                Material.entries.filter { !it.isLegacy && it.isItem && itemDifficulty.itemTag.contains(it.key()) }
                    .random()

            while (newBoard.contains(item)) {
                item = Material.entries.filter {
                    !it.isLegacy && it.isItem && itemDifficulty.itemTag.contains(it.key())
                }.random()
            }
            newBoard.add(item)
        }

        newBoard.shuffle()

        gameManager.bingoBoard = newBoard.toList()

        gameManager.plugin.server.onlinePlayers.forEach { player ->
            player.inventory.clear()

            player.setBossbar()
        }

        setResourcePack()

        filledTeams = gameManager.teams.filter { it.value.isNotEmpty() }

        initializeTeams()

        generateWorlds { worlds ->
            gameManager.teamWorlds = worlds

            gameManager.plugin.server.scheduler.runTaskTimer(gameManager.plugin, { task ->
                if (finishedPlayerInits == filledTeams.map { it.value.size }.sum()) {
                    gameManager.plugin.server.onlinePlayers.forEach { player ->
                        player.teleport(gameManager.teamWorlds[player.getBingoTeam(gameManager)]!!.first.spawnLocation)
                    }
                    gameManager.nextState()
                    task.cancel()
                }
            }, 0L, 1L)
        }

        initializeSidebar()
    }

    override fun endState() {
        if (scheduler != null) {
            gameManager.plugin.server.scheduler.cancelTask(scheduler!!)
            scheduler = null
        }

        super.endState()
    }

    @EventHandler
    fun onPlayerResourcePackStatus(event: PlayerResourcePackStatusEvent) {
        if (event.status == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            val team = event.player.getBingoTeam(gameManager)

            if (team != null) {
                finishedPlayerInits++
            }
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!isActive) return
        if (gameManager.teams.values.any { it.contains(event.player.uniqueId) }) return

        event.player.kick(Component.text("A game is running", NamedTextColor.RED))
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!isActive) return
        if (event.hand != EquipmentSlot.HAND) return

        event.isCancelled = true
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (!isActive) return
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