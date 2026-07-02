package dev.boecker.cherrycave.bingo.command

import com.mojang.brigadier.Command
import dev.boecker.cherrycave.bingo.game.BingoGameManager
import dev.boecker.cherrycave.bingo.game.team.getBingoTeam
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.entity.Player

fun bingoUpCommand(gameManager: BingoGameManager) = Commands.literal("up").executes {
    if (it.source.sender !is Player) return@executes Command.SINGLE_SUCCESS

    if (!gameManager.ingameState.isActive) {
        it.source.sender.sendMessage(Component.text("Game is not running", NamedTextColor.RED))
        return@executes Command.SINGLE_SUCCESS
    }

    if (!gameManager.bingoConfiguration.allowTeleportCommands) {
        it.source.sender.sendMessage(Component.text("This command is disabled for this game", NamedTextColor.RED))
        return@executes Command.SINGLE_SUCCESS
    }

    val player = (it.source.sender as Player)
    if (player.world.environment != World.Environment.NORMAL) {
        player.sendMessage(Component.text("This command only works in the overworld", NamedTextColor.RED))
        return@executes Command.SINGLE_SUCCESS
    }

    val y = player.world.getHighestBlockYAt(player.location) + 1
    val newLocation = player.location
    newLocation.y = y.toDouble()
    player.teleport(newLocation)

    player.playSound(Sound.sound(Key.key("entity.player.teleport"), Sound.Source.PLAYER, 1f, 1f), Sound.Emitter.self())

    return@executes Command.SINGLE_SUCCESS
}