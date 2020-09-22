package com.github.emulio.scrapers

import com.github.emulio.scrapers.tgdb.model.*
import org.json.JSONObject

interface Scrapper {

    fun getGamesUpdates(gameId: Int, time: Int?, page: Int): List<Game>
    fun getGamesImages(gameId: Int, filter: GameImageFields = GameImageFields(), page: Int): List<Game>
    fun getGamesByPlatformId(platformId: Int,
                             fields: GameFields = GameFields(),
                             include: GameInclude = GameInclude(),
                             page: Int = 0): List<Game>
    fun getGamesByName(name: String,
                       fields: GameFields = GameFields(),
                       include: GameInclude = GameInclude(),
                       filterPlatformIds: List<Int> = emptyList(),
                       page: Int): List<Game>
    fun getGamesById(gameId: Int,
                     fields: GameFields = GameFields(),
                     include: GameInclude = GameInclude(),
                     page: Int): List<Game>
    fun getPlatformsImages(platformId: Int,
                           fields: PlatformImageFields = PlatformImageFields()): List<Platform>
    fun getPlatformsByName(platformName: String,
                           fields: PlatformFields = PlatformFields()): List<Platform>
    fun getPlatformsById(platformId: Int,
                         fields: PlatformFields = PlatformFields()): List<Platform>

    fun getPlatforms(): List<Platform>
    fun getGenres(): List<Genre>
    fun getDevelopers(): List<Developer>
    fun getPublishers(): List<Publisher>

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

fun JSONObject.getNullableInt(key: String): Int? {
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

fun JSONObject.getNullableString(key: String): String? {
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