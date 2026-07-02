package dev.boecker.cherrycave.bingo.game.state.ingame

import kotlin.time.Duration.Companion.seconds

fun Long.formatBingoTime(): String {
    return this.seconds.toComponents { hours, minutes, seconds, _ ->
        "${if (hours > 0) "${hours}h " else ""}${if (minutes > 0) "${minutes}m " else ""}${if (seconds > 0) "${seconds}s" else ""}"
    }
}