package org.wirelessredstone.recipe

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ShapedRecipe
import org.wirelessredstone.WirelessRedstone
import org.wirelessredstone.model.ChipType

object ChipRecipeManager {

    private fun getSenderKey(plugin: WirelessRedstone) = NamespacedKey(plugin, "sender_chip")
    private fun getReceiverKey(plugin: WirelessRedstone) = NamespacedKey(plugin, "receiver_chip")

    fun registerRecipes(plugin: WirelessRedstone) {
        // 🟢 1. Xóa recipe cũ trước khi đăng ký lại (Phòng trường hợp reload)
        unregisterRecipes(plugin)

        // 🟢 2. Đăng ký Sender Recipe
        val senderItem = plugin.chipDataManager.createChipItem(ChipType.SENDER, "Unassigned")
        val senderRecipe = ShapedRecipe(getSenderKey(plugin), senderItem).apply {
            shape("RR", "OO")
            setIngredient('R', Material.REDSTONE)
            setIngredient('O', Material.OBSERVER)
        }
        plugin.server.addRecipe(senderRecipe)

        // 🟢 3. Đăng ký Receiver Recipe
        val receiverRecipe = plugin.chipDataManager.createChipItem(ChipType.RECEIVER, "Unassigned")
        val receiverRecipeObj = ShapedRecipe(getReceiverKey(plugin), receiverRecipe).apply {
            shape("OO", "RR")
            setIngredient('O', Material.OBSERVER)
            setIngredient('R', Material.REDSTONE)
        }
        plugin.server.addRecipe(receiverRecipeObj)
    }

    // 🟢 HÀM XÓA RECIPE - Gọi cả khi register lẫn khi plugin disable
    fun unregisterRecipes(plugin: WirelessRedstone) {
        val senderKey = getSenderKey(plugin)
        val receiverKey = getReceiverKey(plugin)

        if (plugin.server.getRecipe(senderKey) != null) {
            plugin.server.removeRecipe(senderKey)
        }
        if (plugin.server.getRecipe(receiverKey) != null) {
            plugin.server.removeRecipe(receiverKey)
        }
    }
}