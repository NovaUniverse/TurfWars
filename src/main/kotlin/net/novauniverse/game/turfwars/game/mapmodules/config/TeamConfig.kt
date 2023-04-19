package net.novauniverse.game.turfwars.game.mapmodules.config

import net.md_5.bungee.api.ChatColor
import net.zeeraa.novacore.spigot.utils.LocationData
import org.bukkit.DyeColor

data class TeamConfig(val chatColor: ChatColor, val dyeColor: DyeColor, val spawnLocation: LocationData, val displayName: String)