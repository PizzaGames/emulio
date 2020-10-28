package com.github.emulio.gamelist

import com.thoughtworks.xstream.XStream
import java.io.File
import java.io.FileWriter

/**
 * Reference page for implementation of GameList reader/writer
 * https://github.com/RetroPie/EmulationStation/blob/master/GAMELISTS.md
 */
object GamelistUpdaterService {

    private val xstreamStax by lazy { XStream().apply { this.configureXstream() } }

    private fun XStream.configureXstream() {
        alias("game", Game::class.java)
        alias("gameList", GameList::class.java)
        alias("folder", Folder::class.java)

        addImplicitArray(GameList::class.java, "games", Game::class.java)
        addImplicitArray(Folder::class.java, "games", Game::class.java)
        addImplicitArray(GameList::class.java, "folders", Folder::class.java)

        setMode(XStream.NO_REFERENCES)
    }

    fun readGameList(xmlFile: File): GameList {
        return xstreamStax.fromXML(xmlFile) as GameList
    }

    fun writeGameList(gameList: GameList, outputXmlFile: File) {
        return FileWriter(outputXmlFile).use {
            xstreamStax.toXML(gameList, it)
        }
    }
}