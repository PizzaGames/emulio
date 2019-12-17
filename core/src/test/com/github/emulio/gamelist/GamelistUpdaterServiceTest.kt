package com.github.emulio.gamelist

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileReader
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class GamelistUpdaterServiceTest {


    @ExperimentalTime
    @Test
    fun shouldReadWriteGameListXMLFileRandomRecords() {
        shouldCreateSimpleGamelistXMLFile(Random.nextInt(10000, 150000), Random.nextInt(10000, 150000))
    }

    @ExperimentalTime
    @Test
    fun shouldReadWriteGameListXMLFile100000Records() {
        shouldCreateSimpleGamelistXMLFile(100000, 100000)
    }

    @ExperimentalTime
    @Test
    fun shouldReadWriteGameListXMLFile1000Records() {
        shouldCreateSimpleGamelistXMLFile(1000, 1000)
    }

    @ExperimentalTime
    @Test
    fun shouldReadWriteGameListXMLFile100Records() {
        shouldCreateSimpleGamelistXMLFile(100, 100)
    }

    @ExperimentalTime
    @Test
    fun shouldReadWriteGameListXMLFile1Record() {
        shouldCreateSimpleGamelistXMLFile(1, 1, true)
    }

    @ExperimentalTime
    private fun shouldCreateSimpleGamelistXMLFile(gamesCount: Int, foldersCount: Int, printXml: Boolean = false) {
        println("shouldCreateSimpleGamelistXMLFile $gamesCount, $foldersCount")
        val tempFile = createTempFile()
        val gameList = GameList(
                createGames(gamesCount),
                createFolders(foldersCount)
        )

        println("== Generating xml file with $gamesCount games and $foldersCount folders")

        println("Time to generate file: " + measureTime {
            GamelistUpdaterService.writeGameList(gameList, tempFile)
        })

        val readGameList: GameList
        println("Time to read file: " + measureTime {
            readGameList = GamelistUpdaterService.readGameList(tempFile)
        })

        if (printXml) {
            FileReader(tempFile).useLines { println(it) }
        }

        Assertions.assertEquals(readGameList, gameList)
        println("== Tests done for xml file with $gamesCount games and $foldersCount folders")
    }

    private fun createFolders(count: Int): List<Folder> {
        return ArrayList<Folder>().apply {
            for (i in 0..count) {
                add(createFolder(i))
            }
        }
    }

    private fun createGames(count: Int): List<Game> {
        return ArrayList<Game>().apply {
            for (i in 0..count) {
                add(createGame(i))
            }
        }
    }

    private fun createFolder(index: Int = Random.nextInt()) = Folder("Random Folder Name $index", "Random folder description $index")

    private fun createGame(index: Int = Random.nextInt()) = Game(
        name ="Random game Name $index",
        desc = "Random description $index",
        image = "image $index",
        thumbnail = "thumbnail $index",
        video = "video $index",
        rating = index.div(10f),
        releasedate = Date(),
        developer = "developer $index",
        publisher = "publisher $index",
        genre = "genre $index",
        players = index,
        playcount = index,
        lastplayed = Date(),
        sortname = "sortname $index"
    )

    private fun createTempFile() = File.createTempFile("test_emulio_gamelist", "xml").apply {
        deleteOnExit()
    }

}