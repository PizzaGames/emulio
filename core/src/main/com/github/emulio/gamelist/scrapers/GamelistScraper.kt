package com.github.emulio.gamelist.scrapers

import com.github.emulio.gamelist.GameListParser
import com.github.emulio.gamelist.model.Game
import com.github.emulio.gamelist.scrapers.tgdb.TGDBScrapper
import com.github.emulio.gamelist.scrapers.tgdb.model.GameFields
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URL

val logger = KotlinLogging.logger { }

object GamelistScraper {

    fun findGames(gameName: String, platformName: String): List<Game> {
        val platforms = TGDBScrapper.getPlatforms()
                .filter { it.name.contains(platformName) }

        if (platforms.isEmpty()) {
            return emptyList()
        }

        if (platforms.size > 1) {
            return emptyList()
        }

        val games = TGDBScrapper.getGamesByName(
                name = gameName,
                fields = GameFields(players = true),
                filterPlatformIds = platforms.map { it.id },
                page = 20
        )

        return emptyList()
    }

    fun saveGame(game: Game, gamelistXmlFile: File) {
        val gamelist = GameListParser.readGameList(gamelistXmlFile)

        game.fetchGameFiles(gamelistXmlFile)

        val newGameList = gamelist.copy(
            games = gamelist.games + game
        )

        GameListParser.writeGameList(newGameList, gamelistXmlFile)
    }

    fun saveGames(games: List<Game>, gamelistXmlFile: File) {
        val gamelist = GameListParser.readGameList(gamelistXmlFile)

        games.forEach {
            it.fetchGameFiles(gamelistXmlFile)
        }

        val newGameList = gamelist.copy(
                games = gamelist.games + games
        )

        GameListParser.writeGameList(newGameList, gamelistXmlFile)
    }

    private fun Game.fetchGameFiles(gamelistXmlFile: File) {
        val parentFolder = gamelistXmlFile.parentFile
        val mediaFolder = File(parentFolder, "media").apply { mkdirs() }

        val gameName = this.name!!

        downloadImage(gameName, this.image, mediaFolder)
        downloadImage(gameName, this.thumbnail, mediaFolder)
    }

    private fun downloadImage(gameName: String, url: String?, mediaFolder: File) {
        if (url == null) {
            return
        }

        val hex = "%x".format(System.currentTimeMillis())

        FileUtils.copyURLToFile(URL(url), File(mediaFolder, "$gameName.$hex.${findImageExtension(url)}"))
    }

    private fun findImageExtension(image: String): String {
        if (image.lastIndexOf(".") == -1) {
            return "png"
        }
        return image.substringAfterLast(".")
    }


}

fun main() {
    val games = GamelistScraper.findGames("Burnout", "Playstation Portable")

    logger.info { "games: $games" }
}



