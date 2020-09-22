package com.github.emulio.scrapers

import com.github.emulio.scrapers.igdb.IGDBScrapper
import com.github.emulio.scrapers.rawg.RAWGScrapper
import com.github.emulio.scrapers.tgdb.*
import com.github.emulio.scrapers.tgdb.model.*

object ScrapperService : Scrapper {

    val scrapers = setOf(TGDBScrapper, IGDBScrapper, RAWGScrapper)

    override fun getGamesUpdates(gameId: Int, time: Int?, page: Int): List<Game> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGamesImages(gameId: Int, filter: GameImageFields, page: Int): List<Game> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGamesByPlatformId(platformId: Int, fields: GameFields, include: GameInclude, page: Int): List<Game> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGamesByName(name: String, fields: GameFields, include: GameInclude, filterPlatformIds: List<Int>, page: Int): List<Game> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGamesById(gameId: Int, fields: GameFields, include: GameInclude, page: Int): List<Game> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPlatformsImages(platformId: Int, fields: PlatformImageFields): List<Platform> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPlatformsByName(platformName: String, fields: PlatformFields): List<Platform> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPlatformsById(platformId: Int, fields: PlatformFields): List<Platform> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPlatforms(): List<Platform> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGenres(): List<Genre> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDevelopers(): List<Developer> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPublishers(): List<Publisher> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }




}