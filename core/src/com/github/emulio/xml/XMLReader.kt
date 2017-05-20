package com.github.emulio.xml

import com.github.emulio.model.Game
import java.io.File
import javax.xml.parsers.SAXParserFactory


class XMLReader {
    fun parseGameList(xmlFile: File, baseDir: File): List<Game> {

        val factory = SAXParserFactory.newInstance()
        val saxParser = factory.newSAXParser()

        val games = mutableListOf<Game>()

        saxParser.parse(xmlFile, GameInfoSAXHandler(games, baseDir))

        return games
    }
}
