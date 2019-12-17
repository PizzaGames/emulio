package com.github.emulio.gamelist

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.io.xml.DomDriver
import com.thoughtworks.xstream.io.xml.StaxDriver
import java.io.File
import java.io.FileWriter

/**
 * Reference page for implementation of GameList reader/writer
 * https://github.com/RetroPie/EmulationStation/blob/master/GAMELISTS.md
 */
object GamelistUpdaterService {

    private val xstreamStax by lazy { XStream().apply { configureXstream(this) } }

    private fun configureXstream(xStream: XStream) {
        xStream.alias("game", Game::class.java)
        xStream.alias("gameList", GameList::class.java)
        xStream.alias("folder", Folder::class.java)

        xStream.addImplicitArray(GameList::class.java, "games", Game::class.java)
        xStream.addImplicitArray(Folder::class.java, "games", Game::class.java)
        xStream.addImplicitArray(GameList::class.java, "folders", Folder::class.java)

        xStream.setMode(XStream.NO_REFERENCES)
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