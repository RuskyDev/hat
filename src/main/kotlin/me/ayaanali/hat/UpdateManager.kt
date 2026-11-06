package me.ayaanali.hat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.net.URI

class UpdateManager(private val plugin: Hat) : Listener {

    @Volatile
    private var updateAvailable: Boolean = false

    @Volatile
    private var latestVersion: String? = null

    @Volatile
    private var releaseUrl: String? = null

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    fun checkForUpdates() {
        plugin.logger.info("Checking for updates...")
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            try {
                val [remote, url] = fetchLatest()
                val current = plugin.pluginMeta.version
                if (remote != null && isNewer(remote, current)) {
                    updateAvailable = true
                    latestVersion = remote
                    releaseUrl = url
                    plugin.logger.warning("A new version of Hat is available: $remote (current: $current)")
                    plugin.logger.warning("Download: $url")
                } else {
                    plugin.logger.info("Hat is already up to date ($current).")
                }
            } catch (e: Exception) {
                plugin.logger.warning("Could not check for updates: ${e.message}")
            }
        })
    }

    private fun fetchLatest(): Pair<String?, String?> {
        return try {
            val json = readUrl("https://api.github.com/repos/RuskyDev/hat/releases/latest")
            val tag = Regex("\"tag_name\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1)
            val url = Regex("\"html_url\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1)
            if (tag != null) {
                tag to (url ?: "https://github.com/RuskyDev/hat/releases/tag/$tag")
            } else {
                fetchLatestTag()
            }
        } catch (e: Exception) {
            fetchLatestTag()
        }
    }

    private fun fetchLatestTag(): Pair<String?, String?> {
        val json = readUrl("https://api.github.com/repos/RuskyDev/hat/tags")
        val tag = Regex("\"name\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1)
        return if (tag != null) {
            tag to "https://github.com/RuskyDev/hat/releases/tag/$tag"
        } else {
            null to null
        }
    }

    private fun readUrl(url: String): String {
        return URI.create(url).toURL().openStream().bufferedReader().use { it.readText() }
    }

    private fun isNewer(remote: String, current: String): Boolean {
        val r = parseVersion(remote)
        val c = parseVersion(current)
        for (i in 0 until maxOf(r.size, c.size)) {
            val rv = r.getOrElse(i) { 0 }
            val cv = c.getOrElse(i) { 0 }
            if (rv != cv) return rv > cv
        }
        return false
    }

    private fun parseVersion(version: String): List<Int> {
        return version.trim().removePrefix("v").removePrefix("V")
            .split(".")
            .map { it.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 0 }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (!player.isOp || !updateAvailable) return
        val version = latestVersion ?: return
        val url = releaseUrl ?: return
        player.sendMessage(Component.text("A new version of Hat is available: $version").color(NamedTextColor.YELLOW))
        player.sendMessage(Component.text(url).color(NamedTextColor.AQUA))
    }
}
