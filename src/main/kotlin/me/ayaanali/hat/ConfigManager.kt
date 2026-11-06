package me.ayaanali.hat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.InputStreamReader

class ConfigManager(private val plugin: JavaPlugin) {

    private lateinit var messages: FileConfiguration

    fun load() {
        val file = File(plugin.dataFolder, "messages.yml")
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false)
        }
        val config = YamlConfiguration.loadConfiguration(file)
        val defaultsStream = plugin.getResource("messages.yml")
        if (defaultsStream != null) {
            val defaults = YamlConfiguration.loadConfiguration(InputStreamReader(defaultsStream))
            config.setDefaults(defaults)
            config.options().copyDefaults(true)
            config.save(file)
        }
        messages = config
    }

    fun reload() {
        load()
    }

    fun msg(key: String): Component {
        val raw = messages.getString(key, "") ?: ""
        return LegacyComponentSerializer.legacyAmpersand().deserialize(raw)
    }
}
