package me.ayaanali.hat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.InputStreamReader

class Hat : JavaPlugin(), CommandExecutor {

    private lateinit var messages: FileConfiguration

    override fun onEnable() {
        loadMessages()
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
                    sender.sendMessage(msg("no-permission"))
                    return true
                }
                loadMessages()
                sender.sendMessage(msg("reloaded"))
                return true
            }
            sender.sendMessage(Component.text("Usage: /hat reload"))
            return true
        }

        if (sender !is Player) {
            sender.sendMessage(msg("only-players"))
            return true
        }

        if (!sender.hasPermission("hat.wear")) {
            sender.sendMessage(msg("no-permission"))
            return true
        }

        val handItem = sender.inventory.itemInMainHand
        if (handItem.type == Material.AIR) {
            sender.sendMessage(msg("no-item"))
            return true
        }

        val currentHelmet = sender.inventory.helmet
        sender.inventory.setHelmet(handItem)
        sender.inventory.setItemInMainHand(currentHelmet)

        sender.playSound(sender.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f)
        return true
    }

    private fun loadMessages() {
        val file = File(dataFolder, "messages.yml")
        if (!file.exists()) {
            saveResource("messages.yml", false)
        }
        val config = YamlConfiguration.loadConfiguration(file)
        val defaultsStream = getResource("messages.yml")
        if (defaultsStream != null) {
            val defaults = YamlConfiguration.loadConfiguration(InputStreamReader(defaultsStream))
            config.setDefaults(defaults)
            config.options().copyDefaults(true)
            config.save(file)
        }
        messages = config
    }

    private fun msg(key: String): Component {
        val raw = messages.getString(key, "") ?: ""
        return LegacyComponentSerializer.legacyAmpersand().deserialize(raw)
    }
}
