package net.skymoe.enchaddons.feature.dungeon.partyfinder

// Suffix helpers: strip previous suffix and build new suffix without touching class level
internal fun hasCustomSuffix(msg: String): Boolean = msg.contains("§0§r§r")

internal fun removeSuffix(msg: String): String = msg.replace(Regex("§0§r§r.*"), "")

internal fun createSuffix(
    msg: String,
    player: PartyMember?,
    floor: Int,
    dungeonType: String,
    cfg: PartyFinderStatsConfig,
): String {
    if (player == null) return msg
    val base = removeSuffix(msg)
    val builder = StringBuilder("§0§r§r")

    if (cfg.partyfinderCata) {
        builder.append(" §b(§6").append(player.dungeons.catalevel ?: "?").append("§b)§r")
    }
    if (cfg.partyfinderSecrets && cfg.partyfinderSecretAverage) {
        builder
            .append(" §8[§a")
            .append(player.dungeons.secrets ?: "?")
            .append("§8/§b")
            .append(player.dungeons.secretAverage ?: "?")
            .append("§8]§r")
    } else {
        if (cfg.partyfinderSecrets) {
            builder.append(" §8[§a").append(player.dungeons.secrets ?: "?").append("§8]§r")
        }
        if (cfg.partyfinderSecretAverage) {
            builder.append(" §8[§b").append(player.dungeons.secretAverage ?: "?").append("§8]§r")
        }
    }
    if (cfg.partyfinderPB) {
        val pb =
            player.dungeons.pb[dungeonType]
                ?.get(floor)
                ?.get("S+") ?: "?"
        builder.append(" §8[§9").append(pb).append("§8]§r")
    }

    return base + builder.toString()
}
