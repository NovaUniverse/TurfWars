package net.novauniverse.game.turfwars.game.mapmodules.config

import net.md_5.bungee.api.ChatColor
import net.zeeraa.novacore.spigot.abstraction.enums.ColoredBlockType
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.map.mapmodule.MapModule
import net.zeeraa.novacore.spigot.utils.ChatColorRGBMapper
import net.zeeraa.novacore.spigot.utils.LocationData
import net.zeeraa.novacore.spigot.utils.VectorArea
import org.bukkit.DyeColor
import org.json.JSONObject

class TurfWarsConfig(json: JSONObject) : MapModule(json) {
    val team1: TeamConfig
    val team2: TeamConfig

    val floorMaterial: ColoredBlockType
    val buildingBlocks: ColoredBlockType

    val playArea: VectorArea

    var respawnTime = 5
        private set

    var maxArrows = 10
        private set

    var arrowRegenSpeed = 3
        private set

    var buildTime = 30
        private set

    var combatTime = 120
        private set

    init {
        val team1JSON = json.getJSONObject("team_1")
        val team2JSON = json.getJSONObject("team_2")

        val team1Spawn = LocationData.fromJSON(team1JSON.getJSONObject("spawn_location"))
        val team2Spawn = LocationData.fromJSON(team2JSON.getJSONObject("spawn_location"))

        val team1Color = DyeColor.valueOf(team1JSON.optString("color", DyeColor.RED.name))
        val team2Color = DyeColor.valueOf(team2JSON.optString("color", DyeColor.BLUE.name))

        val team1ChatColor = ChatColor.valueOf(team1JSON.optString("chat_color", ChatColor.RED.name))
        val team2ChatColor = ChatColor.valueOf(team2JSON.optString("chat_color", ChatColor.BLUE.name))

        maxArrows = json.optInt("max_arrows", maxArrows)
        arrowRegenSpeed = json.optInt("arrow_regen_speed", arrowRegenSpeed)
        buildTime = json.optInt("build_time", buildTime)
        combatTime = json.optInt("combat_time", combatTime)
        respawnTime = json.optInt("respawn_time", respawnTime)

        val team1DisplayName = json.optString("display_name", "Red Team")
        val team2DisplayName = json.optString("display_name", "Blue Team")

        playArea = VectorArea.fromJSON(json.getJSONObject("play_area"))

        floorMaterial = ColoredBlockType.valueOf(json.optString("floor_type", ColoredBlockType.CLAY.name))
        buildingBlocks = ColoredBlockType.valueOf(json.optString("building_block_type", ColoredBlockType.WOOL.name))

        team1 = TeamConfig(team1ChatColor, team1Color, team1Spawn, team1DisplayName)
        team2 = TeamConfig(team2ChatColor, team2Color, team2Spawn, team2DisplayName)
    }
}