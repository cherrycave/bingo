package dev.boecker.cherrycave.bingo.game.state.ingame

import dev.boecker.cherrycave.bingo.game.BingoGameManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.window.Window

fun backPackInventory(gameManager: BingoGameManager) = Window.builder()
    .setTitle(Component.text("Backpack", NamedTextColor.GOLD))
    .setUpperGui(Gui.builder()
    .setStructure(
        *(0 until gameManager.bingoConfiguration.backPackSize).map { "x x x x x x x x x" }.toTypedArray(),
    )
    .addIngredient('x', VirtualInventory(9*gameManager.bingoConfiguration.backPackSize))
    .build())
