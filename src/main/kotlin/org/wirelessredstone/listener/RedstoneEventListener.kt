package org.wirelessredstone.listener

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Door
import org.bukkit.block.data.type.Gate
import org.bukkit.block.data.type.TrapDoor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.wirelessredstone.WirelessRedstone

class RedstoneEventListener(private val plugin: WirelessRedstone) : Listener {

    private val checkFaces = arrayOf(
        BlockFace.SELF, BlockFace.UP, BlockFace.DOWN,
        BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    )

    // 1. Tín hiệu Redstone thay đổi (Nguồn điện, Nút bấm, Cần gạt...)
    @EventHandler
    fun onRedstoneChange(event: BlockRedstoneEvent) {
        // 🟢 Bỏ qua nếu dòng điện không thực sự thay đổi (Chống Double Trigger)
        if (event.oldCurrent == event.newCurrent) return

        val isPowered = event.newCurrent > 0
        val wasPowered = event.oldCurrent > 0

        // Chỉ gửi tín hiệu khi TRẠNG THÁI BẬT/TẮT thay đổi thực sự
        if (isPowered == wasPowered) return

        val block = event.block

        for (face in checkFaces) {
            val targetBlock = block.getRelative(face)
            val senderLoc = targetBlock.location.block.location
            val signalId = plugin.signalManager.getSenderSignalId(senderLoc) ?: continue

            // 📢 BẮN LOG CHAT BÁO KHỐI PHÁT (Adventure API Paper 26.2)
            if (plugin.config.getBoolean("debug-messages", false)) {
                val statusComponent = if (isPowered) {
                    Component.text("[BẬT ON]", NamedTextColor.GREEN, TextDecoration.BOLD)
                } else {
                    Component.text("[TẮT OFF]", NamedTextColor.RED, TextDecoration.BOLD)
                }

                val msg = Component.text("[WirelessRedstone] ", NamedTextColor.YELLOW)
                    .append(Component.text("Khối Phát (ID: ", NamedTextColor.WHITE))
                    .append(Component.text(signalId, NamedTextColor.GOLD))
                    .append(Component.text(") -> Trạng thái: ", NamedTextColor.WHITE))
                    .append(statusComponent)
                    .append(Component.text(" (Power: ${event.newCurrent})", NamedTextColor.GRAY))

                Bukkit.broadcast(msg)
            }

            // 🟢 FIX: Cập nhật trạng thái theo VỊ TRÍ SENDER cụ thể
            plugin.signalManager.updateSenderState(senderLoc, isPowered)

            // Đã tìm thấy Khối Phát tương ứng thì break ngay
            break
        }
    }

    // 2. Mở/Đóng Cửa trực tiếp bằng tay (Door, Trapdoor, Fence Gate)
    @EventHandler
    fun onDoorInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val clickedBlock = event.clickedBlock ?: return

        val blockData = clickedBlock.blockData
        val isDoorType = blockData is Door || blockData is TrapDoor || blockData is Gate
        if (!isDoorType) return

        // Bỏ qua cửa sắt/bẫy sắt vì người chơi không thể mở bằng tay không
        if (clickedBlock.type == Material.IRON_DOOR || clickedBlock.type == Material.IRON_TRAPDOOR) {
            return
        }

        // Chuẩn hóa vị trí cửa: Nếu là cửa 2 block (Door) và đang click nửa trên, hạ xuống nửa dưới để tra cứu đúng ID
        val targetBlock = if (blockData is Door && blockData.half == Bisected.Half.TOP) {
            clickedBlock.getRelative(BlockFace.DOWN)
        } else {
            clickedBlock
        }

        val loc = targetBlock.location.block.location
        val signalId = plugin.signalManager.getSenderSignalId(loc) ?: return

        // Do event chạy TRƯỚC KHU CỬA ĐỔI TRẠNG THÁI, nên trạng thái mới sẽ là phủ định của 'isOpen' hiện tại
        val isOpenNow = when (blockData) {
            is Door -> !blockData.isOpen
            is TrapDoor -> !blockData.isOpen
            is Gate -> !blockData.isOpen
            else -> false
        }

        // Bắn Log Debug nếu bật trong config
        if (plugin.config.getBoolean("debug-messages", false)) {
            val statusComponent = if (isOpenNow) {
                Component.text("[MỞ / BẬT]", NamedTextColor.GREEN, TextDecoration.BOLD)
            } else {
                Component.text("[ĐÓNG / TẮT]", NamedTextColor.RED, TextDecoration.BOLD)
            }

            val msg = Component.text("[WirelessRedstone] ", NamedTextColor.YELLOW)
                .append(Component.text("Cửa Phát (ID: ", NamedTextColor.WHITE))
                .append(Component.text(signalId, NamedTextColor.GOLD))
                .append(Component.text(") -> Trạng thái: ", NamedTextColor.WHITE))
                .append(statusComponent)

            Bukkit.broadcast(msg)
        }

        // 🟢 FIX: Gọi updateSenderState thay vì setSignalState
        plugin.signalManager.updateSenderState(loc, isOpenNow)
    }
}