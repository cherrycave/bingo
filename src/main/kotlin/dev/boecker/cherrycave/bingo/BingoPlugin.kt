package dev.boecker.cherrycave.bingo

import dev.boecker.cherrycave.bingo.command.bingoBoardCommand
import dev.boecker.cherrycave.bingo.game.BingoGameManager
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin


class BingoPlugin : JavaPlugin() {

    lateinit var bingoManager: BingoGameManager

    override fun onEnable() {
        bingoManager = BingoGameManager(this)

        this.lifecycleManager.registerEventHandler(
            LifecycleEvents.COMMANDS, { commands ->
                commands.registrar().register(bingoBoardCommand(bingoManager).build())
            })
    }

}