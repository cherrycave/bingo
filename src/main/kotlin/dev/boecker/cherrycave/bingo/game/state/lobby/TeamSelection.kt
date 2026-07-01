package dev.boecker.cherrycave.bingo.game.state.lobby

import dev.boecker.cherrycave.bingo.game.BingoGameManager
import dev.boecker.cherrycave.bingo.game.team.BingoTeams
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.window.Window

fun teamSelectionGUI(gameManager: BingoGameManager) = Window.builder()
    .setTitle(Component.text("Team Selection", NamedTextColor.BLUE))
    .setUpperGui(
        Gui.builder().setStructure(
            "# # # # # # # # #",
            "# p b g # a r y #",
            "# # # # # # # # #",
        ).addIngredient('#', Item.simple(ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("")))
            .addIngredient('p', createTeamItem(gameManager, BingoTeams.PURPLE))
            .addIngredient('b', createTeamItem(gameManager, BingoTeams.BLUE))
            .addIngredient('g', createTeamItem(gameManager, BingoTeams.GREEN))
            .addIngredient('a', createTeamItem(gameManager, BingoTeams.AQUA))
            .addIngredient('r', createTeamItem(gameManager, BingoTeams.RED))
            .addIngredient('y', createTeamItem(gameManager, BingoTeams.YELLOW)).build()
    )

fun createTeamItem(gameManager: BingoGameManager, team: BingoTeams): BoundItem.Builder<Gui> =
    BoundItem.builder().setItemProvider { player ->
        var builder = ItemBuilder(team.block).setName(Component.text(team.teamName, team.teamColor))
            .setLore(gameManager.teams[team]!!.map { teamUUIDs ->
                Component.text(
                    gameManager.plugin.server.onlinePlayers.find { teamUUIDs == it.uniqueId }?.name ?: "???"
                )
            })

        if (gameManager.teams[team]!!.contains(player.uniqueId)) {
            builder = builder.setGlint(true)
        }

        builder
    }.addClickHandler { item, gui, click ->
        val itemProvider = item.getItemProvider(click.player)
        val clickedTeam = gameManager.teams[BingoTeams.entries.find { it.block == itemProvider.get().type }]!!
        if (clickedTeam.contains(click.player.uniqueId)) {
            return@addClickHandler
        }
        clickedTeam.add(click.player.uniqueId)
        gameManager.teams.forEach { (previousTeam, uuids) ->
            if (uuids.contains(click.player.uniqueId) && previousTeam != team) {
                uuids.remove(click.player.uniqueId)
            }
        }
        gameManager.lobbyState.checkStartRequirements()
        gui.notifyWindows()
    }