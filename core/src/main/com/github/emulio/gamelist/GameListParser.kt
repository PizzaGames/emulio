package com.github.emulio.gamelist

import com.github.emulio.gamelist.model.GameList
import java.io.File
import java.io.FileWriter


/**
 * Reference page for implementation of GameList reader/writer
 * https://github.com/RetroPie/EmulationStation/blob/master/GAMELISTS.md
 */
object GameListParser {

    fun readGameList(xmlFile: File): GameList {
        if (!xmlFile.exists()) {
            return GameList()
        }
        return GamelistXStream.stax.fromXML(xmlFile) as GameList
    }

    fun writeGameList(gameList: GameList, outputXmlFile: File) {
        return FileWriter(outputXmlFile).use {
            GamelistXStream.stax.toXML(gameList, it)
        }
    }

}

