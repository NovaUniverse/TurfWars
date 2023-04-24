package net.novauniverse.game.turfwars

import net.novauniverse.game.turfwars.game.TurfWars
import net.novauniverse.game.turfwars.game.mapmodules.config.TurfWarsConfig
import net.novauniverse.game.turfwars.game.team.teampopulator.DefaultTeamPopulator
import net.novauniverse.game.turfwars.game.team.teampopulator.TurfWarsTeamPopulator
import net.zeeraa.novacore.commons.log.Log
import net.zeeraa.novacore.spigot.gameengine.NovaCoreGameEngine
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameManager
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.map.mapmodule.MapModuleManager
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.mapselector.selectors.guivoteselector.GUIMapVote
import net.zeeraa.novacore.spigot.gameengine.module.modules.gamelobby.GameLobby
import net.zeeraa.novacore.spigot.language.LanguageReader
import net.zeeraa.novacore.spigot.module.ModuleManager
import org.apache.commons.io.FileUtils
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class TurfWarsPlugin : JavaPlugin() {
    var game: TurfWars? = null
        get() = field
        private set

    var turfWarsTeamPopulator: TurfWarsTeamPopulator? = DefaultTeamPopulator()
        get
        set

    companion object {
        private var gameInstance: TurfWarsPlugin? = null

        @JvmStatic
        fun getInstance(): TurfWarsPlugin {
            return gameInstance!!
        }
    }

    override fun onEnable() {
        gameInstance = this

        saveDefaultConfig()

        Log.info("TurfWars", "Loading language files...")
        try {
            LanguageReader.readFromJar(this.javaClass, "/lang/en-us.json")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        ModuleManager.require(GameManager::class.java)
        ModuleManager.require(GameLobby::class.java)

        var mapFolder = File(dataFolder.path + File.separator + "Maps")
        var worldFolder = File(dataFolder.path + File.separator + "Worlds")

        if (NovaCoreGameEngine.getInstance().requestedGameDataDirectory != null) {
            val dataDir = File(NovaCoreGameEngine.getInstance().requestedGameDataDirectory.absolutePath + File.separator + "TurfWars")
            FileUtils.forceMkdir(dataDir)
            mapFolder = File(dataDir.absolutePath + File.separator + "Maps")
            worldFolder = File(dataDir.absolutePath + File.separator + "Worlds")
        }

        FileUtils.forceMkdir(mapFolder)
        FileUtils.forceMkdir(worldFolder)

        MapModuleManager.addMapModule("turfwars.config", TurfWarsConfig::class.java)

        ModuleManager.getModule(GameLobby::class.java).isDisableAutoAddPlayers = true

        val mapSelector = GUIMapVote()
        Bukkit.getServer().pluginManager.registerEvents(mapSelector, this)

        game = TurfWars(this)
        var gameManager = ModuleManager.getModule(GameManager::class.java)
        gameManager.loadGame(game)
        gameManager.isUseCombatTagging = false
        gameManager.mapSelector = mapSelector

        Log.info(name, "Scheduled loading maps from " + mapFolder.path)
        gameManager.readMapsFromFolderDelayed(mapFolder, worldFolder)
    }

    override fun onDisable() {
        HandlerList.unregisterAll(this)
    }
}