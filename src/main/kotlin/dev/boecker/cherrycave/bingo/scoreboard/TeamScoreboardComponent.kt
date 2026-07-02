package dev.boecker.cherrycave.bingo.scoreboard

import dev.boecker.cherrycave.bingo.game.BingoGameManager
import dev.boecker.cherrycave.bingo.game.team.BingoTeams
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.megavex.scoreboardlibrary.api.sidebar.component.LineDrawable
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent

class TeamScoreboardComponent(val gameManager: BingoGameManager, val team: BingoTeams) : SidebarComponent {

    override fun draw(drawable: LineDrawable) {
        val teamMembers = gameManager.teams[team]!!

        val teamLine = Component.join(
            JoinConfiguration.spaces(), Component.text(team.teamName, team.teamColor),
            gameManager.mm.deserialize(teamMembers.joinToString(" ") { "<head:${it}>" }),
            Component.text(
                "(${gameManager.ingameState.collectedItems[team]?.size}/${gameManager.bingoConfiguration.boardSize * gameManager.bingoConfiguration.boardSize})",
                NamedTextColor.GRAY
            )
        )

        drawable.drawLine(Component.text(""))
        drawable.drawLine(teamLine)
    }

}