package dev.boecker.cherrycave.bingo.command

import com.mojang.brigadier.Command
import dev.boecker.cherrycave.bingo.game.BingoGameManager
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

fun bingoBoardCommand(gameManager: BingoGameManager) = Commands.literal("board").executes {
    if (it.source.sender !is Player) return@executes Command.SINGLE_SUCCESS

    if (!gameManager.ingameState.isActive && !gameManager.gameOverState.isActive) {
        it.source.sender.sendMessage(Component.text("Game is not running", NamedTextColor.RED))
        return@executes Command.SINGLE_SUCCESS
    }

    gameManager.ingameState.bingoBoardInventory.open(it.source.sender as Player)

    return@executes Command.SINGLE_SUCCESS
}