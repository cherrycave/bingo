package dev.boecker.cherrycave.bingo

import dev.boecker.cherrycave.bingo.command.bingoBackpackCommand
import dev.boecker.cherrycave.bingo.command.bingoBoardCommand
import dev.boecker.cherrycave.bingo.command.bingoSpawnCommand
import dev.boecker.cherrycave.bingo.command.bingoUpCommand
import dev.boecker.cherrycave.bingo.game.BingoGameManager
import dev.boecker.cherrycave.slpf.SimpleLuckPermsFormatter
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries


class BingoPlugin : JavaPlugin() {

    lateinit var bingoManager: BingoGameManager

    var slpf: SimpleLuckPermsFormatter? = null

    @OptIn(ExperimentalPathApi::class)
    override fun onEnable() {
        slpf = server.pluginManager.getPlugin("SimpleLuckPermsFormatter") as SimpleLuckPermsFormatter?

        val bingoWorldFiles = server.levelDirectory.resolve("dimensions").resolve("bingo")
        if (bingoWorldFiles.exists()) {
            bingoWorldFiles.listDirectoryEntries().forEach { file ->
                file.deleteRecursively()
            }
        }

        bingoManager = BingoGameManager(this)

        this.lifecycleManager.registerEventHandler(
            LifecycleEvents.COMMANDS
        ) { commands ->
            commands.registrar().register(bingoBoardCommand(bingoManager).build())
            commands.registrar().register(bingoSpawnCommand(bingoManager).build())
            commands.registrar().register(bingoUpCommand(bingoManager).build())
            commands.registrar().register(bingoBackpackCommand(bingoManager).build(), listOf("bp"))
        }
    }

}