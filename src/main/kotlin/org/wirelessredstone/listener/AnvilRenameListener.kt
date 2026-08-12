package org.wirelessredstone.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.wirelessredstone.WirelessRedstone
import org.wirelessredstone.model.ChipType

class AnvilRenameListener(private val plugin: WirelessRedstone) : Listener {

    @EventHandler
    fun onAnvilPrepare(event: PrepareAnvilEvent) {
        val firstSlot = event.inventory.getItem(0) ?: return

        // 🟢 FIX: Gọi qua instance chipDataManager
        if (!plugin.chipDataManager.isChipItem(firstSlot)) return

        // Dùng event.view.renameText chuẩn Paper
        val renameText = event.view.renameText?.trim()
        if (renameText.isNullOrEmpty()) return

        val oldMeta = firstSlot.itemMeta ?: return
        val chipData = plugin.chipDataManager.readFromPdc(oldMeta.persistentDataContainer) ?: return

        // Tạo ChipData mới với Signal ID đã đổi
        val updatedChipData = chipData.copy(signalId = renameText)

        val resultItem = firstSlot.clone()
        val newMeta = resultItem.itemMeta ?: return

        // 🟢 FIX: Ghi dữ liệu PDC mới
        plugin.chipDataManager.writeToPdc(newMeta.persistentDataContainer, updatedChipData)

        // 🟢 FIX: Cập nhật DisplayName & Lore mới rút từ config.yml qua LanguageManager
        val nameKey = if (updatedChipData.type == ChipType.SENDER) "sender-name" else "receiver-name"
        val displayName = plugin.languageManager.getItemComponent(nameKey, mapOf("signal_id" to renameText))

        val lore = listOf(
            plugin.languageManager.getItemComponent("chip-description"),
            plugin.languageManager.getItemComponent("signal-id-lore", mapOf("signal_id" to renameText))
        )

        newMeta.displayName(displayName)
        newMeta.lore(lore)

        resultItem.itemMeta = newMeta
        event.result = resultItem
    }
}