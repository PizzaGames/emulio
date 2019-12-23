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

    /**
     * This function is used to read a xml file on the gamelist.xml format.
     *
     * Note:
     *  This reader is not the same as used to load all the GUI platforms, since that one is based
     *  on the rx.java and is progressive, meanwhile the games are being shown in the UI the xml continues
     *  to be readed in background with a high performance implementation. (com.github.emulio.XMLReader)
     *
     * @see com.github.emulio.xml.XMLReader
     *
     */
    fun readGameList(xmlFile: File): GameList {
        return xstreamStax.fromXML(xmlFile) as GameList
    }

    /**
     * This function is used to rewrite all the gamelist.xml file on a desired platform.
     *
     */
    fun writeGameList(gameList: GameList, outputXmlFile: File) {
        return FileWriter(outputXmlFile).use {
            xstreamStax.toXML(gameList, it)
        }
    }
}