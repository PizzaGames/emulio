package com.github.emulio.xml

import com.github.emulio.model.Game
import com.github.emulio.model.Platform
import io.reactivex.FlowableEmitter
import io.reactivex.ObservableEmitter
import mu.KotlinLogging
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.util.*

class GameInfoSAXHandler(val emitter: FlowableEmitter<Game>, val baseDir: File, val pathSet: MutableSet<String>, val platform: Platform) : DefaultHandler() {

	val logger = KotlinLogging.logger { }

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
        RATING("rating"),
        PUBLISHER("publisher"),
        DESC("desc"),
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
		} else if (qName.equals(Tag.PUBLISHER.value, true)) {
			tag = Tag.PUBLISHER
		} else if (qName.equals(Tag.DESC.value, true)) {
			tag = Tag.DESC
		} else if (qName.equals(Tag.GENRE.value, true)) {
			tag = Tag.GENRE
		} else if (qName.equals(Tag.PLAYERS.value, true)) {
			tag = Tag.PLAYERS
        } else if (qName.equals(Tag.RATING.value, true)) {
            tag = Tag.RATING
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
    var rating: Float? = null

	override fun endElement(uri: String?, localName: String?, qName: String?) {
		if (qName.equals(Tag.GAME.value, true)) {


			if (path == null) {

//				if (emitter.size > 0) {
//					logger.error { "There was a problem parsing game after the ${emitter[emitter.size - 1].name}"  }
//				}

				throw XMLInvalidException("Error processing XML File. Incorrect '$qName' tag/structure ")
			}
			
			
			val gamePath = getFile(baseDir, path!!)
			pathSet.add(gamePath.absolutePath)
			
			emitter.onNext(
					Game(id, source, gamePath,
							name, description, if (image != null) File(baseDir, image) else null,
							releaseDate, developer,
							publisher, genre,
							players, rating, platform))

            name = null
            id = null
            source = null
            path = null
            image = null
            releaseDate = null
            developer = null
            publisher = null
            genre = null
            players = null
            rating = null


            description = null
		}
		
		if (qName.equals(Tag.GAME_LIST.value, true)) {
			emitter.onComplete()
		}

		tag = Tag.NO_STATE
	}

	fun getFile(baseDir: File, path: String): File {
		val pathFixed = if (path.startsWith("./")) {
			path.replaceFirst("./", "")
		} else {
			path
		}

		return File(baseDir, pathFixed)
	}

	override fun characters(ch: CharArray, start: Int, length: Int) {
		when (tag) {
			Tag.PATH -> {
                if (path == null) {
                    path = String(ch, start, length)
                } else {
                    path += String(ch, start, length)
                }
            }
			Tag.NAME -> {
                if (name == null) {
                    name = String(ch, start, length)
                } else {
                    name += String(ch, start, length)
                }
            }
			Tag.IMAGE -> {image = String(ch, start, length).trim()}
			Tag.RELEASE_DATE -> {releaseDate = convertDate(ch, start, length)}
			Tag.DEVELOPER -> {developer = String(ch, start, length).trim()}
			Tag.PUBLISHER -> {publisher = String(ch, start, length).trim()}
			Tag.DESC -> {
                if (description != null) {
                    description += String(ch, start, length)
                } else {
                    description = String(ch, start, length)
                }
            }
			Tag.GENRE -> {genre = String(ch, start, length).trim()}
			Tag.PLAYERS -> {players = String(ch, start, length).trim()}
            Tag.RATING -> {rating = readFloat(String(ch, start, length).trim())}
//            Tag.GAME_LIST -> TODO()
//            Tag.GAME -> TODO()
//            Tag.NO_STATE -> TODO()
        }
	}

    private fun readFloat(value: String): Float {
        return value.toFloat()
    }

    fun convertDate(ch: CharArray, start: Int, length: Int): Date {
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