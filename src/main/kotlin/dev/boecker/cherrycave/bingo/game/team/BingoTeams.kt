package dev.boecker.cherrycave.bingo.game.team

import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material

enum class BingoTeams(val teamName: String, val teamColor: NamedTextColor, val block: Material) {
    PURPLE("Purple", NamedTextColor.LIGHT_PURPLE, Material.PURPLE_WOOL),
    BLUE("Blue", NamedTextColor.BLUE, Material.BLUE_WOOL),
    GREEN("Green", NamedTextColor.GREEN, Material.LIME_WOOL),
    AQUA("Aqua", NamedTextColor.AQUA, Material.CYAN_WOOL),
    RED("Red", NamedTextColor.RED, Material.RED_WOOL),
    YELLOW("Yellow", NamedTextColor.YELLOW, Material.YELLOW_WOOL),
}