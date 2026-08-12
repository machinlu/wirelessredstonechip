package org.wirelessredstone.listener

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class ResourcePackListener : Listener {

    private val packUrl = "https://github.com/machinlu/wirelessredstonechip/releases/download/v1.0.0/wirelessredstonechips.zip"

    // 🟢 Chuyển SHA-1 Hash về dạng chữ viết thường (lowercase)
    private val packHash = "792D4406E38438E319DC54701538AEF2E20005B8"

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val promptText = Component.text("Vui lòng tải Resource Pack để hiển thị mô hình Chip 3D chuẩn nhất!")
            .color(NamedTextColor.YELLOW)

        player.setResourcePack(
            packUrl,
            packHash,
            true, // force = true
            promptText
        )
    }
}