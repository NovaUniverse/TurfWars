package net.novauniverse.game.turfwars.game.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TurfWarsTurfChangeEvent(team1Turf: Int, team2Turf: Int) : Event() {
    val team1Turf : Int = team1Turf
        get
    val team2Turf: Int = team2Turf
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