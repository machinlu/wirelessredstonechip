package org.wirelessredstone.manager

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.wirelessredstone.WirelessRedstone
import java.io.File
import java.io.IOException

class DataManager(private val plugin: WirelessRedstone) {

    private val file = File(plugin.dataFolder, "data.yml")
    private var config = YamlConfiguration.loadConfiguration(file)

    init {
        // 🟢 FIX: Kiểm tra thư mục plugin và tạo file data.yml nếu chưa tồn tại
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                plugin.logger.severe("Không thể tạo file data.yml: ${e.message}")
            }
        }
    }

    // 🟢 1. Tải dữ liệu từ data.yml vào SignalManager khi bật Server
    fun loadData() {
        if (!file.exists()) return
        config = YamlConfiguration.loadConfiguration(file)

        // Load Senders
        val sendersSection = config.getConfigurationSection("senders")
        sendersSection?.getKeys(false)?.forEach { signalId ->
            val locList = config.getStringList("senders.$signalId")
            for (locStr in locList) {
                val loc = parseLocation(locStr) ?: continue
                plugin.signalManager.registerSender(signalId, loc)
            }
        }

        // Load Receivers
        val receiversSection = config.getConfigurationSection("receivers")
        receiversSection?.getKeys(false)?.forEach { signalId ->
            val locList = config.getStringList("receivers.$signalId")
            for (locStr in locList) {
                val loc = parseLocation(locStr) ?: continue
                plugin.signalManager.registerReceiver(signalId, loc)
            }
        }

        plugin.logger.info("[WirelessRedstone] Đã tải lại toàn bộ vị trí Chip từ data.yml!")
    }

    // 🟢 2. Lưu dữ liệu từ SignalManager vào data.yml khi tắt Server
    fun saveData() {
        config.set("senders", null)
        config.set("receivers", null)

        // 🟢 Dùng getAllSenders() công khai từ SignalManager
        val sendersGrouped = mutableMapOf<String, MutableList<String>>()
        for ((location, signalId) in plugin.signalManager.getAllSenders()) {
            val list = sendersGrouped.getOrPut(signalId) { mutableListOf() }
            list.add(serializeLocation(location))
        }

        // Ghi Senders vào YML
        for ((signalId, locStrings) in sendersGrouped) {
            config.set("senders.$signalId", locStrings)
        }

        // 🟢 Dùng getAllReceivers() công khai từ SignalManager
        for ((signalId, locations) in plugin.signalManager.getAllReceivers()) {
            val locStrings = locations.map { serializeLocation(it) }
            config.set("receivers.$signalId", locStrings)
        }

        try {
            config.save(file)
            plugin.logger.info("[WirelessRedstone] Đã lưu dữ liệu Chip vào data.yml thành công!")
        } catch (e: IOException) {
            plugin.logger.severe("Không thể lưu file data.yml: ${e.message}")
        }
    }

    private fun serializeLocation(loc: Location): String {
        return "${loc.world?.name},${loc.blockX},${loc.blockY},${loc.blockZ}"
    }

    private fun parseLocation(str: String): Location? {
        val parts = str.split(",")
        if (parts.size != 4) return null
        val world = Bukkit.getWorld(parts[0]) ?: return null
        val x = parts[1].toDouble()
        val y = parts[2].toDouble()
        val z = parts[3].toDouble()
        return Location(world, x, y, z)
    }
}