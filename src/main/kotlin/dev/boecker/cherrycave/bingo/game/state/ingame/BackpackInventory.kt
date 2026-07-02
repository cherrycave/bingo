package dev.boecker.cherrycave.bingo.game.state.ingame

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.window.Window

fun backPackInventory() = Window.builder()
    .setTitle(Component.text("Backpack", NamedTextColor.GOLD))
    .setUpperGui(Gui.builder()
    .setStructure(
        "# # # # # # # # #",
        "# x x x x x x x #",
        "# x x x x x x x #",
        "# x x x x x x x #",
        "# # # # # # # # #",
    )
    .addIngredient('#', Item.simple(ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("")))
    .addIngredient('x', VirtualInventory(7*3))
    .build())
