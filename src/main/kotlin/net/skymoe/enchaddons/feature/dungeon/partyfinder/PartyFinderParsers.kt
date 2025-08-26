package net.skymoe.enchaddons.feature.dungeon.partyfinder

import java.util.regex.Pattern

internal val PARTY_MEMBER_LINE: Pattern = Pattern.compile("§5§o §.[^§]+§f: §.\u002B.*")
internal val USERNAME_REGEX: Pattern = Pattern.compile("§5§o §.([^§]+)§f:")
internal val CLASS_FROM_LINE: Pattern = Pattern.compile("§5§o §\\w\\w+§f: §\\w(\\w+)§\\w \\(§e\\d+§b\\)")
internal val MASTER_DUNGEON_REGEX: Pattern = Pattern.compile("§5§o§7Dungeon: §bMaster Mode(?: The)* Catacombs")

internal fun decodeNumeral(s: String): Int =
    when (s.trim().uppercase()) {
        "I" -> 1
        "II" -> 2
        "III" -> 3
        "IV" -> 4
        "V" -> 5
        "VI" -> 6
        "VII" -> 7
        else -> 0
    }

internal fun getFloor(lore: List<String>): Int {
    val floorLine = lore.firstOrNull { it.contains("§7Floor: §bFloor ") }
    if (floorLine != null) {
        val token = floorLine.split(" ").lastOrNull() ?: return 0
        return token.toIntOrNull() ?: decodeNumeral(token)
    }
    return 0
}

internal fun getDungeonType(lore: List<String>): String =
    if (lore.any { MASTER_DUNGEON_REGEX.matcher(it).find() }) "master_catacombs" else "catacombs"

internal fun getUsername(msg: String): String? {
    val m = USERNAME_REGEX.matcher(msg)
    return if (m.find()) m.group(1) else null
}

internal fun getMissingClasses(lore: List<String>): List<String> {
    val classes = mutableListOf("Archer", "Berserk", "Mage", "Tank", "Healer")
    lore.forEach { line ->
        val m = CLASS_FROM_LINE.matcher(line)
        if (m.find()) {
            val cls = m.group(1)
            classes.remove(cls)
        }
    }
    return classes
}
