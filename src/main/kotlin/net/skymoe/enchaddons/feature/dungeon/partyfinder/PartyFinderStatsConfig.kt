package net.skymoe.enchaddons.feature.dungeon.partyfinder

import net.skymoe.enchaddons.feature.config.FeatureConfig

interface PartyFinderStatsConfig : FeatureConfig {
    var partyfinderEnabled: Boolean
    var partyfinderCata: Boolean
    var partyfinderSecrets: Boolean
    var partyfinderSecretAverage: Boolean
    var partyfinderPB: Boolean
    var missingclasses: Boolean

    // Debug
    var debugLogs: Boolean
}
