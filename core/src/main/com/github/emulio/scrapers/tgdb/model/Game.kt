package com.github.emulio.scrapers.tgdb.model

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