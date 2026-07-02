package dev.boecker.cherrycave.bingo.game.state.lobby

import dev.boecker.cherrycave.bingo.game.BingoGameManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Difficulty
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
            "# b t # # # # p #",
            "# d k # # # # s #",
            "# # # # # # # # #",
        ).addIngredient('#', Item.simple(ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("")))
            .addIngredient('p', timerPauseItem(gameManager))
            .addIngredient('s', skipTimerItem(gameManager))
            .addIngredient('b', boardSizeItem(gameManager))
            .addIngredient('t', allowTPCommandsItem(gameManager))
            .addIngredient('d', minecraftDifficultyItem(gameManager))
            .addIngredient('k', keepInvItem(gameManager)).build()
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

fun skipTimerItem(gameManager: BingoGameManager) = BoundItem.builder().setItemProvider { _ ->
    var itemBuilder = ItemBuilder(Material.BELL)
    itemBuilder = if (gameManager.lobbyState.timer >= 5) {
        itemBuilder.setName(Component.text("Skip Start Timer", NamedTextColor.GREEN))
    } else {
        itemBuilder.setName(Component.text("Reset Start Timer", NamedTextColor.GOLD))
    }

    itemBuilder
}.addClickHandler { item, _, _ ->
    if (gameManager.lobbyState.timer >= 5) {
        gameManager.lobbyState.timer = 5
    } else {
        gameManager.lobbyState.timer = 30
    }
    item.notifyWindows()
}

fun boardSizeItem(gameManager: BingoGameManager) = BoundItem.builder().setItemProvider { _ ->
    val currentBoardSize = gameManager.bingoConfiguration.boardSize
    val itemBuilder = ItemBuilder(Material.TARGET).setName(gameManager.mm.deserialize("<gray>Board Size: <blue>${currentBoardSize}")).setLore(listOf(
        gameManager.mm.deserialize("<gray>Left-Click to <green>increase"),
        gameManager.mm.deserialize("<gray>Right-Click to <red>decrease")))

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

fun allowTPCommandsItem(gameManager: BingoGameManager) = BoundItem.builder().setItemProvider { _ ->
    val currentValue = gameManager.bingoConfiguration.allowTeleportCommands
    val itemBuilder = ItemBuilder(Material.ENDER_PEARL).setName(gameManager.mm.deserialize("<gray>Allow Teleport commands: <blue>${currentValue}")).setLore(listOf(
        gameManager.mm.deserialize("<gray>Click to change")))

    itemBuilder
}.addClickHandler { item, _, _ ->
    val config = gameManager.bingoConfiguration
    gameManager.bingoConfiguration = gameManager.bingoConfiguration.copy(allowTeleportCommands = !config.allowTeleportCommands)
    item.notifyWindows()
}


fun minecraftDifficultyItem(gameManager: BingoGameManager) = BoundItem.builder().setItemProvider { _ ->
    val currentDifficulty = gameManager.bingoConfiguration.minecraftDifficulty
    val itemBuilder = ItemBuilder(Material.BOW).setName(gameManager.mm.deserialize("<gray>Minecraft Difficulty: <blue>${currentDifficulty.name}")).setLore(listOf(
        gameManager.mm.deserialize("<gray>Left-Click to <green>increase"),
        gameManager.mm.deserialize("<gray>Right-Click to <red>decrease")))

    itemBuilder
}.addClickHandler { item, _, click ->
    if (click.clickType == ClickType.LEFT) {
        val config = gameManager.bingoConfiguration
        if (config.minecraftDifficulty == Difficulty.HARD) return@addClickHandler
        gameManager.bingoConfiguration = gameManager.bingoConfiguration.copy(minecraftDifficulty = Difficulty.getByValue(config.minecraftDifficulty.value + 1)!!)
    } else if (click.clickType == ClickType.RIGHT) {
        val config = gameManager.bingoConfiguration
        if (config.minecraftDifficulty == Difficulty.EASY) return@addClickHandler
        gameManager.bingoConfiguration = gameManager.bingoConfiguration.copy(minecraftDifficulty = Difficulty.getByValue(config.minecraftDifficulty.value - 1)!!)
    }
    item.notifyWindows()
}

fun keepInvItem(gameManager: BingoGameManager) = BoundItem.builder().setItemProvider { _ ->
    val currentValue = gameManager.bingoConfiguration.keepInventory
    val itemBuilder = ItemBuilder(Material.CHEST).setName(gameManager.mm.deserialize("<gray>KeepInventory: <blue>${currentValue}")).setLore(listOf(
        gameManager.mm.deserialize("<gray>Click to change")))

    itemBuilder
}.addClickHandler { item, _, _ ->
    val config = gameManager.bingoConfiguration
    gameManager.bingoConfiguration = gameManager.bingoConfiguration.copy(keepInventory = !config.keepInventory)
    item.notifyWindows()
}
