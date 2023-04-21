package net.novauniverse.game.turfwars.game.team.teampopulator

import org.bukkit.entity.Player

interface TurfWarsTeamPopulator {
    fun populateTeams(): net.zeeraa.novacore.commons.utils.Pair<List<Player>>
}