package dev.boecker.cherrycave.bingo.game

import dev.boecker.cherrycave.bingo.BingoPlugin
import dev.boecker.cherrycave.bingo.game.configuration.BingoConfiguration
import dev.boecker.cherrycave.bingo.game.state.GameOverState
import dev.boecker.cherrycave.bingo.game.state.GamePreperationState
import dev.boecker.cherrycave.bingo.game.state.IngameState
import dev.boecker.cherrycave.bingo.game.state.LobbyState
import dev.boecker.cherrycave.bingo.game.team.BingoTeams
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.World
import xyz.xenondevs.invui.InvUI
import java.util.*
import kotlin.collections.Map

class BingoGameManager(val plugin: BingoPlugin) {

    val mm = MiniMessage.miniMessage()

    val lobbyState: LobbyState
    val gamePreperationState: GamePreperationState
    val ingameState: IngameState
    val gameOverState: GameOverState

    var bingoConfiguration = BingoConfiguration()

    val teamWorlds: MutableMap<BingoTeams, Pair<World, World>?> =
        BingoTeams.entries.toTypedArray().associateWith { null }.toMutableMap()

    val teams: Map<BingoTeams, MutableList<UUID>> =
        BingoTeams.entries.toTypedArray().associateWith { mutableListOf() }

    var bingoBoard: List<Material>? = null

    var winnerTeam: BingoTeams? = null

    init {
        InvUI.getInstance().setPlugin(plugin)

        lobbyState = LobbyState(this)
        gamePreperationState = GamePreperationState(this)
        ingameState = IngameState(this)
        gameOverState = GameOverState(this)

        lobbyState.startState()

        plugin.server.pluginManager.registerEvents(lobbyState, plugin)
        plugin.server.pluginManager.registerEvents(gamePreperationState, plugin)
        plugin.server.pluginManager.registerEvents(ingameState, plugin)
        plugin.server.pluginManager.registerEvents(gameOverState, plugin)
    }

    fun nextState() {
        if (lobbyState.isActive) {
            lobbyState.endState()
            gamePreperationState.startState()
        } else if (gamePreperationState.isActive) {
            gamePreperationState.endState()
            ingameState.startState()
        } else if (ingameState.isActive) {
            ingameState.endState()
            gameOverState.startState()
        } else if (gameOverState.isActive) {
            gameOverState.endState()
            lobbyState.startState()
        }
    }

}