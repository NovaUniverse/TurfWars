package net.novauniverse.game.turfwars.game.mapmodules.config

import net.md_5.bungee.api.ChatColor
import net.zeeraa.novacore.spigot.abstraction.bossbar.NovaBossBar.NovaBarColor
import net.zeeraa.novacore.spigot.utils.LocationData
import net.zeeraa.novacore.spigot.utils.VectorArea
import org.bukkit.Color
import org.bukkit.DyeColor

data class TeamConfig(val chatColor: ChatColor, val dyeColor: DyeColor, val color: Color, val spawnLocations: List<LocationData>, val spawnArea: VectorArea, val displayName: String, val bossBarColor: NovaBarColor)