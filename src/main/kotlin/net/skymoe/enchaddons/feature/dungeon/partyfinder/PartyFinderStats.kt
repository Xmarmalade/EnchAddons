package net.skymoe.enchaddons.feature.dungeon.partyfinder

import net.skymoe.enchaddons.feature.FeatureBase
import net.skymoe.enchaddons.feature.buildRegisterEventEntries
import net.skymoe.enchaddons.feature.featureInfo
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

val PARTY_FINDER_STATS_INFO = featureInfo<PartyFinderStatsConfig>("party_finder_stats", "Party Finder Stats")

object PartyFinderStats : FeatureBase<PartyFinderStatsConfig>(PARTY_FINDER_STATS_INFO) {
    private val background = Executors.newCachedThreadPool()

    // Cache for players
    private val players: MutableMap<String, PartyMember> = ConcurrentHashMap()

    override fun registerEvents(dispatcher: net.skymoe.enchaddons.event.RegistryEventDispatcher) {
        // No custom EA events needed; handled by Forge ItemTooltipEvent above
        buildRegisterEventEntries {}
    }

    // Called from mixin redirect in GuiContainerMixin
    fun onTooltip(tooltip: MutableList<String>) {
        val cfg = config
        if (!cfg.enabled) return
        if (tooltip.isEmpty()) return

        // Prepare filtered lore (without technical lines) for parsing
        val loreForParsing =
            tooltip
                .drop(1) // skip name line
                .filter { !it.contains("minecraft:") && !it.contains("NBT:") }

        val floor = getFloor(loreForParsing)
        val dungeonType = getDungeonType(loreForParsing)

        var hasChanged = false

        // Missing classes line handling
        if (cfg.missingclasses && dungeonType == "master_catacombs" && (floor == 4 || floor == 6 || floor == 7) &&
            !hasMissingClasses(tooltip)
        ) {
            val missing = getMissingClasses(loreForParsing)
            if (missing.isNotEmpty()) {
                tooltip.add("§e§lMissing:§r§f ${missing.joinToString(", ")}")
                hasChanged = true
            }
        }

        // Map and possibly modify party member lines
        for (i in tooltip.indices) {
            val line = tooltip[i]
            val matched = PARTY_MEMBER_LINE.matcher(line)
            if (!matched.find()) {
                // strip stray §5§o if present to match CT behavior
                if (line.contains("§5§o")) {
                    val replaced = line.replace("§5§o", "")
                    if (replaced != line) {
                        tooltip[i] = replaced
                        hasChanged = true
                    }
                }
                continue
            }

            val username = getUsername(line)
            if (username.isNullOrEmpty()) continue

            val player = players.computeIfAbsent(username) { PartyMember(it) }

            // if we've already appended a custom suffix and nothing has changed, leave as-is
            if (hasCustomSuffix(line) && !player.hasChanged()) continue

            // kick off init if needed
            if (player.uuid == null && !player.inFlight) {
                player.inFlight = true
                background.execute {
                    try {
                        player.uuid = PartyFinderApi.requestUUID(username)
                        if (player.uuid != null) {
                            val stats = PartyFinderApi.requestStats(player.uuid!!)
                            if (stats != null) {
                                player.dungeons = stats.dungeons
                                player.updateSecretAverage()
                                player.markChanged()
                            }
                        }
                    } catch (_: Exception) {
                    } finally {
                        player.inFlight = false
                    }
                }
            }

            val updated = createSuffix(line, player, floor, dungeonType, cfg)
            if (updated != line) {
                tooltip[i] = updated
                hasChanged = true
            }
        }

        // No explicit set; tooltip is a live list
    }

    // Utils
    private fun hasMissingClasses(lore: List<String>): Boolean = lore.any { it.contains("Missing:§r") }
}
