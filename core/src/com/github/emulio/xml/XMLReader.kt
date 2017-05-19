package com.github.emulio.xml

import com.github.emulio.model.GameInfo
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.SAXParserFactory


class XMLReader {

    fun parseGameList(xmlFile: File): List<GameInfo> {

        val factory = SAXParserFactory.newInstance()
        val saxParser = factory.newSAXParser()

        val games = mutableListOf<GameInfo>()

        saxParser.parse(xmlFile, GameInfoSAXHandler(games))

        return games
    }
}

private class GameInfoSAXHandler(val gamelist: MutableList<GameInfo>) : DefaultHandler() {

    enum class Tag(val value: String) {
        GAME_LIST("gamelist"),
        GAME("game"),
        PATH("path"),
        NAME("name"),
        IMAGE("image"),
        RELEASE_DATE("releasedate"),
        DEVELOPER("developer"),
        GENRE("genre"),
        PLAYERS("players"),
        NO_STATE(""),
    }

    var tag: Tag = Tag.NO_STATE


    override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {

        if (qName.equals(Tag.GAME_LIST.value, true)) {
            tag = Tag.GAME_LIST
        } else if (qName.equals(Tag.GAME.value, true)) {
            tag = Tag.GAME

            for (i in 0..attributes.length) {
                if (attributes.getQName(i).equals("id", true)) {
                    id = attributes.getValue(i)
                } else if (attributes.getQName(i).equals("source", true)) {
                    source = attributes.getValue(i)
                }
            }

        } else if (qName.equals(Tag.PATH.value, true)) {
            tag = Tag.PATH
        } else if (qName.equals(Tag.NAME.value, true)) {
            tag = Tag.NAME
        } else if (qName.equals(Tag.IMAGE.value, true)) {
            tag = Tag.IMAGE
        } else if (qName.equals(Tag.RELEASE_DATE.value, true)) {
            tag = Tag.RELEASE_DATE
        } else if (qName.equals(Tag.DEVELOPER.value, true)) {
            tag = Tag.DEVELOPER
        } else if (qName.equals(Tag.GENRE.value, true)) {
            tag = Tag.GENRE
        } else if (qName.equals(Tag.PLAYERS.value, true)) {
            tag = Tag.PLAYERS
        }
    }

    var id: String? = null
    var source: String? = null
    var path: String? = null
    var name: String? = null
    var description: String? = null
    var image: String? = null
    var releaseDate: Date? = null
    var developer: String? = null
    var publisher: String? = null
    var genre: String? = null
    var players: String? = null



    override fun endElement(uri: String?, localName: String?, qName: String?) {
        tag = Tag.NO_STATE

        //TODO throw exception?
        check(id != null)
        check(path != null)

        gamelist.add(
                GameInfo(id!!,
                        source,
                        path!!,
                        name,
                        description,
                        image,
                        releaseDate,
                        developer,
                        publisher,
                        genre,
                        players))
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        when (tag) {
            Tag.PATH -> {path = String(ch, start, length)}
            Tag.NAME -> {name = String(ch, start, length)}
            Tag.IMAGE -> {image = String(ch, start, length)}
            Tag.RELEASE_DATE -> {releaseDate = convertDate(ch, start, length)}
            Tag.DEVELOPER -> {developer = String(ch, start, length)}
            Tag.GENRE -> {genre = String(ch, start, length)}
            Tag.PLAYERS -> {players = String(ch, start, length)}
        }
    }

    private fun convertDate(ch: CharArray, start: Int, length: Int): Date {
        val calendar = GregorianCalendar.getInstance()

        var currentOffset = start
        calendar.set(Calendar.YEAR, Integer.parseInt(String(ch, currentOffset, 4)))
        currentOffset += 4

        calendar.set(Calendar.MONTH, Integer.parseInt(String(ch, currentOffset, 2)) - 1) //months starts with 0
        currentOffset += 2

        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(String(ch, currentOffset, 2)))
        currentOffset += 2
        currentOffset += 1 // T

        calendar.set(Calendar.HOUR, Integer.parseInt(String(ch, currentOffset, 2)))
        currentOffset += 2

        calendar.set(Calendar.MINUTE, Integer.parseInt(String(ch, currentOffset, 2)))
        currentOffset += 2

        calendar.set(Calendar.SECOND, Integer.parseInt(String(ch, currentOffset, 2)))
        currentOffset += 2

        return calendar.time

    }

}


fun main(args: Array<String>) {
    val start = System.currentTimeMillis()
    val gamelist = XMLReader().parseGameList(File("sample-files/Atari 2600/gamelist.xml"))
    val elapsed = System.currentTimeMillis() - start

    gamelist.forEach {
        println(it)
    }

    println("document parsed in: ${elapsed}ms")
}
