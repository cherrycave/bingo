package dev.boecker.cherrycave.bingo.game.state.ingame

import dev.boecker.cherrycave.bingo.game.BingoGameManager
import dev.boecker.cherrycave.bingo.game.team.getBingoTeam
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.Material
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.window.Window
import kotlin.math.floor

fun bingoBoardInventory(size: Int, board: List<Material>, gameManager: BingoGameManager) = Window.builder()
    .setTitle(Component.text("Bingo Board", NamedTextColor.BLUE))
    .setUpperGui(
        PagedGui.itemsBuilder().setStructure(*generateGUIStructure(size))
            .addIngredient(
                '#', Item.simple(ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(""))
            ).addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .setContent(board.map { material ->
                Item.builder().setItemProvider { player ->
                    val team = player.getBingoTeam(gameManager)
                    val itemBuilder = ItemBuilder(material)
                    itemBuilder.setLore(gameManager.teams.filter {
                        gameManager.ingameState.collectedItems[it.key]?.contains(
                            material
                        ) ?: false
                    }.map { (team, _) ->
                        Component.join(
                            JoinConfiguration.spaces(),
                            Component.text(
                                team.teamName,
                                team.teamColor
                            ),
                            Component.text("collected at", NamedTextColor.GREEN),
                            Component.text(
                                gameManager.ingameState.collectedItems[team]!![material]!!.formatBingoTime(),
                                NamedTextColor.BLUE
                            )
                        )
                    })
                    if (gameManager.ingameState.collectedItems[team]?.contains(material) ?: false) {
                        itemBuilder.setGlint(true)
                    }

                    itemBuilder
                }.build()
            }).build()
    )

fun generateGUIStructure(size: Int): Array<String> {
    val basicString = (0 until minOf(size, 6)).map {
        "#########".toMutableList()
    }.toMutableList()
    val startRow = floor((basicString.size - size) / 2.0).toInt()
    val endRow = startRow + size
    val startColumn = floor((9.0 - size) / 2.0).toInt()
    val endColumn = startColumn + size
    basicString.forEachIndexed { i, _ ->
        if (i in startRow until endRow) {
            for (j in startColumn until endColumn) {
                basicString[i][j] = 'x'
            }
        }
    }
    return basicString.map { it.joinToString("") }.toTypedArray()
}