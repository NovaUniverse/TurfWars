package net.novauniverse.game.turfwars.game.team

import net.novauniverse.game.turfwars.game.mapmodules.config.TeamConfig
import org.bukkit.entity.Player
import java.util.*

class TurfWarsTeamData(val team: TurfWarsTeam, val teamConfig: TeamConfig) {
    var members: List<UUID> = ArrayList<UUID>()

    var kills = 0

    fun addKill() {
        kills++
    }

    fun isMember(uuid: UUID): Boolean {
        return members.contains(uuid)
    }

    fun isMember(player: Player): Boolean {
        return isMember(player.uniqueId)
    }
}