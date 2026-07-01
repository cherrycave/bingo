package dev.boecker.cherrycave.bingo.game.state.lobby

import dev.boecker.cherrycave.bingo.game.BingoGameManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.window.Window

fun gameConfigurationGUI(gameManager: BingoGameManager) = Window.builder()
    .setTitle(Component.text("Team Selection", NamedTextColor.BLUE))
    .setUpperGui(
        Gui.builder().setStructure(
            "# # # # # # # # #",
            "# b # # # # # p #",
            "# # # # # # # # #",
        ).addIngredient('#', Item.simple(ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("")))
            .addIngredient('p', timerPauseItem(gameManager))
            .addIngredient('b', boardSizeItem(gameManager)).build()
    )

fun timerPauseItem(gameManager: BingoGameManager) = BoundItem.builder().setItemProvider { _ ->
    var itemBuilder = ItemBuilder(Material.CLOCK)
    itemBuilder = if (gameManager.lobbyState.paused) {
        itemBuilder.setName(Component.text("Resume Start Timer", NamedTextColor.GREEN))
    } else {
        itemBuilder.setName(Component.text("Pause Start Timer", NamedTextColor.GOLD))
    }

    itemBuilder
}.addClickHandler { item, _, _ ->
    gameManager.lobbyState.paused = !gameManager.lobbyState.paused
    item.notifyWindows()
}

fun boardSizeItem(gameManager: BingoGameManager) = BoundItem.builder().setItemProvider { _ ->
    val currentBoardSize = gameManager.bingoConfiguration.boardSize
    val itemBuilder = ItemBuilder(Material.TARGET).setName(gameManager.mm.deserialize("<gray>Board Size: <blue>${currentBoardSize}")).setLore(listOf(
        gameManager.mm.deserialize("<gray>Left-Click to <green>increase"),
        gameManager.mm.deserialize("<gray>Left-Click to <red>decrease")))

    itemBuilder
}.addClickHandler { item, _, click ->
    if (click.clickType == ClickType.LEFT) {
        val config = gameManager.bingoConfiguration
        if (config.boardSize >= 6) return@addClickHandler
        gameManager.bingoConfiguration = gameManager.bingoConfiguration.copy(boardSize = config.boardSize + 1)
    } else if (click.clickType == ClickType.RIGHT) {
        val config = gameManager.bingoConfiguration
        if (config.boardSize <= 2) return@addClickHandler
        gameManager.bingoConfiguration = gameManager.bingoConfiguration.copy(boardSize = config.boardSize - 1)
    }
    item.notifyWindows()
}

