package com.github.emulio.service.scrapers

import com.github.emulio.model.gamelist.Game

interface GameScraperService {

    fun findGames(gameName: String, platformName: String): List<Game>

}