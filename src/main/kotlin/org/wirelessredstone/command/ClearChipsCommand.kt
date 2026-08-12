package org.wirelessredstone.command

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.wirelessredstone.WirelessRedstone

class ClearChipsCommand(private val plugin: WirelessRedstone) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val lang = plugin.languageManager

        if (!sender.hasPermission("wirelessredstone.admin")) {
            sender.sendMessage(lang.getMessage(sender, "no-permission"))
            return true
        }

        val count = plugin.signalManager.clearAllChips()
        plugin.dataManager.saveData()

        sender.sendMessage(lang.getMessage(sender, "commands.wrclear.success", mapOf("count" to count.toString())))
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        return emptyList()
    }
}