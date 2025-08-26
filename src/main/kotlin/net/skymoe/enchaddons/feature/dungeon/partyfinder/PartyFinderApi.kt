package net.skymoe.enchaddons.feature.dungeon.partyfinder

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request

object PartyFinderApi {
    private val http = OkHttpClient()

    fun requestUUID(name: String): String? {
        // Try Mojang first
        runCatching {
            val req = Request.Builder().url("https://api.mojang.com/users/profiles/minecraft/$name").build()
            http.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: return@use
                    val obj = JsonParser().parse(body).asJsonObject
                    obj.getAsJsonPrimitive("id")?.asString?.let { return it }
                }
            }
        }

        // Fallback to Ashcon
        runCatching {
            val req = Request.Builder().url("https://api.ashcon.app/mojang/v2/user/$name").build()
            http.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: return@use
                    val obj = JsonParser().parse(body).asJsonObject
                    obj.getAsJsonPrimitive("id")?.asString?.let { return it.replace("-", "") }
                }
            }
        }
        return null
    }

    fun requestStats(uuid: String): StatsResponse? {
        val url = "https://sbd.evankhell.workers.dev/player/$uuid"
        val req = Request.Builder().url(url).build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val body = resp.body?.string() ?: return null
            val root = JsonParser().parse(body).asJsonObject
            if (root.getAsJsonPrimitive("success")?.asBoolean != true) return null
            val dungeons = root.getAsJsonObject("dungeons") ?: return null

            val pbObj = dungeons.getAsJsonObject("pb") ?: JsonObject()
            val catacombsPB = parseFloorPB(pbObj.getAsJsonObject("catacombs"))
            val masterPB = parseFloorPB(pbObj.getAsJsonObject("master_catacombs"))

            val cataxp = dungeons.getAsJsonPrimitive("cataxp")?.asDouble ?: 0.0
            val secrets = dungeons.getAsJsonPrimitive("secrets")?.asInt
            val runs = dungeons.getAsJsonPrimitive("runs")?.asInt

            val catalevel = floorCatacombsLevel(cataxp)

            val stats = DungeonsStats(
                catalevel = catalevel,
                secrets = secrets,
                runs = runs,
                pb = mutableMapOf(
                    "catacombs" to catacombsPB,
                    "master_catacombs" to masterPB,
                ),
            )
            val respObj = StatsResponse(stats)
            stats.updateSecretAverage()
            return respObj
        }
    }

    private fun parseFloorPB(obj: JsonObject?): MutableMap<Int, MutableMap<String, String>> {
        val result = mutableMapOf<Int, MutableMap<String, String>>()
        if (obj == null) return result
        for ((k, v) in obj.entrySet()) {
            val floor = k.toIntOrNull() ?: continue
            val valueObj = v.asJsonObject
            val map = mutableMapOf<String, String>()
            valueObj.entrySet().forEach { (kk, vv) ->
                if (vv is JsonPrimitive && vv.isString) {
                    map[kk] = vv.asString
                } else if (vv.isJsonPrimitive) {
                    map[kk] = vv.asJsonPrimitive.toString()
                } else if (vv.isJsonNull) {
                    // skip
                } else {
                    map[kk] = vv.toString()
                }
            }
            result[floor] = map
        }
        return result
    }
}

