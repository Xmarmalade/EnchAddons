package net.skymoe.enchaddons.feature.dungeon.partyfinder

class PartyMember(
    val name: String,
) {
    @Volatile var uuid: String? = null
    @Volatile var inFlight: Boolean = false
    @Volatile private var changed: Boolean = false

    var dungeons: DungeonsStats = DungeonsStats()

    fun updateSecretAverage() = dungeons.updateSecretAverage()
    fun markChanged() { changed = true }
    fun hasChanged(): Boolean {
        val v = changed
        changed = false
        return v
    }
}

data class StatsResponse(
    val dungeons: DungeonsStats,
)

class DungeonsStats(
    var catalevel: Int? = null,
    var secrets: Int? = null,
    var runs: Int? = null,
    val pb: MutableMap<String, MutableMap<Int, MutableMap<String, String>>> =
        mutableMapOf(
            "catacombs" to mutableMapOf(),
            "master_catacombs" to mutableMapOf(),
        ),
) {
    var secretAverage: String? = null

    fun updateSecretAverage() {
        val s = secrets
        val r = runs
        if (s != null && r != null && r > 0) {
            secretAverage = String.format("%.1f", s.toDouble() / r.toDouble())
        }
    }
}

