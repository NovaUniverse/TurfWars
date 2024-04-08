package net.novauniverse.game.turfwars.game.mapmodules.config

import net.md_5.bungee.api.ChatColor
import net.zeeraa.novacore.commons.log.Log
import net.zeeraa.novacore.spigot.abstraction.VersionIndependentUtils
import net.zeeraa.novacore.spigot.abstraction.enums.ColoredBlockType
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.map.mapmodule.MapModule
import net.zeeraa.novacore.spigot.utils.LocationData
import net.zeeraa.novacore.spigot.utils.VectorArea
import org.bukkit.Color
import org.bukkit.DyeColor
import org.json.JSONObject

class TurfWarsConfig(json: JSONObject) : MapModule(json) {
    val team1: TeamConfig
    val team2: TeamConfig

    val floorMaterial: ColoredBlockType
    val buildingBlocks: ColoredBlockType

    val playArea: VectorArea

    val nightvision: Boolean = json.optBoolean("nightvision")

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

        val team1Spawns = ArrayList<LocationData>() //LocationData.fromJSON(team1JSON.getJSONObject("spawn_location"))
        val team2Spawns = ArrayList<LocationData>()//LocationData.fromJSON(team2JSON.getJSONObject("spawn_location"))

        val team1SpawnData = team1JSON.getJSONArray("spawn_location")
        val team2SpawnData = team2JSON.getJSONArray("spawn_location")

        for (i in 0 until  team1SpawnData.length()) {
            team1Spawns.add(LocationData.fromJSON(team1SpawnData.getJSONObject(i)))
        }

        for (i in 0 until team2SpawnData.length()) {
            team2Spawns.add(LocationData.fromJSON(team2SpawnData.getJSONObject(i)))
        }

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


        val team1RGB = if (team1JSON.has("rgb_color")) {
            val rgb = team1JSON.getJSONObject("rgb_color")
            Color.fromRGB(rgb.optInt("r", 0), rgb.optInt("g", 0), rgb.optInt("b", 0))
        } else {
            VersionIndependentUtils.get().bungeecordChatColorToBukkitColor(team1ChatColor)
        }

        val team2RGB = if (team2JSON.has("rgb_color")) {
            val rgb = team2JSON.getJSONObject("rgb_color")
            Color.fromRGB(rgb.optInt("r", 0), rgb.optInt("g", 0), rgb.optInt("b", 0))
        } else {
            VersionIndependentUtils.get().bungeecordChatColorToBukkitColor(team2ChatColor)
        }

        playArea = VectorArea.fromJSON(json.getJSONObject("play_area"))

        floorMaterial = ColoredBlockType.valueOf(json.optString("floor_type", ColoredBlockType.CLAY.name))
        buildingBlocks = ColoredBlockType.valueOf(json.optString("building_block_type", ColoredBlockType.WOOL.name))

        team1 = TeamConfig(team1ChatColor, team1Color, team1RGB, team1Spawns, team1DisplayName)
        team2 = TeamConfig(team2ChatColor, team2Color, team2RGB, team2Spawns, team2DisplayName)
    }
}