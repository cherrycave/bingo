package dev.boecker.cherrycave.bingo.game.state.prepare

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player

fun Player.setBossbar() {
    this.activeBossBars().forEach { bossBar ->
        this.hideBossBar(bossBar)
    }

    this.showBossBar(
        BossBar.bossBar(
            Component.text("\uE000", TextColor.fromHexString("#FE01FE")), 0f, BossBar.Color.RED,
            BossBar.Overlay.PROGRESS
        )
    )
}