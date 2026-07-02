package dev.boecker.cherrycave.bingo.game.state.prepare

import dev.boecker.cherrycave.bingo.game.state.GamePreperationState
import dev.boecker.cherrycave.bingo.scoreboard.TeamScoreboardComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.megavex.scoreboardlibrary.api.sidebar.component.ComponentSidebarLayout
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent

fun GamePreperationState.initializeSidebar() {
    filledTeams.forEach { team ->
        teamSideBarComponents[team.key] = TeamScoreboardComponent(gameManager, team.key)
    }

    sideBar = gameManager.plugin.scoreboardLibrary.createSidebar()

    sideBarLayout = ComponentSidebarLayout(
        SidebarComponent.staticLine(
            Component.text(
                "Bingo", NamedTextColor.BLUE,
                TextDecoration.BOLD
            )
        ),
        SidebarComponent.builder().run {
            var componentBuilder: SidebarComponent.Builder = this
            teamSideBarComponents.values.forEach {
                componentBuilder = componentBuilder.addComponent(it)
            }
            componentBuilder
        }.build()
    )
}