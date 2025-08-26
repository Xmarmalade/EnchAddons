package net.skymoe.enchaddons.impl.config.feature

import cc.polyfrost.oneconfig.config.annotations.Dropdown
import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.annotations.Text
import cc.polyfrost.oneconfig.config.annotations.Slider
import cc.polyfrost.oneconfig.config.elements.SubConfig
import net.skymoe.enchaddons.feature.dungeon.autokick.AutoKickConfig
import net.skymoe.enchaddons.feature.featureInfo
import net.skymoe.enchaddons.impl.config.ConfigImpl
import net.skymoe.enchaddons.impl.config.EnchAddonsConfig

class AutoKickConfigImpl : ConfigImpl(featureInfo<AutoKickConfig>("auto_kick", "Auto Kick")), AutoKickConfig {
    override val enabled get() =
        EnchAddonsConfig.enabled &&
            (EnchAddonsConfig.dungeonConfig as SubConfig).enabled &&
            (this as SubConfig).enabled

    @Switch(
        name = "Show reason locally",
        description = "Show a local reason message before kicking.",
        category = "Auto Kick",
        subcategory = "General",
    )
    override var kickMessageEnabled: Boolean = true

    @Switch(
        name = "Never auto-kick",
        description = "Do not execute kick commands. You will kick manually.",
        category = "Auto Kick",
        subcategory = "General",
    )
    override var neverAutoKick: Boolean = false

    @Switch(
        name = "Send party explanation",
        description = "Send a /pc message with the kick reason before kicking.",
        category = "Auto Kick",
        subcategory = "General",
    )
    override var partyExplainEnabled: Boolean = true

    @Slider(
        name = "Required S+ PB (seconds)",
        description = "Kick if S+ PB slower than this (60s-600s).",
        min = 60F,
        max = 600F,
        category = "Auto Kick",
        subcategory = "Requirements",
    )
    override var requiredPBSeconds: Float = 210F

    @Text(
        name = "Required secrets (number or Nk)",
        description = "Kick if total secrets lower than this. Example: 25k or 25000",
        size = 1,
        category = "Auto Kick",
        subcategory = "Requirements",
    )
    override var requiredSecrets: String = ""

    @Dropdown(
        name = "Floor Preset",
        options = [
            "F7 S+ (Catacombs)",
            "M4 S+",
            "M5 S+",
            "M6 S+",
            "M7 S+",
        ],
        category = "Auto Kick",
        subcategory = "Requirements",
    )
    override var selectedFloor: Int = 0
}
