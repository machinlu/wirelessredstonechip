package org.wirelessredstone.manager

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Dispenser
import org.bukkit.block.Dropper
import org.bukkit.block.data.Lightable
import org.bukkit.block.data.Openable
import org.bukkit.block.data.type.RedstoneWire
import org.bukkit.entity.TNTPrimed

class SignalManager {

    private val receivers = mutableMapOf<String, MutableList<Location>>()
    private val senders = mutableMapOf<Location, String>()
    // Lưu các Sender ĐANG CÓ ĐIỆN (Active)
    private val activeSenders = mutableSetOf<Location>()
    private val activeReceivers = mutableSetOf<Location>()

    // 🔴 Helper: Chuẩn hóa Location về tâm khối để tránh lỗi Pitch/Yaw/Double
    private fun Location.toBlockLocation(): Location {
        return Location(this.world, this.blockX.toDouble(), this.blockY.toDouble(), this.blockZ.toDouble())
    }

    fun registerReceiver(signalId: String, location: Location) {
        val blockLoc = location.toBlockLocation()
        val list = receivers.computeIfAbsent(signalId) { mutableListOf() }
        if (!list.contains(blockLoc)) {
            list.add(blockLoc)
        }
    }

    fun registerSender(signalId: String, location: Location) {
        senders[location.toBlockLocation()] = signalId
    }

    fun getSenderSignalId(location: Location): String? {
        return senders[location.toBlockLocation()]
    }

    fun isReceiverActive(location: Location): Boolean {
        return activeReceivers.contains(location.toBlockLocation())
    }

    fun unregisterLocation(location: Location) {
        val blockLoc = location.toBlockLocation()
        receivers.values.forEach { list ->
            list.removeIf { it == blockLoc }
        }
        senders.remove(blockLoc)
        activeSenders.remove(blockLoc)
        activeReceivers.remove(blockLoc)
    }

    /**
     * Cập nhật trạng thái của một Sender cụ thể
     */
    fun updateSenderState(senderLoc: Location, isPowered: Boolean) {
        val blockLoc = senderLoc.toBlockLocation()
        val signalId = senders[blockLoc] ?: return

        if (isPowered) {
            activeSenders.add(blockLoc)
        } else {
            activeSenders.remove(blockLoc)
        }

        // Tín hiệu chung của signalId chỉ BẬT nếu có ÍT NHẤT 1 Sender đang bật
        val isSignalActive = senders.entries
            .filter { it.value == signalId }
            .any { activeSenders.contains(it.key) }

        refreshReceivers(signalId, isSignalActive)
    }

    /**
     * Cập nhật toàn bộ Receiver thuộc signalId
     */
    fun refreshReceivers(signalId: String, active: Boolean) {
        val targetLocations = receivers[signalId] ?: return

        for (loc in targetLocations) {
            if (active) activeReceivers.add(loc) else activeReceivers.remove(loc)

            val targetBlock = loc.block
            val state = targetBlock.state

            when {
                // 1. TNT
                targetBlock.type == Material.TNT -> {
                    if (active) triggerTNT(targetBlock)
                }
                // 2. Dispenser
                state is Dispenser -> {
                    if (active) state.dispense()
                }
                // 3. Dropper
                state is Dropper -> {
                    if (active) state.drop()
                }
                // 4. Khối khác (Đất, Đèn, Cửa Receiver,...)
                else -> {
                    applyRedstoneState(targetBlock, active)

                    // Cập nhật các khối lân cận (Ví dụ: bột Redstone nối vào khối đất)
                    for (face in BlockFace.entries) {
                        val neighbor = targetBlock.getRelative(face)

                        // ⛔ CỰC KỲ QUAN TRỌNG: Nếu khối lân cận là SENDER -> BỎ QUA NGAY!
                        if (senders.containsKey(neighbor.location.toBlockLocation())) continue

                        if (neighbor.state !is Dispenser && neighbor.state !is Dropper) {
                            applyRedstoneState(neighbor, active)
                        }
                    }
                }
            }
        }
    }

    private fun applyRedstoneState(block: Block, active: Boolean) {
        // ⛔ KHÔNG NGHỆO CẠ: Nếu khối này chính là 1 SENDER -> Không bao giờ đổi trạng thái nó từ Receiver!
        if (senders.containsKey(block.location.toBlockLocation())) return

        val data = block.blockData
        val state = block.state

        when {
            data is RedstoneWire -> {
                data.power = if (active) 15 else 0
                block.setBlockData(data, true)
            }
            data is Openable -> {
                data.isOpen = active
                block.setBlockData(data, true)
            }
            data is Lightable -> {
                data.isLit = active
                block.setBlockData(data, true)
            }
            block.type == Material.TNT -> {
                if (active) triggerTNT(block)
            }
            state is Dispenser -> {
                if (active) state.dispense()
            }
            state is Dropper -> {
                if (active) state.drop()
            }
            else -> {
                block.state.update(true, true)
            }
        }
    }

    private fun triggerTNT(block: Block) {
        val loc = block.location.add(0.5, 0.0, 0.5)
        block.type = Material.AIR
        val tnt = block.world.spawn(loc, TNTPrimed::class.java)
        tnt.fuseTicks = 80
    }

    fun clearAllChips(): Int {
        val totalSenders = senders.size
        val totalReceivers = receivers.values.sumOf { it.size }
        val total = totalSenders + totalReceivers

        senders.clear()
        receivers.clear()
        activeSenders.clear()
        activeReceivers.clear()

        return total
    }

    fun getReceiverSignalId(location: Location): String? {
        val blockLoc = location.toBlockLocation()
        return receivers.entries.firstOrNull { entry ->
            entry.value.contains(blockLoc)
        }?.key
    }

    fun getAllSenders(): Map<Location, String> {
        return senders
    }

    fun getAllReceivers(): Map<String, List<Location>> {
        return receivers
    }
}