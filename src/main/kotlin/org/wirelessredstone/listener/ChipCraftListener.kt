package org.wirelessredstone.listener

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.ItemStack
import org.wirelessredstone.WirelessRedstone
import org.wirelessredstone.model.ChipType

class ChipCraftListener(private val plugin: WirelessRedstone) : Listener {

    @EventHandler
    fun onPrepareCraft(event: PrepareItemCraftEvent) {
        val matrix = event.inventory.matrix
        if (matrix.isEmpty()) return

        var chipItem: ItemStack? = null
        var targetBlockItem: ItemStack? = null
        var itemCount = 0

        for (item in matrix) {
            if (item == null || item.type == Material.AIR) continue
            itemCount++

            // 🟢 FIX: Gọi qua instance plugin.chipDataManager
            if (plugin.chipDataManager.isChipItem(item)) {
                chipItem = item
            } else if (item.type.isBlock) {
                targetBlockItem = item
            }
        }

        // Bắt buộc chính xác 1 Chip + 1 Block bất kỳ trong bàn chế tạo
        if (itemCount == 2 && chipItem != null && targetBlockItem != null) {
            val itemMeta = chipItem.itemMeta ?: return

            // 🟢 FIX: Gọi qua instance plugin.chipDataManager
            val chipData = plugin.chipDataManager.readFromPdc(itemMeta.persistentDataContainer) ?: return

            val result = targetBlockItem.clone()
            result.amount = 1
            val resultMeta = result.itemMeta ?: return

            // Ghi PDC từ Chip sang Block kết quả
            plugin.chipDataManager.writeToPdc(resultMeta.persistentDataContainer, chipData)

            // 🟢 FIX: Lấy Lore ghép chip từ config.yml thông qua LanguageManager
            val loreKey = if (chipData.type == ChipType.SENDER) "attached-sender-lore" else "attached-receiver-lore"
            val attachedLore = plugin.languageManager.getItemComponent(loreKey, mapOf("signal_id" to chipData.signalId))

            val lore = resultMeta.lore() ?: mutableListOf()
            lore.add(attachedLore)
            resultMeta.lore(lore)

            result.itemMeta = resultMeta
            event.inventory.result = result
        }
    }
}