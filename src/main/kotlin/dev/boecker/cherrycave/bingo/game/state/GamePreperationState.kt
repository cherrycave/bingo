package dev.boecker.cherrycave.bingo.game.state

import com.destroystokyo.paper.MaterialTags
import dev.boecker.cherrycave.bingo.game.BingoGameManager
import dev.boecker.cherrycave.bingo.game.state.prepare.getResourcePackForMaterials
import dev.boecker.cherrycave.bingo.game.state.prepare.resourcePackGenUrl
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
                it != Material.TEST_INSTANCE_BLOCK &&
                it != Material.FARMLAND
    }

    var scheduler: Int? = null

    var finishedPlayerInits: Int = 0
    var finishedTeamInits: Int = 0

    var sideBar: Sidebar? = null
    var sideBarLayout: ComponentSidebarLayout? = null
    var teamSideBarComponents: MutableMap<BingoTeams, TeamScoreboardComponent> = mutableMapOf()

    override fun startState() {
        super.startState()

        finishedPlayerInits = 0
        finishedTeamInits = 0

        teamSideBarComponents = mutableMapOf()

        scheduler = gameManager.plugin.server.scheduler.scheduleSyncRepeatingTask(gameManager.plugin, {
            gameManager.plugin.server.sendActionBar(Component.text("Preparing game...", NamedTextColor.GREEN))
        }, 0L, 20)

        val seed = Random.nextLong()

        val newBoard = mutableListOf<Material>()

        (0 until (gameManager.bingoConfiguration.boardSize * gameManager.bingoConfiguration.boardSize)).forEach { _ ->
            var item = bingoMaterials.random()
            while (newBoard.contains(item)) {
                item = bingoMaterials.random()
            }
            newBoard.add(item)
        }

        gameManager.bingoBoard = newBoard.toList()

        gameManager.plugin.coroutineScope.launch {
            val resourcePackResponse =
                getResourcePackForMaterials(gameManager.bingoBoard!!, gameManager.bingoConfiguration.boardSize)

            val (hash, downloadPath) = resourcePackResponse

            gameManager.plugin.server.onlinePlayers.forEach { player ->
                player.setResourcePack("${resourcePackGenUrl}${downloadPath}", hash, true)

                player.activeBossBars().forEach { bossBar ->
                    player.hideBossBar(bossBar)
                }

                player.showBossBar(
                    BossBar.bossBar(
                        Component.text("\uE000", TextColor.fromHexString("#FE01FE")), 0f, BossBar.Color.RED,
                        BossBar.Overlay.PROGRESS
                    )
                )
            }
        }

        val filledTeams = gameManager.teams.filter { it.value.isNotEmpty() }

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

        for (team in filledTeams) {
            teamSideBarComponents[team.key] = TeamScoreboardComponent(gameManager, team.key)

            val overworldCreator = WorldCreator(
                NamespacedKey(
                    gameManager.plugin,
                    team.key.teamName
                )
            ).seed(seed).forcedSpawnPosition(Position.block(0, 100, 0), 0f, 0f)
            val overworld = overworldCreator.createWorld()!!
            val nether = WorldCreator(
                NamespacedKey(
                    gameManager.plugin,
                    "${team.key.teamName}_nether"
                )
            ).environment(World.Environment.NETHER).seed(seed).forcedSpawnPosition(Position.block(0, 100, 0), 0f, 0f)
                .createWorld()!!

            // Set gamerules
            overworld.setGameRule(GameRules.PVP, false)
            nether.setGameRule(GameRules.PVP, false)
            overworld.setGameRule(GameRules.KEEP_INVENTORY, gameManager.bingoConfiguration.keepInventory)
            nether.setGameRule(GameRules.KEEP_INVENTORY, gameManager.bingoConfiguration.keepInventory)
            overworld.difficulty = gameManager.bingoConfiguration.minecraftDifficulty
            nether.difficulty = gameManager.bingoConfiguration.minecraftDifficulty

            gameManager.teamWorlds[team.key] = overworld to nether

            gameManager.plugin.server.scheduler.runTaskAsynchronously(gameManager.plugin) { _ ->
                val spawnPosition =
                    (overworld as CraftWorld).handle.chunkSource.randomState().sampler().findSpawnPosition()
                val spawnLocation = Location(
                    overworld, spawnPosition.x.toDouble(),
                    spawnPosition.y.toDouble(), spawnPosition.z.toDouble()
                )
                gameManager.plugin.server.scheduler.runTask(gameManager.plugin) { _ ->
                    overworld.getChunkAtAsync(spawnLocation).thenAccept { chunk ->
                        spawnLocation.y = chunk.world.getHighestBlockYAt(
                            spawnLocation.x.toInt(),
                            spawnLocation.z.toInt()
                        ) + 1.0
                        overworld.spawnLocation = spawnLocation
                        team.value.forEach { teamUUID ->
                            gameManager.plugin.server.onlinePlayers.filter { it.uniqueId == teamUUID }.forEach {
                                it.respawnLocation = overworld.spawnLocation
                            }
                        }
                        finishedTeamInits++
                    }
                }
            }
        }

        sideBar = gameManager.plugin.scoreboardLibrary.createSidebar()

        sideBarLayout = ComponentSidebarLayout(
            SidebarComponent.staticLine(
                Component.text(
                    "Bingo", NamedTextColor.BLUE,
                    TextDecoration.BOLD
                )
            ),
            SidebarComponent.builder().run {
                var componentBuilder: SidebarComponent.Builder = this
                teamSideBarComponents.values.forEach {
                    componentBuilder = componentBuilder.addComponent(it)
                }
                componentBuilder
            }.build()
        )

        gameManager.plugin.server.scheduler.runTaskTimer(gameManager.plugin, { task ->
            if (finishedPlayerInits == filledTeams.map { it.value.size }
                    .sum() && finishedTeamInits == filledTeams.size) {
                gameManager.plugin.server.onlinePlayers.forEach { player ->
                    player.teleport(gameManager.teamWorlds[player.getBingoTeam(gameManager)]!!.first.spawnLocation)
                }
                gameManager.nextState()
                task.cancel()
            }
        }, 0L, 1L)
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