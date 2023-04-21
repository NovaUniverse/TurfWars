package net.novauniverse.game.turfwars.game.team.teampopulator

import net.zeeraa.novacore.commons.utils.Pair
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class DefaultTeamPopulator : TurfWarsTeamPopulator {
    override fun populateTeams(): Pair<List<Player>> {
        var team1: List<Player> = ArrayList<Player>()
        var team2: List<Player> = ArrayList<Player>()

        var all = ArrayList<Player>(Bukkit.getOnlinePlayers())

        all.shuffle()

        if (all.size > 0) {
            for (i in 0 until all.size / 2) {
                team1 += all.removeFirst()
            }
            team2 += all.removeFirst()
        }

        return Pair<List<Player>>(team1, team2)
    }
}
