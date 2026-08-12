package org.wirelessredstone.util

import org.bukkit.NamespacedKey
import org.wirelessredstone.WirelessRedstone

object Keys {
    lateinit var CHIP_TYPE: NamespacedKey
    lateinit var SIGNAL_ID: NamespacedKey
    lateinit var TRIGGER_CONDITION: NamespacedKey

    fun init(plugin: WirelessRedstone) {
        CHIP_TYPE = NamespacedKey(plugin, "chip_type")
        SIGNAL_ID = NamespacedKey(plugin, "signal_id")
        TRIGGER_CONDITION = NamespacedKey(plugin, "trigger_condition")
    }
}