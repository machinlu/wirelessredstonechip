package org.wirelessredstone.manager

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.wirelessredstone.WirelessRedstone

class LanguageManager(private val plugin: WirelessRedstone) {

    private val miniMessage = MiniMessage.miniMessage()

    fun getMessage(
        sender: CommandSender,
        path: String,
        placeholders: Map<String, String> = emptyMap()
    ): Component {
        val defaultLang = plugin.config.getString("default-language") ?: "en_us"
        val userLang = if (sender is Player) sender.locale().toString().lowercase() else defaultLang
        val langKey = if (plugin.config.contains("messages.$userLang")) userLang else defaultLang

        val prefix = plugin.config.getString("messages.$langKey.prefix") ?: ""
        var rawMessage = plugin.config.getString("messages.$langKey.$path")
            ?: plugin.config.getString("messages.$defaultLang.$path")
            ?: "<red>Missing message: $path</red>"

        placeholders.forEach { (key, value) ->
            rawMessage = rawMessage.replace("<$key>", value)
        }

        return miniMessage.deserialize(prefix + rawMessage)
    }

    // 🟢 Lấy Component dành riêng cho Item (dùng default-language của server)
    fun getItemComponent(path: String, placeholders: Map<String, String> = emptyMap()): Component {
        val defaultLang = plugin.config.getString("default-language") ?: "en_us"
        var rawMessage = plugin.config.getString("messages.$defaultLang.items.$path")
            ?: "<red>Missing item text: $path</red>"

        placeholders.forEach { (key, value) ->
            rawMessage = rawMessage.replace("<$key>", value)
        }

        return miniMessage.deserialize(rawMessage)
    }
}