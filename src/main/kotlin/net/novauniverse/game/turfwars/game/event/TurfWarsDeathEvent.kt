package net.novauniverse.game.turfwars.game.event

import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TurfWarsKillEvent(player: Player, killer: Player) : Event() {
    val player = player
        get
    val killer = killer
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