package org.wirelessredstone.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.wirelessredstone.WirelessRedstone
import org.wirelessredstone.model.ChipType

class ChipInteractListener(private val plugin: WirelessRedstone) : Listener {

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val item = event.itemInHand
        val itemMeta = item.itemMeta ?: return

        // 🟢 อ่าน PDC thông qua chipDataManager
        val chipData = plugin.chipDataManager.readFromPdc(itemMeta.persistentDataContainer) ?: return

        val blockPlaced = event.blockPlaced
        val loc = blockPlaced.location.block.location
        val player = event.player

        // Dọn dẹp dữ liệu cũ ở tọa độ này nếu có
        plugin.signalManager.unregisterLocation(loc)

        val lang = plugin.languageManager

        when (chipData.type) {
            ChipType.RECEIVER -> {
                plugin.signalManager.registerReceiver(chipData.signalId, loc)

                // 🟢 Kiểm tra xem signalId này có Sender nào đang BẬT không.
                // Nếu có, kích hoạt điện cho Receiver mới đặt xuống luôn!
                val allSenders = plugin.signalManager.getAllSenders()
                val isAnySenderActive = allSenders.entries
                    .filter { it.value == chipData.signalId }
                    .any { (senderLoc, _) -> plugin.signalManager.getSenderSignalId(senderLoc) != null }

                // Cập nhật trạng thái hiển thị/điện cho Receiver mới
                plugin.signalManager.refreshReceivers(chipData.signalId, isAnySenderActive)

                player.sendMessage(lang.getMessage(player, "events.place-receiver", mapOf(
                    "signal_id" to chipData.signalId,
                    "x" to loc.blockX.toString(),
                    "y" to loc.blockY.toString(),
                    "z" to loc.blockZ.toString()
                )))
            }
            ChipType.SENDER -> {
                plugin.signalManager.registerSender(chipData.signalId, loc)
                // Đăng ký trạng thái ban đầu của Sender mới đặt (Default là tắt)
                plugin.signalManager.updateSenderState(loc, false)

                player.sendMessage(lang.getMessage(player, "events.place-sender", mapOf(
                    "signal_id" to chipData.signalId,
                    "x" to loc.blockX.toString(),
                    "y" to loc.blockY.toString(),
                    "z" to loc.blockZ.toString()
                )))
            }
        }

        // Lưu vị trí mới vào data.yml
        plugin.dataManager.saveData()
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        val loc = block.location.block.location
        val player = event.player

        // 🟢 Tra cứu xem đây là Khối Phát hay Khối Thu
        val senderSignalId = plugin.signalManager.getSenderSignalId(loc)
        val receiverSignalId = plugin.signalManager.getReceiverSignalId(loc)

        var droppedSignalId: String? = null
        var droppedChipType: ChipType? = null

        if (senderSignalId != null) {
            droppedSignalId = senderSignalId
            droppedChipType = ChipType.SENDER

            // 🟢 FIX: Báo cho SignalManager biết Sender tại loc vừa bị đập (Tắt điện)
            // SignalManager sẽ tự tính toán: Nếu còn Sender khác giữ nguồn thì Receiver không bị ngắt điện oan.
            plugin.signalManager.updateSenderState(loc, false)
        } else if (receiverSignalId != null) {
            droppedSignalId = receiverSignalId
            droppedChipType = ChipType.RECEIVER
        }

        // 🟢 NẾU ĐÚNG LÀ KHỐI CÓ GẮN CHIP (Bất kể Thu hay Phát)
        if (droppedSignalId != null && droppedChipType != null) {
            event.isDropItems = false

            // Trả lại khối block gốc
            val blockDrop = ItemStack(block.type, 1)
            loc.world?.dropItemNaturally(loc.clone().add(0.5, 0.5, 0.5), blockDrop)

            // Trả lại Chip Item tương ứng có PDC chứa signalId
            val chipItem = plugin.chipDataManager.createChipItem(droppedChipType, droppedSignalId)
            loc.world?.dropItemNaturally(loc.clone().add(0.5, 0.5, 0.5), chipItem)

            val typeText = if (droppedChipType == ChipType.SENDER) "Sender Chip" else "Receiver Chip"

            player.sendMessage(plugin.languageManager.getMessage(player, "events.break-chip", mapOf(
                "type" to typeText,
                "signal_id" to droppedSignalId
            )))
        }

        // Dọn dẹp tọa độ khỏi RAM
        plugin.signalManager.unregisterLocation(loc)

        // Cập nhật & Xóa vị trí khỏi data.yml
        plugin.dataManager.saveData()
    }
}