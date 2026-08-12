package org.wirelessredstone

import org.bukkit.plugin.java.JavaPlugin
import org.wirelessredstone.command.ClearChipsCommand
import org.wirelessredstone.command.GiveChipCommand
import org.wirelessredstone.listener.*
import org.wirelessredstone.manager.ChipDataManager
import org.wirelessredstone.manager.DataManager
import org.wirelessredstone.manager.LanguageManager
import org.wirelessredstone.manager.SignalManager
import org.wirelessredstone.recipe.ChipRecipeManager
import org.wirelessredstone.util.Keys

class WirelessRedstone : JavaPlugin() {

    lateinit var signalManager: SignalManager
        private set

    lateinit var dataManager: DataManager
        private set

    lateinit var languageManager: LanguageManager
        private set

    lateinit var chipDataManager: ChipDataManager
        private set

    override fun onEnable() {
        saveDefaultConfig()

        // 1. Khởi tạo Keys & Managers
        Keys.init(this)
        languageManager = LanguageManager(this)
        chipDataManager = ChipDataManager(this)
        signalManager = SignalManager()
        dataManager = DataManager(this)

        // 2. Load Dữ liệu
        dataManager.loadData()

        // 3. Đăng ký Recipe
        ChipRecipeManager.registerRecipes(this)

        // 4. Đăng ký Listeners
        val pm = server.pluginManager
        pm.registerEvents(ResourcePackListener(), this)
        pm.registerEvents(ChipCraftListener(this), this)
        pm.registerEvents(ChipInteractListener(this), this)
        pm.registerEvents(RedstoneEventListener(this), this)
        pm.registerEvents(ReceiverPowerListener(this), this)
        pm.registerEvents(AnvilRenameListener(this), this)

        // 5. Đăng ký Commands
        registerCommandDirectly("givechip", GiveChipCommand(this))
        registerCommandDirectly("wrclear", ClearChipsCommand(this))

        logger.info("WirelessRedstone initialized successfully!")
    }

    private fun registerCommandDirectly(name: String, executor: Any) {
        val command = object : org.bukkit.command.Command(name) {
            override fun execute(sender: org.bukkit.command.CommandSender, commandLabel: String, args: Array<out String>): Boolean {
                if (executor is org.bukkit.command.CommandExecutor) {
                    return executor.onCommand(sender, this, commandLabel, args)
                }
                return false
            }

            override fun tabComplete(sender: org.bukkit.command.CommandSender, alias: String, args: Array<out String>): List<String> {
                if (executor is org.bukkit.command.TabCompleter) {
                    return executor.onTabComplete(sender, this, alias, args) ?: emptyList()
                }
                return super.tabComplete(sender, alias, args)
            }
        }
        server.commandMap.register(description.name, command)
    }

    override fun onDisable() {
        // 🟢 1. Lưu Dữ liệu vị trí các chip
        if (::dataManager.isInitialized) {
            dataManager.saveData()
        }

        // 🟢 2. FIX LỖI: Hủy đăng ký tất cả Recipe khi plugin ngắt/reload
        ChipRecipeManager.unregisterRecipes(this)
        logger.info("WirelessRedstone disabled!")
    }
}