package dev.boecker.cherrycave.bingo.game.item

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.tag.Tag
import io.papermc.paper.registry.tag.TagKey
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemType

enum class ItemDifficulty(val difficultyName: String, val difficultyColor: TextColor, val itemTag: Tag<ItemType>) {
    VERY_EASY(
        "Very Easy", NamedTextColor.GREEN, RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).getTag(
            TagKey.create(RegistryKey.ITEM, Key.key("bingo:veryeasy"))
        )
    ),
    EASY(
        "Easy", NamedTextColor.DARK_GREEN, RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).getTag(
            TagKey.create(RegistryKey.ITEM, Key.key("bingo:easy"))
        )
    ),
    MEDIUM(
        "Medium", NamedTextColor.YELLOW, RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).getTag(
            TagKey.create(RegistryKey.ITEM, Key.key("bingo:medium"))
        )
    ),
    HARD(
        "Hard", NamedTextColor.RED, RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).getTag(
            TagKey.create(RegistryKey.ITEM, Key.key("bingo:hard"))
        )
    ),
    VERY_HARD(
        "Hard", NamedTextColor.DARK_RED, RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).getTag(
            TagKey.create(RegistryKey.ITEM, Key.key("bingo:veryhard"))
        )
    )
}

fun Material.getItemDifficulty(): ItemDifficulty? {
    return ItemDifficulty.entries.reversed().find {
        it.itemTag.contains(this.key())
    }
}