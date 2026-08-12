package org.wirelessredstone.item

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.wirelessredstone.WirelessRedstone
import org.wirelessredstone.model.ChipType
import org.wirelessredstone.model.TriggerCondition
import org.wirelessredstone.util.Keys

class ChipItemFactory(private val plugin: WirelessRedstone) {

    fun createChip(
        type: ChipType,
        signalId: String = "Unassigned",
        condition: TriggerCondition = TriggerCondition.ON_CLICK
    ): ItemStack {
        val material = if (type == ChipType.SENDER) Material.REDSTONE_TORCH else Material.REPEATER
        val item = ItemStack(material)
        val meta = item.itemMeta ?: return item

        val lang = plugin.languageManager

        // 🟢 Lấy Name & Lore từ LanguageManager (khai báo trong config.yml)
        val nameKey = if (type == ChipType.SENDER) "sender-name" else "receiver-name"
        val displayName = lang.getItemComponent(nameKey, mapOf("signal_id" to signalId))

        val lore = mutableListOf<Component>(
            lang.getItemComponent("chip-description"),
            lang.getItemComponent("signal-id-lore", mapOf("signal_id" to signalId))
        )

        meta.displayName(displayName)
        meta.lore(lore)

        // 🟢 Ghi dữ liệu PDC (vẫn giữ nguyên logic cũ để lưu trữ)
        meta.persistentDataContainer.set(Keys.CHIP_TYPE, PersistentDataType.STRING, type.name)
        meta.persistentDataContainer.set(Keys.SIGNAL_ID, PersistentDataType.STRING, signalId)
        meta.persistentDataContainer.set(Keys.TRIGGER_CONDITION, PersistentDataType.STRING, condition.name)

        item.itemMeta = meta
        return item
    }
}