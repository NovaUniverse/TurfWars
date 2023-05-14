package net.novauniverse.game.turfwars.game.data

import net.md_5.bungee.api.ChatColor
import net.novauniverse.game.turfwars.TurfWarsPlugin
import net.zeeraa.novacore.spigot.abstraction.VersionIndependentUtils
import org.bukkit.Bukkit
import java.util.*

class PlayerData(val uuid: UUID) {
    var respawnTicks = -1
    var kills = 0

    fun tick() {
        if (respawnTicks >= 0) {
            if (Bukkit.getPlayer(uuid) != null) {
                respawnTicks--
                val player = Bukkit.getPlayer(uuid)
                VersionIndependentUtils.get().sendTitle(player, "${ChatColor.RED}Respawning in${ChatColor.AQUA} ${(respawnTicks / 20) + 1}", "", 0, 5, 0)
                if (respawnTicks == 0) {
                    TurfWarsPlugin.getInstance().game!!.tpPlayer(player)
                }
            }
        }
    }

    fun incrementKills() {
        kills++
    }
}