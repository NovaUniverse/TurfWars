package net.novauniverse.game.turfwars.game.event

import net.novauniverse.game.turfwars.game.mapmodules.config.TeamConfig
import net.novauniverse.game.turfwars.game.team.TurfWarsTeamData
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TurfWarsBeginEvent(teamTurfSize: Int, team1Config: TeamConfig, team2Config: TeamConfig) : Event() {
    val teamTurfSize : Int = teamTurfSize
        get
    val team1Config : TeamConfig = team1Config
        get
    val team2Config: TeamConfig = team2Config
        get

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}