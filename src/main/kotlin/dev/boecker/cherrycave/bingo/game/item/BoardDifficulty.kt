package dev.boecker.cherrycave.bingo.game.item

enum class BoardDifficulty(val itemDifficulties: List<Pair<Int, ItemDifficulty>>) {
    VERY_EASY(
        listOf(
            50 to ItemDifficulty.VERY_EASY,
            40 to ItemDifficulty.EASY,
            10 to ItemDifficulty.MEDIUM,
        )
    ),
    EASY(
        listOf(
            20 to ItemDifficulty.VERY_EASY,
            60 to ItemDifficulty.EASY,
            20 to ItemDifficulty.MEDIUM,
        )
    ),
    NORMAL(
        listOf(
            30 to ItemDifficulty.EASY,
            60 to ItemDifficulty.MEDIUM,
            10 to ItemDifficulty.HARD,
        )
    ),
    MEDIUM(
        listOf(
            20 to ItemDifficulty.EASY,
            50 to ItemDifficulty.MEDIUM,
            30 to ItemDifficulty.HARD,
        )
    ),
    HARD(
        listOf(
            40 to ItemDifficulty.MEDIUM,
            60 to ItemDifficulty.HARD,
        )
    ),
    VERY_HARD(
        listOf(
            50 to ItemDifficulty.HARD,
            50 to ItemDifficulty.VERY_HARD
        )
    ),
    IMPOSSIBLE(
        listOf(
            20 to ItemDifficulty.HARD,
            80 to ItemDifficulty.VERY_HARD
        )
    )
}