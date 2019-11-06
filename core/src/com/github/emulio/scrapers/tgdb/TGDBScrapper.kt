package com.github.emulio.scrapers.tgdb

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

object TGDBScrapper {

    private const val baseUrl = "https://api.thegamesdb.net"

    val logger = KotlinLogging.logger { }

    fun getGamesUpdates(gameId: Int,
                     time: Int?,
                     page: Int): List<Game> {


        val params = if (time != null) {
            mapOf(
                "apikey" to key,
                "id" to gameId.toString(),
                "time" to time.toString(), // TODO time can be null
                "page" to page.toString())
        } else {
            mapOf(
                "apikey" to key,
                "id" to gameId.toString(),
                "page" to page.toString())
        }

        return tgdbGet(
                url = "$baseUrl/Games/ByGameId",
                params = params,
                filterName = "games",
                converter = ::gameConverter
        )
    }

    fun getGamesImages(gameId: Int,
                       filter: GameImageFields = GameImageFields(),
                       page: Int): List<Game> {
        return tgdbGet(
                url = "$baseUrl/Games/Images",
                params = mapOf(
                        "apikey" to key,
                        "id" to gameId.toString(),
                        "filter" to filter.fields(),
                        "page" to page.toString()),
                filterName = "games",
                converter = ::gameConverter
        )
    }

    fun getGamesByPlatformId(platformId: Int,
                     fields: GameFields = GameFields(),
                     include: GameInclude = GameInclude(),
                     page: Int = 0): List<Game> {
        return tgdbGet(
                url = "$baseUrl/Games/ByPlatformId",
                params = mapOf(
                        "apikey" to key,
                        "id" to platformId.toString(),
                        "fields" to fields.fields(),
                        "include" to include.fields(),
                        "page" to page.toString()),
                filterName = "games",
                converter = ::gameConverter
        )
    }

    fun getGamesByName(name: String,
                     fields: GameFields = GameFields(),
                     include: GameInclude = GameInclude(),
                     filterPlatformIds: List<Int> = emptyList(),
                     page: Int): List<Game> {
        return tgdbGet(
                url = "$baseUrl/Games/ByGameName",
                params = mapOf(
                        "apikey" to key,
                        "id" to name,
                        "fields" to fields.fields(),
                        "include" to include.fields(),
                        "filter" to filterPlatformIds.joinToString(separator = ","),
                        "page" to page.toString()),
                filterName = "games",
                converter = ::gameConverter
        )
    }

    fun getGamesById(gameId: Int,
                     fields: GameFields = GameFields(),
                     include: GameInclude = GameInclude(),
                     page: Int): List<Game> {
        return tgdbGet(
                url = "$baseUrl/Games/ByGameId",
                params = mapOf(
                        "apikey" to key,
                        "id" to gameId.toString(),
                        "fields" to fields.fields(),
                        "include" to include.fields(),
                        "page" to page.toString()),
                filterName = "games",
                converter = ::gameConverter
        )
    }

    fun getPlatformsImages(platformId: Int,
                           fields: PlatformImageFields = PlatformImageFields()): List<Platform> {
        return tgdbGet(
                url = "$baseUrl/Platforms/Images",
                params = mapOf(
                        "apikey" to key,
                        "platforms_id" to platformId.toString(),
                        "fields" to fields.fields()),
                filterName = "platforms",
                converter = ::platformConverter
        )
    }

    fun getPlatformsByName(platformName: String,
                           fields: PlatformFields = PlatformFields()): List<Platform> {
        return tgdbGet(
                url = "$baseUrl/Platforms/ByPlatformName",
                params = mapOf(
                        "apikey" to key,
                        "name" to platformName,
                        "fields" to fields.fields()),
                filterName = "platforms",
                converter = ::platformConverter
        )
    }

    fun getPlatformsById(platformId: Int,
                         fields: PlatformFields = PlatformFields()): List<Platform> {
        return tgdbGet(
                url = "$baseUrl/Platforms/ByPlatformId",
                params = mapOf(
                        "apikey" to key,
                        "id" to platformId.toString(),
                        "fields" to fields.fields()),
                filterName = "platforms",
                converter = ::platformConverter
        )
    }

    fun getPlatforms(): List<Platform> {
        return tgdbGet(
                url = "$baseUrl/Platforms",
                filterName = "platforms",
                converter = ::platformConverter
        )
    }

    private fun <T> tgdbGet(url: String,
                            params: Map<String, String> = mapOf("apikey" to key),
                            converter: (JSONObject) -> T,
                            filterName: String): List<T> {
        return try {
            logger.info { "requesting url: $url; $params" }
            val response = get(url, params = params)
            logger.debug { "url: $url; responseData: ${response.text};"}

            val responseJson = response.jsonObject

            if (!responseJson.has("data")) {
                return emptyList()
            }

            val data = responseJson["data"] as JSONObject

            if (data.getInt("count") == 0) {
                return emptyList()
            }

            val list: List<JSONObject> = when (val jsonData = data[filterName]) {
                is JSONArray -> jsonData.toList().map { it as JSONObject }
                is JSONObject -> jsonData.keySet().map { jsonData[it] as JSONObject }
                else -> throw ScrapperException("unsupported type of object $jsonData")
            }

            list.map { platform ->
                converter(platform)
            }
        } catch (ex: Exception) {
            throw ScrapperException("There was a problem obtaining list", ex)
        }
    }

    fun getGenres(): List<Genre> {
        return tgdbGet(
            url = "$baseUrl/Genres",
            filterName = "genres",
            converter = { genre ->
                Genre(name = genre.getString("name"),
                      id = genre.getInt("id"))
            }
        )
    }

    fun getDevelopers(): List<Developer> {
        return tgdbGet(
            url = "$baseUrl/Developers",
            filterName = "developers",
            converter = { developer ->
                Developer(
                        name = developer.getString("name"),
                        id = developer.getInt("id")
                )
            }
        )
    }

    fun getPublishers(): List<Publisher> {
        return tgdbGet(
            url = "$baseUrl/Publishers",
            filterName = "publishers",
            converter = { publisher ->
                Publisher(
                        name = publisher.getString("name"),
                        id = publisher.getInt("id")
                )
            }
        )
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

        return list.map(::platformConverter)
    }

    private fun gameConverter(jsonObject: JSONObject): Game {
        return Game(id = jsonObject.getInt("id"))
    }

    private fun platformConverter(platform: JSONObject): Platform {
        return Platform(
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
            id = platform.getInt("id"))
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
//        println(getPlatformsByName("Takeda"))

        println(getPlatforms())
        println(getGamesByPlatformId(10))
    }
}

class GameInclude(private val boxart: Boolean = false,
                  private val publishers: Boolean = false) {
    fun fields(): String {
        return StringBuilder().apply {
            append(if (boxart) "icon," else "")
            append(if (publishers) "console," else "")
            if (length > 0) {
                setLength(length - 1)
            }

        }.toString()
    }
}

class GameFields(private val players: Boolean = false,
                 private val publishers: Boolean = false,
                 private val genres: Boolean = false,
                 private val last_updated: Boolean = false,
                 private val rating: Boolean = false,
                 private val platform: Boolean = false,
                 private val coop: Boolean = false,
                 private val os: Boolean = false,
                 private val processor: Boolean = false,
                 private val ram: Boolean = false,
                 private val hdd: Boolean = false,
                 private val video: Boolean = false,
                 private val sound: Boolean = false,
                 private val overview: Boolean = false,
                 private val youtube: Boolean = false) {
    fun fields(): String {
        return StringBuilder().apply {
            append(if (players) "icon," else "")
            append(if (publishers) "console," else "")
            append(if (genres) "controller," else "")
            append(if (last_updated) "developer," else "")
            append(if (rating) "manufacturer," else "")
            append(if (platform) "media," else "")
            append(if (coop) "cpu," else "")
            append(if (os) "memory," else "")
            append(if (processor) "graphics," else "")
            append(if (ram) "sound," else "")
            append(if (hdd) "maxcontrollers," else "")
            append(if (video) "display," else "")
            append(if (sound) "display," else "")
            append(if (overview) "overview," else "")
            append(if (youtube) "youtube," else "")
            if (length > 0) {
                setLength(length - 1)
            }

        }.toString()
    }
}

class GameImageFields(private val fanart: Boolean = false,
                      private val banner: Boolean = false,
                      private val boxart: Boolean = false,
                      private val screenshot: Boolean = false,
                      private val clearlogo: Boolean = false) {
    fun fields(): String {
        return StringBuilder().apply {
            append(if (fanart) "icon," else "")
            append(if (banner) "console," else "")
            append(if (boxart) "controller," else "")
            append(if (screenshot) "developer," else "")
            append(if (clearlogo) "manufacturer," else "")
            if (length > 0) {
                setLength(length - 1)
            }

        }.toString()
    }
}

class PlatformImageFields(
        private val fanart: Boolean = false,
        private val banner: Boolean = false,
        private val boxart: Boolean = false) {
    fun fields(): String {
        return StringBuilder().apply {
            append(if (fanart) "fanart," else "")
            append(if (banner) "banner," else "")
            append(if (boxart) "boxart," else "")
            if (length > 0) {
                setLength(length - 1)
            }

        }.toString()
    }
}

class PlatformFields(private val icon: Boolean = false,
                     private val console: Boolean = false,
                     private val controller: Boolean = false,
                     private val developer: Boolean = false,
                     private val manufacturer: Boolean = false,
                     private val media: Boolean = false,
                     private val cpu: Boolean = false,
                     private val memory: Boolean = false,
                     private val graphics: Boolean = false,
                     private val sound: Boolean = false,
                     private val maxcontrollers: Boolean = false,
                     private val display: Boolean = false,
                     private val overview: Boolean = false,
                     private val youtube: Boolean = false) {
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

data class Game(
        val id: Int,
        val players: Int? = null,
        val publishers: String? = null,
        val genres: String? = null,
        val last_updated: String? = null,
        val rating: String? = null,
        val platform: String? = null,
        val coop: String? = null,
        val os: String? = null,
        val processor: String? = null,
        val ram: String? = null,
        val hdd: String? = null,
        val video: String? = null,
        val sound: String? = null,
        val overview: String? = null,
        val youtube: String? = null
)

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