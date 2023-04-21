package net.novauniverse.game.turfwars.game.team

import net.novauniverse.game.turfwars.game.mapmodules.config.TeamConfig
import org.bukkit.entity.Player
import java.util.ArrayList
import java.util.UUID

class TurfWarsTeamData(team: TurfWarsTeam, teamConfig: TeamConfig) {
    val team: TurfWarsTeam
    val teamConfig: TeamConfig

    var members: List<UUID> = ArrayList<UUID>()

    var kills = 0

    init {
        this.team = team
        this.teamConfig = teamConfig
    }

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