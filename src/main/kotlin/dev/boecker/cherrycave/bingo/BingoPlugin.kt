package dev.boecker.cherrycave.bingo

import dev.boecker.cherrycave.bingo.command.bingoBoardCommand
import dev.boecker.cherrycave.bingo.game.BingoGameManager
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries


class BingoPlugin : JavaPlugin() {

    lateinit var bingoManager: BingoGameManager

    @OptIn(ExperimentalPathApi::class)
    override fun onEnable() {
        val bingoWorldFiles = server.levelDirectory.resolve("dimensions").resolve("bingo")
        if (bingoWorldFiles.exists()) {
            bingoWorldFiles.listDirectoryEntries().forEach { file ->
                file.deleteRecursively()
            }
        }

        bingoManager = BingoGameManager(this)

        this.lifecycleManager.registerEventHandler(
            LifecycleEvents.COMMANDS, { commands ->
                commands.registrar().register(bingoBoardCommand(bingoManager).build())
            })
    }

}