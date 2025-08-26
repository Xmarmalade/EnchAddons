package net.skymoe.enchaddons.impl.config.feature

import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.elements.SubConfig
import net.skymoe.enchaddons.feature.dungeon.partyfinder.PARTY_FINDER_STATS_INFO
import net.skymoe.enchaddons.feature.dungeon.partyfinder.PartyFinderStatsConfig
import net.skymoe.enchaddons.impl.config.ConfigImpl
import net.skymoe.enchaddons.impl.config.EnchAddonsConfig

class PartyFinderStatsConfigImpl :
    ConfigImpl(PARTY_FINDER_STATS_INFO),
    PartyFinderStatsConfig {
    override val enabled get() =
        EnchAddonsConfig.enabled &&
            (EnchAddonsConfig.dungeonConfig as SubConfig).enabled &&
            (this as SubConfig).enabled

    @Switch(
        name = "Party Finder Stats",
        description = "Shows stats of players in Party Finder.",
        category = "Party Finder Stats",
        subcategory = "Overlay",
    )
    override var partyfinderEnabled: Boolean = false

    @Switch(
        name = "Show cata level",
        description = "Shows the player's catacombs level in party finder.",
        category = "Party Finder Stats",
        subcategory = "Overlay",
    )
    override var partyfinderCata: Boolean = true

    @Switch(
        name = "Show total secrets",
        description = "Shows the player's total amount of secrets in party finder.",
        category = "Party Finder Stats",
        subcategory = "Overlay",
    )
    override var partyfinderSecrets: Boolean = true

    @Switch(
        name = "Show secret average",
        description = "Shows the player's secret average in party finder.",
        category = "Party Finder Stats",
        subcategory = "Overlay",
    )
    override var partyfinderSecretAverage: Boolean = true

    @Switch(
        name = "Show S+ PB",
        description = "Shows the player's fastest S+ time for the current floor in party finder.",
        category = "Party Finder Stats",
        subcategory = "Overlay",
    )
    override var partyfinderPB: Boolean = true

    @Switch(
        name = "Show missing classes",
        description = "Displays missing classes in M4/M6/M7 party finder.",
        category = "Party Finder Stats",
        subcategory = "Overlay",
    )
    override var missingclasses: Boolean = true

    @Switch(
        name = "Debug logs",
        description = "Log debug info for Party Finder Stats.",
        category = "Party Finder Stats",
        subcategory = "Debug",
    )
    override var debugLogs: Boolean = false
}
