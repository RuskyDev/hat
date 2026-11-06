package me.ayaanali.hat

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class Hat : JavaPlugin(), CommandExecutor {

    private lateinit var configManager: ConfigManager

    override fun onEnable() {
        configManager = ConfigManager(this)
        configManager.load()
        getCommand("wear")?.setExecutor(this)
        getCommand("hat")?.setExecutor(this)
        UpdateManager(this).checkForUpdates()
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (command.name.equals("hat", ignoreCase = true)) {
            if (args.size == 1 && args[0].equals("reload", ignoreCase = true)) {
                if (!sender.hasPermission("hat.reload")) {
                    sender.sendMessage(configManager.msg("no-permission"))
                    return true
                }
                configManager.reload()
                sender.sendMessage(configManager.msg("reloaded"))
                return true
            }
            sender.sendMessage(Component.text("Usage: /hat reload"))
            return true
        }

        if (sender !is Player) {
            sender.sendMessage(configManager.msg("only-players"))
            return true
        }

        if (!sender.hasPermission("hat.wear")) {
            sender.sendMessage(configManager.msg("no-permission"))
            return true
        }

        val handItem = sender.inventory.itemInMainHand
        if (handItem.type == Material.AIR) {
            sender.sendMessage(configManager.msg("no-item"))
            return true
        }

        val currentHelmet = sender.inventory.helmet
        sender.inventory.setHelmet(handItem)
        sender.inventory.setItemInMainHand(currentHelmet)

        sender.playSound(sender.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f)
        return true
    }
}
