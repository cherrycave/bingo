package dev.boecker.cherrycave.bingo.game.state.prepare

import dev.boecker.cherrycave.bingo.game.state.GamePreperationState
import dev.boecker.cherrycave.bingo.game.team.BingoTeams
import io.papermc.paper.math.Position
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.bukkit.*
import org.bukkit.craftbukkit.CraftWorld
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import kotlin.random.Random

fun GamePreperationState.generateWorlds(worldGenCompletion: Consumer<Map<BingoTeams, Pair<World, World>>>) {
    val seed = Random.nextLong()

    val spawnSelection = Channel<Pair<BingoTeams, Pair<World, World>>>()

    for (team in filledTeams) {
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

        gameManager.plugin.server.scheduler.runTaskAsynchronously(gameManager.plugin) { _ ->
            val spawnPosition =
                (overworld as CraftWorld).handle.chunkSource.randomState().sampler().findSpawnPosition()
            val spawnLocation = Location(
                overworld, spawnPosition.x.toDouble(),
                spawnPosition.y.toDouble(), spawnPosition.z.toDouble()
            )
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
                gameManager.plugin.coroutineScope.launch {
                    spawnSelection.send(team.key to (overworld to nether))
                }
            }
        }
    }

    gameManager.plugin.coroutineScope.launch {
        val receivedSpawns = mutableListOf<Pair<BingoTeams, Pair<World, World>>>()
        repeat(filledTeams.size) {
            receivedSpawns.add(spawnSelection.receive())
        }
        gameManager.plugin.server.scheduler.runTask(gameManager.plugin) { _ ->
            worldGenCompletion.accept(receivedSpawns.toMap())
        }
    }
}