package net.novauniverse.game.turfwars.game.data

import net.md_5.bungee.api.ChatColor
import net.novauniverse.game.turfwars.TurfWarsPlugin
import net.zeeraa.novacore.spigot.abstraction.VersionIndependentUtils
import net.zeeraa.novacore.spigot.utils.PlayerUtils
import org.bukkit.Bukkit
import java.util.UUID

class PlayerData(uuid: UUID) {
    val uuid: UUID = uuid
    var respawnTicks = -1
    var kills = 0

    fun tick() {
        if (respawnTicks >= 0) {
            if (PlayerUtils.existsAndIsOnline(uuid)) {
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