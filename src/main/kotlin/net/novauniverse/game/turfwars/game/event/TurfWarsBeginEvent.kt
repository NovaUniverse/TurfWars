package net.novauniverse.game.turfwars.game.event

import net.novauniverse.game.turfwars.game.team.TurfWarsTeamData
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TurfWarsBeginEvent(val teamTurfSize: Int, val team1: TurfWarsTeamData, val team2: TurfWarsTeamData) : Event() {
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