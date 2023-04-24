package net.novauniverse.game.turfwars.game.event

import net.novauniverse.game.turfwars.game.team.TurfWarsTeamData
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TurfWarsBeginEvent(teamTurfSize: Int, team1: TurfWarsTeamData, team2: TurfWarsTeamData) : Event() {
    val teamTurfSize : Int = teamTurfSize
        get
    val team1 : TurfWarsTeamData = team1
        get
    val team2: TurfWarsTeamData = team2
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