package net.novauniverse.game.turfwars.game

import net.md_5.bungee.api.ChatColor
import net.novauniverse.game.turfwars.TurfWarsPlugin
import net.novauniverse.game.turfwars.game.data.PlayerData
import net.novauniverse.game.turfwars.game.event.TurfWarsBeginEvent
import net.novauniverse.game.turfwars.game.event.TurfWarsTurfChangeEvent
import net.novauniverse.game.turfwars.game.mapmodules.config.TurfWarsConfig
import net.novauniverse.game.turfwars.game.team.TurfWarsTeam
import net.novauniverse.game.turfwars.game.team.TurfWarsTeamData
import net.zeeraa.novacore.commons.log.Log
import net.zeeraa.novacore.commons.tasks.Task
import net.zeeraa.novacore.spigot.abstraction.VersionIndependentUtils
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameEndReason
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.MapGame
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.elimination.PlayerQuitEliminationAction
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.triggers.DelayedGameTrigger
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.triggers.GameTrigger
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.triggers.RepeatingGameTrigger
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.triggers.TriggerFlag
import net.zeeraa.novacore.spigot.gamerule.Gamerule
import net.zeeraa.novacore.spigot.language.LanguageManager
import net.zeeraa.novacore.spigot.tasks.SimpleTask
import net.zeeraa.novacore.spigot.utils.InventoryUtils
import net.zeeraa.novacore.spigot.utils.ItemBuilder
import net.zeeraa.novacore.spigot.utils.PlayerUtils
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import kotlin.math.abs
import kotlin.math.floor


class TurfWars(plugin: TurfWarsPlugin) : MapGame(plugin), Listener {
    private var started = false
    private var ended = false
    private val turfWarsPlugin: TurfWarsPlugin

    private var playerDataList: List<PlayerData> = ArrayList()

    private var tickTask: SimpleTask? = null

    private var floorMaterials: ArrayList<Material> = ArrayList()
    private var buildMaterials: ArrayList<Material> = ArrayList()

    var winner: TurfWarsTeam? = null
        private set

    var initialMiddleLocation = 0
        private set

    var turfSize = 0
        private set

    var goal = 0
        private set

    var team1FrontLine = 0
        private set

    var team1: TurfWarsTeamData? = null
        private set

    var team2: TurfWarsTeamData? = null
        private set

    var buildTimeEndTrigger: DelayedGameTrigger? = null
        private set
    var buildTimeStartTrigger: DelayedGameTrigger? = null
        private set

    var giveArrowTrigger: RepeatingGameTrigger? = null
        private set

    init {
        turfWarsPlugin = plugin
    }

    var config: TurfWarsConfig? = null
        get
        private set

    fun getTeam(team: TurfWarsTeam): TurfWarsTeamData {
        if (team == TurfWarsTeam.TEAM_1) {
            return team1!!
        }
        return team2!!
    }

    fun getTeam(uuid: UUID): TurfWarsTeamData? {
        if (team1!!.isMember(uuid)) {
            return team1
        }

        if (team2!!.isMember(uuid)) {
            return team2
        }

        return null
    }

    fun getTeam(player: Player): TurfWarsTeamData? {
        return getTeam(player.uniqueId)
    }

    fun getPlayerData(uuid: UUID): PlayerData? {
        return playerDataList.stream().filter { d: PlayerData -> d.uuid.equals(uuid) }.findFirst().orElse(null)
    }

    fun getPlayerData(player: Player): PlayerData? {
        return getPlayerData(player.uniqueId)
    }

    override fun getName(): String {
        return "turfwars"
    }

    override fun getDisplayName(): String {
        return "TurfWars"
    }

    override fun getPlayerQuitEliminationAction(): PlayerQuitEliminationAction {
        return PlayerQuitEliminationAction.DELAYED
    }

    override fun eliminatePlayerOnDeath(player: Player?): Boolean {
        return false
    }

    override fun isPVPEnabled(): Boolean {
        return !buildPeriodActive
    }

    override fun autoEndGame(): Boolean {
        return false
    }

    override fun hasStarted(): Boolean {
        return started
    }

    override fun hasEnded(): Boolean {
        return ended
    }

    override fun isFriendlyFireAllowed(): Boolean {
        return false
    }

    override fun canAttack(attacker: LivingEntity?, target: LivingEntity?): Boolean {
        if (attacker is Player && target is Player) {

            val team1 = getTeam(attacker.uniqueId)
            val team2 = getTeam(target.uniqueId)

            if (team1 != null && team2 != null) {
                return team1.team != team2.team
            }
            return false
        }
        return true;
    }

    fun tpPlayer(player: Player) {
        val turfWarsTeamData: TurfWarsTeamData? = getTeam(player)
        if (turfWarsTeamData != null) {
            player.fallDistance = 0F
            player.fireTicks = 0
            player.health = 20.0

            player.inventory.setItem(0, ItemBuilder(Material.BOW).setName("${turfWarsTeamData.teamConfig.chatColor}Bow").setUnbreakable(true).build())
            player.inventory.setItem(1, ItemBuilder(Material.STONE_SWORD).setName("${turfWarsTeamData.teamConfig.chatColor}Dagger").setUnbreakable(true).build())

            player.inventory.helmet = ItemBuilder(Material.LEATHER_HELMET).setLeatherArmorColor(turfWarsTeamData.teamConfig.color).setUnbreakable(true).build()
            player.inventory.chestplate = ItemBuilder(Material.LEATHER_CHESTPLATE).setLeatherArmorColor(turfWarsTeamData.teamConfig.color).setUnbreakable(true).build()
            player.inventory.leggings = ItemBuilder(Material.LEATHER_LEGGINGS).setLeatherArmorColor(turfWarsTeamData.teamConfig.color).setUnbreakable(true).build()
            player.inventory.boots = ItemBuilder(Material.LEATHER_BOOTS).setLeatherArmorColor(turfWarsTeamData.teamConfig.color).setUnbreakable(true).build()

            player.gameMode = GameMode.SURVIVAL
            player.teleport(turfWarsTeamData.teamConfig.spawnLocation.toLocation(world))

            addArrow(player)
        } else {
            tpToSpectator(player)
        }
    }

    fun addArrow(player: Player) {
        val arrowCount = InventoryUtils.countItemsOfType(player.inventory, Material.ARROW)
        if (arrowCount == 0) {
            player.inventory.setItem(8, ItemBuilder.materialToItemStack(Material.ARROW, 1))
        } else if (arrowCount < config!!.maxArrows) {
            player.inventory.addItem(ItemBuilder.materialToItemStack(Material.ARROW, 1))
        }
    }

    var buildPeriodActive = true
        private set

    private fun startBuildTime() {
        buildPeriodActive = true

        VersionIndependentUtils.get().broadcastTitle("${ChatColor.GREEN}Build", "${ChatColor.AQUA}Build period ends in ${config!!.buildTime} seconds", 0, 60, 20)

        Gamerule.DO_TILE_DROPS.set(world, true)

        Bukkit.getOnlinePlayers().stream().filter(this::isPlayerInGame).forEach {
            PlayerUtils.fullyHealPlayer(it)
            val team = getTeam(it)
            if (team != null) {
                it.inventory.setItem(2, ItemBuilder(config!!.buildingBlocks, team.teamConfig.dyeColor).setAmount(32).build())
            }
        }
        buildTimeEndTrigger?.start()
    }

    private fun endBuildTime() {
        buildPeriodActive = false

        VersionIndependentUtils.get().broadcastTitle("${ChatColor.RED}Fight", "${ChatColor.AQUA}Build period starts in ${config!!.combatTime} seconds", 0, 60, 20)

        Gamerule.DO_TILE_DROPS.set(world, false)

        Bukkit.getOnlinePlayers().filter(this::isPlayerInGame).forEach {
            it.inventory.setItem(2, ItemStack(Material.AIR))
        }
        buildTimeStartTrigger?.start()
    }

    private fun addTeamKill(team: TurfWarsTeamData) {
        if (ended) {
            return
        }

        team.addKill()

        val capturedLocation = if (team.team == TurfWarsTeam.TEAM_1) team1FrontLine + 1 else team1FrontLine

        val config: TurfWarsConfig = this.config!!;

        val diff: Int = team1!!.kills - team2!!.kills
        team1FrontLine = initialMiddleLocation + diff
        val floor = config.floorMaterial

        val floorY = config.playArea.position1.blockY;
        for (z in config.playArea.position1.blockZ..config.playArea.position2.blockZ) {
            val location = Location(world, capturedLocation.toDouble(), floorY.toDouble(), z.toDouble())
            if (floorMaterials.contains(location.block.type)) {
                VersionIndependentUtils.get().setColoredBlock(location.block, team.teamConfig.dyeColor, floor)
            }

            for (y in config.playArea.position1.blockY + 1..config.playArea.position2.blockY) {
                val checkLocation = Location(world, capturedLocation.toDouble(), y.toDouble(), location.z)
                if (buildMaterials.contains(checkLocation.block.type)) {
                    breakBlockWithEffect(checkLocation.block)
                }
            }
        }

        val team1Turf: Int = turfSize / 2 + diff
        val team2Turf: Int = turfSize / 2 - diff

        Bukkit.getServer().pluginManager.callEvent(TurfWarsTurfChangeEvent(team1Turf, team2Turf))

        if (abs(diff) >= goal) {
            winner = if (diff > 0) TurfWarsTeam.TEAM_1 else TurfWarsTeam.TEAM_2
            endGame(GameEndReason.WIN)
        }
    }

    override fun onStart() {
        if (started) {
            return
        }

        config = activeMap.mapData.getMapModule(TurfWarsConfig::class.java)
        if (config == null) {
            Log.error("TurfWars", "No turf wars config map module was loaded. Cant continue")
            return
        }

        if (turfWarsPlugin.turfWarsTeamPopulator == null) {
            Log.error("TurfWars", "No turf wars team populator was loaded")
            return
        }

        giveArrowTrigger = RepeatingGameTrigger("turfwars.givearrows", config!!.arrowRegenSpeed * 20L, config!!.arrowRegenSpeed * 20L) { _: GameTrigger, _: TriggerFlag? ->
            if (!buildPeriodActive) {
                Bukkit.getOnlinePlayers().stream().filter(this::isPlayerInGame).forEach(this::addArrow)
            }
        }
        giveArrowTrigger!!.addFlag(TriggerFlag.START_ON_GAME_START)
        giveArrowTrigger!!.addFlag(TriggerFlag.STOP_ON_GAME_END)
        giveArrowTrigger!!.addFlag(TriggerFlag.DISABLE_LOGGING)

        buildTimeEndTrigger = DelayedGameTrigger("turfwars.endbuildtime", config!!.buildTime * 20L) { _: GameTrigger, _: TriggerFlag? ->
            endBuildTime()
        }
        buildTimeEndTrigger!!.addFlag(TriggerFlag.STOP_ON_GAME_END)

        buildTimeStartTrigger = DelayedGameTrigger("turfwars.startbuildtime", config!!.combatTime * 20L) { _: GameTrigger, _: TriggerFlag? ->
            startBuildTime()
        }
        buildTimeStartTrigger!!.addFlag(TriggerFlag.STOP_ON_GAME_END)

        addTrigger(giveArrowTrigger!!)
        addTrigger(buildTimeEndTrigger!!)
        addTrigger(buildTimeStartTrigger!!)

        tickTask = SimpleTask(plugin, {
            playerDataList.forEach(PlayerData::tick)

            Bukkit.getOnlinePlayers().forEach {
                it.foodLevel = 20
                it.saturation = 20F
            }

            if (team2!!.members.stream().noneMatch(this::isPlayerInGame)) {
                winner = team2!!.team.getOpposite()
                endGame(GameEndReason.WIN)
            } else if (team1!!.members.stream().noneMatch(this::isPlayerInGame)) {
                winner = team1!!.team.getOpposite()
                endGame(GameEndReason.WIN)
            }
        }, 1L, 1L)

        team1 = TurfWarsTeamData(TurfWarsTeam.TEAM_1, config!!.team1)
        team2 = TurfWarsTeamData(TurfWarsTeam.TEAM_2, config!!.team2)

        val teamPairs = turfWarsPlugin.turfWarsTeamPopulator!!.populateTeams()

        teamPairs.object1.forEach {
            addPlayer(it)
            team1!!.members += it.uniqueId
        }

        teamPairs.object2.forEach {
            addPlayer(it)
            team2!!.members += it.uniqueId
        }

        turfSize = (config!!.playArea.position2.blockX - config!!.playArea.position1.blockX) + 1
        goal = floor(turfSize.toDouble() / 2.0).toInt()

        initialMiddleLocation = config!!.playArea.position1.blockX + floor(turfSize.toDouble() / 2.0).toInt() - 1
        team1FrontLine = initialMiddleLocation

        Log.debug("TurfWars", "Arena length is $turfSize. Goal is $goal")

        Log.trace("TurfWars", "Team 1 size: " + team1!!.members.size + " Team 2 size: " + team2!!.members.size)

        Bukkit.getServer().onlinePlayers.forEach {
            PlayerUtils.clearPlayerInventory(it)
            PlayerUtils.resetPlayerXP(it)
            PlayerUtils.resetMaxHealth(it)
            PlayerUtils.clearPotionEffects(it)
            tpPlayer(it)
        }

        Gamerule.KEEP_INVENTORY.set(world, true)
        Gamerule.DO_TILE_DROPS.set(world, false)

        Task.tryStartTask(tickTask)

        sampleColoredMaterials()

        startBuildTime()

        started = true
        sendBeginEvent()

        Bukkit.getServer().pluginManager.callEvent(TurfWarsBeginEvent(goal, team1!!, team2!!))
    }

    // To support both 1.8 and 1.16+ we need this
    private fun sampleColoredMaterials() {
        floorMaterials.clear()
        buildMaterials.clear()

        val location = Location(world, 69420.0, 1.0, 69420.0)

        VersionIndependentUtils.get().setColoredBlock(location.block, team1!!.teamConfig.dyeColor, config!!.floorMaterial)
        floorMaterials.add(location.block.type)

        location.add(1.0, 0.0, 0.0)
        VersionIndependentUtils.get().setColoredBlock(location.block, team1!!.teamConfig.dyeColor, config!!.buildingBlocks)
        buildMaterials.add(location.block.type)

        location.add(1.0, 0.0, 0.0)
        VersionIndependentUtils.get().setColoredBlock(location.block, team2!!.teamConfig.dyeColor, config!!.floorMaterial)
        if (!floorMaterials.contains(location.block.type)) {
            floorMaterials.add(location.block.type)
        }

        location.add(1.0, 0.0, 0.0)
        VersionIndependentUtils.get().setColoredBlock(location.block, team2!!.teamConfig.dyeColor, config!!.buildingBlocks)
        if (!buildMaterials.contains(location.block.type)) {
            buildMaterials.add(location.block.type)
        }

        Log.debug("TurfWarsMaterialCache", "Floor materials: ${floorMaterials.size} Build materials: ${buildMaterials.size}")
    }

    override fun onPlayerAdded(player: Player) {
        if (!playerDataList.stream().anyMatch { d: PlayerData -> d.uuid.equals(player.uniqueId) }) {
            playerDataList += PlayerData(player.uniqueId)
        }
    }

    override fun onEnd(reason: GameEndReason?) {
        if (ended) {
            return
        }

        if (reason == GameEndReason.WIN) {
            if (winner != null) {
                val teamData = getTeam(winner!!)
                LanguageManager.broadcast("turfwars.end.win", teamData.teamConfig.chatColor, teamData.teamConfig.displayName)
            }
        } else {
            LanguageManager.broadcast("turfwars.end.ended")
        }

        Task.tryStopTask(tickTask)

        ended = true
    }

    override fun onPlayerRespawn(player: Player) {
        object : BukkitRunnable() {
            override fun run() {
                tpToSpectator(player)
                val playerData = getPlayerData(player)
                if (playerData != null) {
                    playerData.respawnTicks = config!!.respawnTime * 20
                }
            }
        }.runTaskLater(plugin, 5L)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerDeath(e: PlayerDeathEvent) {
        val player = e.entity

        var playerColor = ChatColor.AQUA
        var enemyColor = ChatColor.AQUA

        if (isPlayerInGame(player)) {
            val teamData = getTeam(player)
            if (teamData != null) {
                playerColor = teamData.teamConfig.chatColor
                val oppositeTeam = getTeam(teamData.team.getOpposite())
                enemyColor = oppositeTeam.teamConfig.chatColor
                addTeamKill(oppositeTeam)
            }
        }


        if (player.killer != null) {
            if (player.killer is Player) {
                val killer: Player = e.entity.killer
                LanguageManager.broadcast("turfwars.death.killed", playerColor, player.name, enemyColor, killer.name)
                getPlayerData(killer)?.incrementKills()
                return
            }
        }

        LanguageManager.broadcast("turfwars.death.death", playerColor, player.name)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        if (!started) {
            return
        }

        val player = e.player

        tpToSpectator(player)

        if (isPlayerInGame(player)) {
            val playerData = getPlayerData(player)
            if (playerData != null) {
                playerData.respawnTicks = config!!.respawnTime * 20
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onBlockPlace(e: BlockPlaceEvent) {
        if (!started) {
            return
        }

        val player = e.player

        if (player.gameMode == GameMode.CREATIVE) {
            return
        }

        if (!config!!.playArea.isInsideBlock(e.block)) {
            player.sendMessage("${ChatColor.RED}You cant place blocks here")
            e.isCancelled = true
            return
        }

        val team = getTeam(player)
        if (team != null) {
            if ((team.team == TurfWarsTeam.TEAM_1 && e.block.location.x > team1FrontLine) || (team.team == TurfWarsTeam.TEAM_2 && e.block.location.x <= team1FrontLine)) {
                player.sendMessage("${ChatColor.RED}You cant build on the other teams turf")
                e.isCancelled = true
                return
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onBlockBreak(e: BlockBreakEvent) {
        if (!started) {
            return
        }

        if (e.player.gameMode == GameMode.CREATIVE) {
            return
        }

        if (!buildMaterials.contains(e.block.type)) {
            e.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onPlayerDropItem(e: PlayerDropItemEvent) {
        if (!started) {
            return
        }

        if (e.player.gameMode == GameMode.CREATIVE) {
            return
        }

        e.isCancelled = true
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onPlayerPickupItem(e: PlayerPickupItemEvent) {
        if (!started) {
            return
        }

        if (e.player.gameMode == GameMode.CREATIVE) {
            return
        }

        if (e.getItem().getItemStack().getType() == Material.ARROW) {
            e.setCancelled(true)
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onEntityShootBow(e: EntityShootBowEvent) {
        if (!started) {
            return
        }

        if (!buildPeriodActive) {
            return
        }

        e.isCancelled = true
    }

    fun breakBlockWithEffect(block: Block) {
        VersionIndependentUtils.get().showBlockBreakParticles(block, 10)
        block.breakNaturally()
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun onProjectileHit(e: ProjectileHitEvent) {
        if (!started) {
            return
        }

        if (e.entity is Arrow) {
            object : BukkitRunnable() {
                override fun run() {
                    val estimatedHitBlock = VersionIndependentUtils.get().getBlockFromProjectileHitEvent(e)
                    if (estimatedHitBlock != null) {
                        if (VersionIndependentUtils.get().isArrowInBlock(e.entity as Arrow)) {
                            if (buildMaterials.contains(estimatedHitBlock.type)) {
                                breakBlockWithEffect(estimatedHitBlock)
                                e.entity.remove()
                            }
                        }
                    }
                }
            }.runTaskLater(plugin, 1L)
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onInventoryInteract(e: InventoryInteractEvent) {
        if (!started) {
            return
        }

        if (e.whoClicked.gameMode == GameMode.CREATIVE) {
            return
        }

        e.isCancelled = true
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onEntityDamageByEntity(e: EntityDamageByEntityEvent) {
        if (!started) {
            return
        }

        if (buildPeriodActive) {
            e.isCancelled = true
            return
        }

        if (e.damager is Arrow) {
            e.damage = 1000.0
        }
    }
}