package org.wirelessredstone.listener

import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.RedstoneWire
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPhysicsEvent
import org.wirelessredstone.WirelessRedstone

class ReceiverPowerListener(private val plugin: WirelessRedstone) : Listener {

    private val faces = arrayOf(
        BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,
        BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
    )

    @EventHandler
    fun onBlockPhysics(event: BlockPhysicsEvent) {
        val block = event.block

        // Nếu khối xung quanh là Bột Đá Đỏ
        if (block.type == Material.REDSTONE_WIRE) {
            for (face in faces) {
                val neighborLoc = block.getRelative(face).location
                // Khối Nhận kế bên đang BẬT -> Ép Bột Đá Đỏ nhận điện tối đa (15)
                if (plugin.signalManager.isReceiverActive(neighborLoc)) {
                    val wireData = block.blockData as RedstoneWire
                    wireData.power = 15
                    block.setBlockData(wireData, false)
                    break
                }
            }
        }
    }
}