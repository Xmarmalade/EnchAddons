package net.skymoe.enchaddons.feature.dungeon.autokick

import net.skymoe.enchaddons.feature.config.FeatureConfig

interface AutoKickConfig : FeatureConfig {
    // Local info prompt before kicking
    val kickMessageEnabled: Boolean
    // Do not auto-kick; only show local info
    val neverAutoKick: Boolean
    // Also send a party chat explanation before kicking
    val partyExplainEnabled: Boolean

    // Thresholds
    var requiredPBSeconds: Float
    var requiredSecrets: String
    var selectedFloor: Int
}

