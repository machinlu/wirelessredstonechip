package org.wirelessredstone.gui

import org.bukkit.entity.Player

class ConditionSelectGui {

    fun openGui(player: Player) {
        // Code mở Anvil GUI hoặc Chest GUI chọn TriggerCondition (ON_STEP, ON_CLICK...)
        player.sendMessage("§e[WirelessRedstone] Mở menu chọn điều kiện kích hoạt...")
    }
}