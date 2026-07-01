package dev.boecker.cherrycave.bingo.game.state

import com.destroystokyo.paper.MaterialTags
import dev.boecker.cherrycave.bingo.game.BingoGameManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Difficulty
import org.bukkit.GameRules
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.generator.BiomeProvider
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
                // Not obtainable in survival or too difficult
                !MaterialTags.INFESTED_BLOCKS.isTagged(it) &&
                !MaterialTags.SKULLS.isTagged(it) &&
                !MaterialTags.SPAWN_EGGS.isTagged(it) &&
                !MaterialTags.COMMAND_BLOCKS.isTagged(it) &&
                it != Material.END_PORTAL_FRAME &&
                it != Material.END_PORTAL &&
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
                it != Material.PETRIFIED_OAK_SLAB &&
                it != Material.FARMLAND
    }

    override fun startState() {
        super.startState()

        gameManager.plugin.server.onlinePlayers.forEach { player ->
            player.inventory.clear()
        }

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

        val filledTeams = gameManager.teams.filter { it.value.isNotEmpty() }

        for (team in filledTeams) {
            val overworld = WorldCreator(NamespacedKey(
                gameManager.plugin,
                team.key.teamName
            )).seed(seed).createWorld()!!
            val nether = WorldCreator(
                NamespacedKey(
                    gameManager.plugin,
                    "${team.key.teamName}_nether"
                )
            ).environment(World.Environment.NETHER).seed(seed).createWorld()!!
            overworld.setGameRule(GameRules.PVP, false)
            nether.setGameRule(GameRules.PVP, false)
            overworld.difficulty = gameManager.bingoConfiguration.minecraftDifficulty
            nether.difficulty = gameManager.bingoConfiguration.minecraftDifficulty

            gameManager.teamWorlds[team.key] = overworld to nether

            team.value.forEach { teamUUIDs ->
                gameManager.plugin.server.onlinePlayers.filter { it.uniqueId == teamUUIDs }.forEach {
                    it.teleport(overworld.spawnLocation)
                    it.respawnLocation = overworld.spawnLocation
                }
            }
        }

        gameManager.nextState()
    }

    override fun endState() {
        super.endState()
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

}