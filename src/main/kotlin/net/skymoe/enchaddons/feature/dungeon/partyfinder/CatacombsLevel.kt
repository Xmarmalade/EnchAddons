package net.skymoe.enchaddons.feature.dungeon.partyfinder

// Catacombs progression and level calc
internal val CATACOMBS: DoubleArray = doubleArrayOf(
    0.0,
    50.0,
    125.0,
    235.0,
    395.0,
    625.0,
    955.0,
    1425.0,
    2095.0,
    3045.0,
    4385.0,
    6275.0,
    8940.0,
    12700.0,
    17960.0,
    25340.0,
    35640.0,
    50040.0,
    70040.0,
    97640.0,
    135640.0,
    188140.0,
    259640.0,
    356640.0,
    488640.0,
    668640.0,
    911640.0,
    1239640.0,
    1684640.0,
    2284640.0,
    3084640.0,
    4149640.0,
    5559640.0,
    7459640.0,
    9959640.0,
    13259640.0,
    17559640.0,
    23159640.0,
    30359640.0,
    39559640.0,
    51559640.0,
    66559640.0,
    85559640.0,
    109559640.0,
    139559640.0,
    177559640.0,
    225559640.0,
    285559640.0,
    360559640.0,
    453559640.0,
    569809640.0,
)

internal fun calcCatacombsLevel(xp: Double?): Double? {
    if (xp == null || xp <= 0.0) return 0.0
    val progression = CATACOMBS
    val maxLevel = 50

    fun getLevel(): Double {
        if (xp > progression[maxLevel]) {
            var lvl = kotlin.math.floor((50 + (xp - progression[50]) / 200000000.0) * 100.0) / 100.0
            if (lvl > 100) lvl = 100.0
            return lvl
        }
        val level = progression.indexOfLast { it < xp }
        if (level <= 0) return 0.0
        val value = (level - 1 + (xp - progression[level - 1]) / (progression[level] - progression[level - 1]))
        return kotlin.math.floor(value * 100.0) / 100.0
    }
    return getLevel()
}

internal fun floorCatacombsLevel(xp: Double): Int? {
    val lvl = calcCatacombsLevel(xp)
    return if (lvl != null) kotlin.math.floor(lvl).toInt() else null
}

