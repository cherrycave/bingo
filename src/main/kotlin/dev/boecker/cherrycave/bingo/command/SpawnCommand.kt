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

fun bingoSpawnCommand(gameManager: BingoGameManager) = Commands.literal("spawn").executes {
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
    player.teleport(gameManager.teamWorlds[player.getBingoTeam(gameManager)]!!.first.spawnLocation)

    player.playSound(Sound.sound(Key.key("entity.player.teleport"), Sound.Source.PLAYER, 1f, 1f), Sound.Emitter.self())

    return@executes Command.SINGLE_SUCCESS
}