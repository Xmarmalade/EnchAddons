package net.skymoe.enchaddons.feature.dungeon.autokick

import net.skymoe.enchaddons.event.RegistryEventDispatcher
import net.skymoe.enchaddons.event.minecraft.ChatEvent
import net.skymoe.enchaddons.event.register
import net.skymoe.enchaddons.feature.FeatureBase
import net.skymoe.enchaddons.feature.dungeon.partyfinder.PartyFinderApi
import net.skymoe.enchaddons.feature.dungeon.partyfinder.PartyMember
import net.skymoe.enchaddons.feature.featureInfo
import net.skymoe.enchaddons.util.LogLevel
import net.skymoe.enchaddons.util.MC
import net.skymoe.enchaddons.util.modMessage
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

val AUTO_KICK_INFO = featureInfo<AutoKickConfig>("auto_kick", "Auto Kick")

object AutoKick : FeatureBase<AutoKickConfig>(AUTO_KICK_INFO) {
    private val background = Executors.newFixedThreadPool(2)
    private val players: MutableMap<String, PartyMember> = ConcurrentHashMap()

    // Regex derived from CT pattern, run on messageRaw (with § codes)
    private val joinRegexPrimary =
        Regex("§dParty Finder §r§f> §r§.([A-Za-z0-9_]+) §r§ejoined the dungeon group! \\(§r§b(\\w+) Level (\\w+)§r§e\\)§r")
    private val joinRegexFallback = Regex("Party Finder .*?> .*?([A-Za-z0-9_]+) .*?joined the dungeon group!")

    override fun registerEvents(dispatcher: RegistryEventDispatcher) {
        dispatcher.run {
            register<ChatEvent.Normal.Pre> { event ->
                val raw = event.messageRaw
                val m = joinRegexPrimary.find(raw) ?: joinRegexFallback.find(raw) ?: return@register
                val username = m.groupValues.getOrNull(1)?.takeIf { it.isNotEmpty() } ?: return@register

                if (!config.enabled) return@register

                val player = players.computeIfAbsent(username) { PartyMember(it) }

                // Ensure stats; then evaluate kick
                if (player.uuid == null && !player.inFlight) {
                    player.inFlight = true
                    background.execute {
                        try {
                            player.uuid = PartyFinderApi.requestUUID(username)
                            if (player.uuid != null) {
                                PartyFinderApi.requestStats(player.uuid!!)?.let { stats ->
                                    player.dungeons = stats.dungeons
                                    player.updateSecretAverage()
                                    player.markChanged()
                                }
                            }
                        } finally {
                            player.inFlight = false
                            checkAndKick(player)
                        }
                    }
                } else {
                    checkAndKick(player)
                }
            }
        }
    }

    private fun checkAndKick(player: PartyMember) {
        val pb = getRawPB(config.selectedFloor, player)
        val requiredPB = getRequiredPB()
        val secrets = player.dungeons.secrets
        val requiredSecrets = getRequiredSecrets()

        // Info line
        val pbStr = pb?.let { timeToString(it) } ?: "No S+"
        modMessage(
            "${player.name} | cata ${player.dungeons.catalevel ?: "?"} | secrets ${player.dungeons.secrets ?: "?"} | avg ${player.dungeons.secretAverage ?: "?"} | PB $pbStr",
            logLevel = LogLevel.INFO,
        )

        if (requiredPB != null && (pb?.let { it > requiredPB } == true || noSPlus(pb, player))) {
            kickPlayer("pb", player.name, pb ?: -1, requiredPB)
            return
        }
        if (secrets != null && requiredSecrets != null && secrets < requiredSecrets) {
            kickPlayer("secrets", player.name, secrets, requiredSecrets)
            return
        }
    }

    private fun kickPlayer(
        type: String,
        name: String,
        stat: Int,
        statRequirement: Int,
    ) {
        // Always show local prompt
        when (type) {
            "pb" ->
                modMessage(
                    "Kicking $name (PB: ${timeToString(stat)} | Req: ${timeToString(statRequirement)})",
                    logLevel = LogLevel.INFO,
                )
            "secrets" -> modMessage("Kicking $name (Secrets: $stat | Req: $statRequirement)", logLevel = LogLevel.INFO)
            else -> {
                modMessage("Kick cancelled for $name (Unknown Reason)", logLevel = LogLevel.WARN)
                return
            }
        }

        // If user disabled auto-kick, only prompt and return
        if (config.neverAutoKick) {
            modMessage("Auto-kick disabled. Use /p kick $name manually.", logLevel = LogLevel.INFO)
            return
        }

        try {
            if (config.kickMessageEnabled) {
                modMessage("Kicking $name...", logLevel = LogLevel.INFO)
            }
            if (config.partyExplainEnabled) {
                // Send a party chat explanation immediately
                val reason =
                    when (type) {
                        "pb" -> "PB: ${timeToString(stat)} | Req: ${timeToString(statRequirement)}"
                        "secrets" -> "Secrets: $stat | Req: $statRequirement"
                        else -> ""
                    }
                MC.thePlayer.sendChatMessage("/pc [EnchAddons] Sorry $name but ($reason)")
            }
            // Delay actual kick by ~1s
            background.execute {
                try {
                    Thread.sleep(1000)
                } catch (_: InterruptedException) {
                }
                MC.thePlayer.sendChatMessage("/p kick $name")
            }
        } catch (_: Exception) {
            // Unknown failure during kick: only prompt
            modMessage("Kick cancelled for $name (Unknown Failure)", logLevel = LogLevel.ERROR)
        }
    }

    private fun getRawPB(
        selected: Int,
        player: PartyMember,
    ): Int? {
        val map = player.dungeons.pb
        val raw =
            when (selected) {
                0 -> map["catacombs"]?.get(7)?.get("rawS+")
                1 -> map["master_catacombs"]?.get(4)?.get("rawS+")
                2 -> map["master_catacombs"]?.get(5)?.get("rawS+")
                3 -> map["master_catacombs"]?.get(6)?.get("rawS+")
                4 -> map["master_catacombs"]?.get(7)?.get("rawS+")
                else -> null
            }
        return raw?.toIntOrNull()
    }

    private fun getRequiredPB(): Int? {
        val sec = config.requiredPBSeconds
        if (sec <= 0f) return null
        return (sec * 1000f).toInt()
    }

    private fun getRequiredSecrets(): Int? {
        val s = config.requiredSecrets.trim().lowercase()
        if (s.isEmpty()) return null
        s.toIntOrNull()?.let { return it }
        val k = Regex("^\\d+k$")
        if (k.matches(s)) {
            val raw = s.removeSuffix("k")
            return (raw.toIntOrNull() ?: return null) * 1000
        }
        return null
    }

    private fun noSPlus(
        pb: Int?,
        player: PartyMember,
    ): Boolean = pb == null && player.dungeons.catalevel != null && player.dungeons.secrets != null && player.dungeons.runs != null

    private fun timeToString(ms: Int): String {
        if (ms < 0) return "No S+"
        val totalSec = ms / 1000
        val m = totalSec / 60
        val s = totalSec % 60
        return String.format("%d:%02d", m, s)
    }
}
