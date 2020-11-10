package com.github.emulio.service.scrapers.tgdb_old

import com.github.emulio.exception.GamesScraperException
import com.github.emulio.service.scrapers.GameScraperService
import com.github.emulio.service.scrapers.tgdb_old.model.*
import khttp.get
import khttp.responses.Response
import mu.KotlinLogging
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.*

@Deprecated("this class is being deprecated and should not be used")
object TGDBGameScraperService : GameScraperService {

    private const val baseUrl = "https://api.thegamesdb.net/v1"

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
                url = "$baseUrl/Games/ByGameID",
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

    fun getGamesByPlatformID(platformId: Int,
                             fields: GameFields = GameFields(),
                             include: GameInclude = GameInclude(),
                             page: Int = 0): List<Game> {
        return tgdbGet(
                url = "$baseUrl/Games/ByPlatformID",
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

    fun getGamesByID(gameId: Int,
                     fields: GameFields = GameFields(),
                     include: GameInclude = GameInclude(),
                     page: Int): List<Game> {
        return tgdbGet(
                url = "$baseUrl/Games/ByGameID",
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

    fun getPlatformsByID(platformId: Int,
                         fields: PlatformFields = PlatformFields()): List<Platform> {
        return tgdbGet(
                url = "$baseUrl/Platforms/ByPlatformID",
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
                else -> throw GamesScraperException("unsupported type of object $jsonData")
            }

            list.map { platform ->
                converter(platform)
            }
        } catch (ex: Exception) {
            throw GamesScraperException("There was a problem obtaining list", ex)
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
            else -> throw GamesScraperException("platforms is in a unsupported type of object")
        }

        return list.map(::platformConverter)
    }

    private fun gameConverter(jsonObject: JSONObject): Game {

        val id = jsonObject.getInt("id")
        val gameTitle = jsonObject.getInt("game_title")
        val releaseDate = jsonObject.getInt("release_date")



        return Game(
                id = id

        )
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

    override fun findGames(gameName: String, platformName: String): List<com.github.emulio.model.gamelist.Game> {
        val platforms = getPlatformsByName(platformName, PlatformFields(
                console = true,
                developer = true,
                overview = true
        ))

        if (platforms.isEmpty()) {
            return emptyList()
        }

        val games = getGamesByName(gameName, GameFields(
                players = true,
                publishers = true,
                genres = true,
                last_updated = true,
                rating = true,
                platform = true,
                coop = true,
                os = true,
                processor = true,
                ram = true,
                hdd = true,
                video = true,
                sound = true,
                overview = true,
                youtube = true
        ), GameInclude(
                boxart = true,
                publishers = true
        ), platforms.map { it.id },
        page = 1)

        return games.map {
            com.github.emulio.model.gamelist.Game(
            )
        }

    }
}

fun main() {
//        println(key)
//        println(getPublishers())
//        println(getDevelopers())
//        println(getGenres())
//        println(getPlatforms().map { it.id to it.name })
//        println(getPlatformsByID(-1))
//        println(getPlatformsByID(4970))
//        println(getPlatformsByID(4970, PlatformFields(youtube = true, console = true)))
//        println(getPlatformsByID(4971, PlatformFields(icon = true, console = true)))
//        println(getPlatformsByName("Nintendo Switch"))
//        println(getPlatformsByName("Switch"))
//        println(getPlatformsByName("A"))
//        println(getPlatformsByName("Takeda"))
//    println(TGDBGameScraperService.getPlatforms())
//    println(TGDBGameScraperService.getGamesByPlatformID(20, page = 20))
//
    println(String(Base64.getDecoder().decode("==QZmNWZyIDM1kjMkZzM4IGMzQWYlNzM0UTNiNDZyUjNhVzN2YTMxkDOlhDMhNTM5MGZ1IzYkZWZxMjZmNDN0gDZ".reversed()), Charset.forName("UTF-8")))
}

