package net.novauniverse.game.turfwars.game.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TurfWarsTurfChangeEvent(val team1Turf: Int, val team2Turf: Int) : Event() {
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