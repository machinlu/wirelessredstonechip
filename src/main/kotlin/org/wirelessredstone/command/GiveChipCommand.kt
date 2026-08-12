package org.wirelessredstone.command

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.wirelessredstone.WirelessRedstone
import org.wirelessredstone.model.ChipType
import org.wirelessredstone.model.TriggerCondition

class GiveChipCommand(private val plugin: WirelessRedstone) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val lang = plugin.languageManager

        if (sender !is Player) {
            sender.sendMessage(lang.getMessage(sender, "only-player"))
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage(lang.getMessage(sender, "commands.givechip.usage"))
            return true
        }

        val typeStr = args[0].lowercase()
        val signalId = if (args.size > 1) args[1] else "demo_a"

        val chipType = when (typeStr) {
            "sender", "phat" -> ChipType.SENDER
            "receiver", "thu" -> ChipType.RECEIVER
            else -> {
                sender.sendMessage(lang.getMessage(sender, "commands.givechip.invalid-type"))
                return true
            }
        }

        // 🟢 FIX: Dùng chipDataManager đã được inject từ main class
        val chipItem = plugin.chipDataManager.createChipItem(chipType, signalId, TriggerCondition.ON_CLICK)

        // Thêm item vào túi đồ (và rớt ra đất nếu túi đầy)
        val leftover = sender.inventory.addItem(chipItem)
        if (leftover.isNotEmpty()) {
            leftover.values.forEach { sender.world.dropItem(sender.location, it) }
        }

        // Format lại tên loại chip để đưa vào placeholder cho đẹp
        val formattedType = if (chipType == ChipType.SENDER) "Sender" else "Receiver"

        sender.sendMessage(lang.getMessage(sender, "commands.givechip.success", mapOf(
            "chip_type" to formattedType,
            "signal_id" to signalId
        )))

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (args.size == 1) return listOf("sender", "receiver").filter { it.startsWith(args[0], ignoreCase = true) }
        if (args.size == 2) return listOf("demo_a", "signal_1", "main_gate").filter { it.startsWith(args[1], ignoreCase = true) }
        return emptyList()
    }
}