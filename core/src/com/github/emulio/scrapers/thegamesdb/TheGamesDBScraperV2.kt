package com.github.emulio.scrapers.thegamesdb

import com.github.emulio.exceptions.ScrapperException
import khttp.get
import khttp.responses.Response
import mu.KotlinLogging
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.util.*

object TheGamesDBScraperV2 {

    private const val baseUrl = "https://api.thegamesdb.net"

    val logger = KotlinLogging.logger { }

    fun getPlatformsByName(platformName: String,
                           fields: PlatformFields = PlatformFields()): List<Platform> {
        try {
            val response = get("$baseUrl/Platforms/ByPlatformName", params = mapOf(
                    "apikey" to key,
                    "name" to platformName,
                    "fields" to fields.fields()))

            return convertPlatforms(response)
        } catch (ex: Exception) {
            throw ScrapperException("There was a problem obtaining platforms list", ex)
        }
    }

    fun getPlatformsById(platformId: Int,
                         fields: PlatformFields = PlatformFields()): List<Platform> {
        try {
            val response = get("$baseUrl/Platforms/ByPlatformID", params = mapOf(
                    "apikey" to key,
                    "id" to platformId.toString(),
                    "fields" to fields.fields()))

            return convertPlatforms(response)
        } catch (ex: Exception) {
            throw ScrapperException("There was a problem obtaining platforms list", ex)
        }
    }

    fun getPlatforms(): List<Platform> {
        try {
            val response = get("$baseUrl/Platforms", params = mapOf("apikey" to key))

            return convertPlatforms(response)
        } catch (ex: Exception) {
            throw ScrapperException("There was a problem obtaining platforms list", ex)
        }
    }

    fun getGenres(): List<Genre> {
        try {
            val response = get("$baseUrl/Genres", params = mapOf("apikey" to key))

            val data = response.jsonObject["data"] as JSONObject
            val genres = data["genres"] as JSONObject

            return genres.keySet().map { key ->
                val genre = genres[key] as JSONObject

                Genre(
                        name = genre.getString("name"),
                        id = genre.getInt("id")
                )
            }
        } catch (ex: Exception) {
            throw ScrapperException("There was a problem obtaining genres list", ex)
        }
    }

    fun getDevelopers(): List<Developer> {
        try {
            val response = get("$baseUrl/Developers", params = mapOf("apikey" to key))

            val data = response.jsonObject["data"] as JSONObject
            val developers = data["developers"] as JSONObject

            return developers.keySet().map { key ->
                val developer = developers[key] as JSONObject

                Developer(
                        name = developer.getString("name"),
                        id = developer.getInt("id")
                )
            }
        } catch (ex: Exception) {
            throw ScrapperException("There was a problem obtaining developers list", ex)
        }
    }

    fun getPublishers(): List<Publisher> {
        try {
            val response = get("$baseUrl/Publishers", params = mapOf("apikey" to key))

            val data = response.jsonObject["data"] as JSONObject
            val publishers = data["publishers"] as JSONObject

            return publishers.keySet().map { key ->
                val publisher = publishers[key] as JSONObject

                Publisher(
                        name = publisher.getString("name"),
                        id = publisher.getInt("id")
                )
            }
        } catch (ex: Exception) {
            throw ScrapperException("There was a problem obtaining publishers list", ex)
        }
    }

    private fun JSONObject.getNullableInt(key: String): Int? {
        return if (this.has(key)) {
            if (this[key] != JSONObject.NULL) {
                this.getInt(key)
            } else {
                null
            }
        } else {
            null
        }
    }

    private fun JSONObject.getNullableString(key: String): String? {
        return if (this.has(key)) {
            if (!this.isNull(key)) {
                this.getString(key)
            } else {
                null
            }
        } else {
            null
        }
    }

    private fun convertPlatforms(response: Response): List<Platform> {
        println("jsonObject: ${response.jsonObject}")
        val data = response.jsonObject["data"] as JSONObject

        if (data.getInt("count") == 0) {
            return emptyList()
        }

        val list: List<JSONObject> = when (val platformsJson = data["platforms"]) {
            is JSONArray -> platformsJson.toList().map { it as JSONObject }
            is JSONObject -> platformsJson.keySet().map { platformsJson[key] as JSONObject }
            else -> throw ScrapperException("platforms is in a unsupported type of object")
        }

        return list.map { platform ->
            Platform(
                    name = platform.getString("name"),
                    alias = platform.getNullableString("alias"),
                    icon = platform.getNullableString("icon"),
                    console = platform.getNullableString("console"),
                    controller = platform.getNullableString("controller"),
                    developer = platform.getNullableString("developer"),
                    manufacturer = platform.getNullableString("manufacturer"),
                    media = platform.getNullableString("media"),
                    cpu = platform.getNullableString("cpu"),
                    memory = platform.getNullableString("memory"),
                    graphics = platform.getNullableString("graphics"),
                    sound = platform.getNullableString("sound"),
                    maxcontrollers = platform.getNullableInt("maxcontrollers"),
                    display = platform.getNullableString("display"),
                    overview = platform.getNullableString("overview"),
                    youtube = platform.getNullableString("youtube"),
                    id = platform.getInt("id")
            )
        }
    }

    private val key =
            String(Base64.getDecoder().decode("==QZmNWZyIDM1kjMkZzM4IGMzQWYlNzM0UTNiNDZyUjNhVzN2YTMxkDOlhDMhNTM5MGZ1IzYkZWZxMjZmNDN0gDZ".reversed()), Charset.forName("UTF-8"))

    @JvmStatic
    fun main(args: Array<String>) {
//        println(key)
//        println(getPublishers())
//        println(getDevelopers())
//        println(getGenres())
//        println(getPlatforms().map { it.id to it.name })
//        println(getPlatformsById(-1))
//        println(getPlatformsById(4970))
//        println(getPlatformsById(4970, PlatformFields(youtube = true, console = true)))
//        println(getPlatformsById(4971, PlatformFields(icon = true, console = true)))
//        println(getPlatformsByName("Nintendo Switch"))
//        println(getPlatformsByName("Switch"))
//        println(getPlatformsByName("A"))
        println(getPlatformsByName("Takeda"))
    }
}

class PlatformFields(val icon: Boolean = false,
                     val console: Boolean = false,
                     val controller: Boolean = false,
                     val developer: Boolean = false,
                     val manufacturer: Boolean = false,
                     val media: Boolean = false,
                     val cpu: Boolean = false,
                     val memory: Boolean = false,
                     val graphics: Boolean = false,
                     val sound: Boolean = false,
                     val maxcontrollers: Boolean = false,
                     val display: Boolean = false,
                     val overview: Boolean = false,
                     val youtube: Boolean = false) {
    fun fields(): String {
        return StringBuilder().apply {
            append(if (icon) "icon," else "")
            append(if (console) "console," else "")
            append(if (controller) "controller," else "")
            append(if (developer) "developer," else "")
            append(if (manufacturer) "manufacturer," else "")
            append(if (media) "media," else "")
            append(if (cpu) "cpu," else "")
            append(if (memory) "memory," else "")
            append(if (graphics) "graphics," else "")
            append(if (sound) "sound," else "")
            append(if (maxcontrollers) "maxcontrollers," else "")
            append(if (display) "display," else "")
            append(if (overview) "overview," else "")
            append(if (youtube) "youtube," else "")
            if (length > 0) {
                setLength(length - 1)
            }

        }.toString()
    }
}

data class Platform(
        val id: Int ,
        val name: String,
        val alias: String?,
        val icon: String?,
        val console: String?,
        val controller: String?,
        val developer: String?,
        val manufacturer: String?,
        val media: String?,
        val cpu: String?,
        val memory: String?,
        val graphics: String?,
        val sound: String?,
        val maxcontrollers: Int?,
        val display: String?,
        val overview: String?,
        val youtube: String?
)

data class Genre(
        val id: Int,
        val name: String
)

data class Developer(
        val id: Int,
        val name: String
)

data class Publisher(
        val name: String,
        val id: Int
)