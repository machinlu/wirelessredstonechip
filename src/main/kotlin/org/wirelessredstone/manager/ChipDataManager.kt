package org.wirelessredstone.manager

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.wirelessredstone.WirelessRedstone
import org.wirelessredstone.model.ChipData
import org.wirelessredstone.model.ChipType
import org.wirelessredstone.model.TriggerCondition
import org.wirelessredstone.util.Keys

class ChipDataManager(private val plugin: WirelessRedstone) {

    fun writeToPdc(pdc: PersistentDataContainer, data: ChipData) {
        pdc.set(Keys.CHIP_TYPE, PersistentDataType.STRING, data.type.name)
        pdc.set(Keys.SIGNAL_ID, PersistentDataType.STRING, data.signalId)
        pdc.set(Keys.TRIGGER_CONDITION, PersistentDataType.STRING, data.condition.name)
    }

    fun readFromPdc(pdc: PersistentDataContainer): ChipData? {
        val typeStr = pdc.get(Keys.CHIP_TYPE, PersistentDataType.STRING) ?: return null
        val signalId = pdc.get(Keys.SIGNAL_ID, PersistentDataType.STRING) ?: return null
        val conditionStr = pdc.get(Keys.TRIGGER_CONDITION, PersistentDataType.STRING) ?: TriggerCondition.ON_CLICK.name

        val type = runCatching { ChipType.valueOf(typeStr) }.getOrNull() ?: return null
        val condition = runCatching { TriggerCondition.valueOf(conditionStr) }.getOrNull() ?: TriggerCondition.ON_CLICK

        return ChipData(type, signalId, condition)
    }

    fun isChipItem(item: ItemStack?): Boolean {
        if (item == null || !item.hasItemMeta()) return false
        val meta = item.itemMeta ?: return false
        return meta.persistentDataContainer.has(Keys.CHIP_TYPE, PersistentDataType.STRING)
    }

    fun createChipItem(
        type: ChipType,
        signalId: String,
        condition: TriggerCondition = TriggerCondition.ON_CLICK
    ): ItemStack {
        val item = ItemStack(Material.PAPER)

        // 🟢 1. Gán Custom Model Data String chuẩn Paper Data Component ("3001" / "3002")
        val modelString = if (type == ChipType.SENDER) "3001" else "3002"
        val customModelData = CustomModelData.customModelData()
            .addString(modelString)
            .build()

        item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData)

        // 🟢 2. Gán Display Name, Lore & PDC bằng editMeta
        item.editMeta { meta ->
            val chipData = ChipData(type, signalId, condition)
            writeToPdc(meta.persistentDataContainer, chipData)

            val lang = plugin.languageManager

            val nameKey = if (type == ChipType.SENDER) "sender-name" else "receiver-name"
            val displayName = lang.getItemComponent(nameKey, mapOf("signal_id" to signalId))

            val lore = listOf(
                lang.getItemComponent("chip-description"),
                lang.getItemComponent("signal-id-lore", mapOf("signal_id" to signalId))
            )

            meta.displayName(displayName)
            meta.lore(lore)
        }

        return item
    }
}