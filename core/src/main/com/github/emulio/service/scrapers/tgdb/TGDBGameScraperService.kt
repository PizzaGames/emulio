package com.github.emulio.service.scrapers.tgdb

import com.github.emulio.exception.GamesScraperException
import com.github.emulio.model.gamelist.Game
import com.github.emulio.service.scrapers.GameScraperService
import khttp.get
import mu.KotlinLogging
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * For more documentation and swagger access check: https://api.thegamesdb.net/
 */
object TGDBGameScraperService : GameScraperService {

    private val logger = KotlinLogging.logger { }

    private const val baseUrl = "https://api.thegamesdb.net/v1"

    private val key by lazy {
        String(
            Base64
                .getDecoder()
                .decode("==QZmNWZyIDM1kjMkZzM4IGMzQWYlNzM0UTNiNDZyUjNhVzN2YTMxkDOlhDMhNTM5MGZ1IzYkZWZxMjZmNDN0gDZ"
                        .reversed()
            ),
            Charset.forName("UTF-8")
        )
    }

    private fun findPlatformsByName(platformName: String): List<JSONObject> {
        val fields = "icon,console,controller,developer,manufacturer,media,cpu," +
                "memory,graphics,sound,maxcontrollers,display,overview,youtube"

        return tgdbGet(
            url = "${baseUrl}/Platforms/ByPlatformName",
            params = mapOf("apikey" to key,
                           "name" to platformName,
                           "fields" to fields),
            filterName = "platforms"
        )
    }

    private fun findGamesByName(gameName: String, platformIds: List<Int>): List<JSONObject> {
        val fields = "players,publishers,genres,overview,last_updated,rating,platform,coop,youtube,os,processor,ram,hdd,video,sound,alternates"

        return tgdbGet(
                url = "${baseUrl}.1/Games/ByGameName",
                params = mapOf("apikey" to key,
                        "name" to gameName,
                        "platforms" to platformIds.joinToString(),
                        "fields" to fields,
                        "include" to "boxart,platform"),
                filterName = "games"
        )
    }

    private fun tgdbGet(
            url: String,
            params: Map<String, String>,
            filterName: String): List<JSONObject> {

        return try {
            fetch(url, params, filterName)
        } catch (ex: Exception) {
            throw GamesScraperException("There was a problem obtaining list", ex)
        }
    }

    private fun fetch(url: String, params: Map<String, String>, filterName: String): List<JSONObject> {
        logger.info { "requesting url: $url; $params" }
        val response = get(url, params = params)
        logger.debug { "url: $url; responseData: ${response.text};" }

        val responseJson = response.jsonObject

        return if (responseJson.has("data")) {
            readData(responseJson, filterName)
        } else {
            emptyList()
        }
    }

    private fun readData(responseJson: JSONObject, filterName: String): List<JSONObject> {
        val data = responseJson["data"] as JSONObject

        return if (data.getInt("count") == 0) {
            emptyList()
        } else {
            val list: List<JSONObject> = when (val jsonData = data[filterName]) {
                is JSONArray -> jsonData.toList().map { it as JSONObject }
                is JSONObject -> jsonData.keySet().map { jsonData[it] as JSONObject }
                else -> throw GamesScraperException("unsupported type of object $jsonData")
            }
            list
        }
    }

    override fun findGames(gameName: String, platformName: String): List<Game> {
        val platforms = findPlatformsByName(platformName)

        val games = findGamesByName(gameName, platforms.map { it.getInt("id") })

        return games.map { json ->
            Game(name = json.getString("game_title"),
                    desc = json.getString("overview"),
                    image = getImageFromGame(json),
                    thumbnail = getThumbnailFromGame(json),
                    video = getVideoFromGame(json),
                    rating = 0f,
                    releasedate = getDate(json),
                    developer = getDeveloper(json),
                    publisher = getPublisher(json),
                    genre = getGenre(json),
                    players = json.getInt("players"),
                    playcount = 0,
                    lastplayed = null
            )
        }
    }

    private fun getGenre(json: JSONObject): String? {
        TODO("Not yet implemented")
    }

    private fun getPublisher(json: JSONObject): String? {
        TODO("Not yet implemented")
    }

    private fun getDeveloper(json: JSONObject): String? {
        TODO("Not yet implemented")
    }

    private fun getDate(json: JSONObject) = Date.from(
        LocalDateTime
            .parse(json.getString("release_date"), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .atZone(ZoneId.systemDefault()).toInstant())

    private fun getVideoFromGame(json: JSONObject): String? {
        TODO("Not yet implemented")
    }

    private fun getThumbnailFromGame(json: JSONObject): String? {
        TODO("Not yet implemented")
    }

    private fun getImageFromGame(json: JSONObject): String? {
        return "- not implemented yet -"
    }
}

fun main() {
    print(TGDBGameScraperService.findGames("Burnout", "Playstation"))
}
