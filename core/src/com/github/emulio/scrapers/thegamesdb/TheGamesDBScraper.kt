package com.github.emulio.scrapers.thegamesdb

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.annotations.XStreamAlias
import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import com.thoughtworks.xstream.annotations.XStreamConverter
import com.thoughtworks.xstream.annotations.XStreamImplicit
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter
import com.thoughtworks.xstream.io.xml.StaxDriver
import mu.KotlinLogging
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URLEncoder

/**
 * Documentation of api can be found in: http://wiki.thegamesdb.net/index.php?title=API_Introduction
 *
 * <b>Note:</b>
 *
 * Xstream is used here to parse all xmls, this is used due to easyness to use, but
 * the performance is a little compromized, maybe this can be changed to a better/
 * more performatic parser (but xStream have a good performance, so, make sure you
 * have a good choice)
 */
object TheGamesDBScraper {
    val logger = KotlinLogging.logger { }

    fun platformsList(): DataPlatformsList {
        logger.debug { "platformsList" }
        val xStream = getXStream()
        xStream.alias("Data", DataPlatformsList::class.java)
        xStream.alias("Platform", Platform::class.java)
        val url = "http://thegamesdb.net/api/GetPlatformsList.php"

        return performRequest(url, xStream) as DataPlatformsList
    }

    private fun performRequest(url: String, xStream: XStream) = HttpClients.createDefault().use { httpClient ->
        logger.debug { "performRequest: $url"  }
        val httpGet = HttpGet(url)
        httpClient.execute(httpGet).use { response ->
            response.entity.content.use { stream ->
                xStream.fromXML(stream)
            }
        }
    }


    private fun getXStream() = XStream(StaxDriver()).apply {
        XStream.setupDefaultSecurity(this)
        allowTypes(arrayOf(
                DataGetGamesList::class.java,
                DataGetGame::class.java,
                DataArt::class.java,
                DataGetPlatform::class.java,
                DataPlatformGames::class.java,
                Game::class.java,
                AlternateTitle::class.java,
                Genres::class.java,
                Similar::class.java,
                DataPlatformsList::class.java,
                Platform::class.java,
                FanartImages::class.java,
                Fanart::class.java,
                Original::class.java,
                Boxart::class.java,
                Banner::class.java
        ))
    }

    fun platformGames(platform: String): DataPlatformGames {
        logger.debug { "platformGames" }
        val xStream = getXStream()
        xStream.alias("Data", DataPlatformGames::class.java)
        xStream.processAnnotations(DataPlatformGames::class.java)

        val url = "http://thegamesdb.net/api/PlatformGames.php?platform=${URLEncoder.encode(platform, "UTF-8")}"

        return performRequest(url, xStream) as DataPlatformGames
    }

    fun getPlatform(id: Int): DataGetPlatform {
        logger.debug { "getPlatform" }
        val xStream = getXStream()
        xStream.alias("Data", DataGetPlatform::class.java)
        xStream.alias("Platform", Platform::class.java)
        xStream.alias("Images", FanartImages::class.java)
        xStream.processAnnotations(FanartImages::class.java)
        xStream.processAnnotations(Fanart::class.java)
        xStream.processAnnotations(Original::class.java)
        xStream.processAnnotations(Boxart::class.java)
        xStream.processAnnotations(Banner::class.java)
        xStream.alias("fanart", Fanart::class.java)
        val url = "http://thegamesdb.net/api/GetPlatform.php?id=$id"

        return performRequest(url, xStream) as DataGetPlatform
    }

    fun getArt(id: Int): DataArt {
        logger.debug { "getArt" }
        val xStream = getXStream()
        xStream.alias("Data", DataArt::class.java)
        xStream.alias("Images", FanartImages::class.java)
        xStream.processAnnotations(Fanart::class.java)
        xStream.processAnnotations(Original::class.java)
        xStream.processAnnotations(Boxart::class.java)
        xStream.processAnnotations(Banner::class.java)
        xStream.processAnnotations(FanartImages::class.java)
        xStream.alias("fanart", Fanart::class.java)

        val url = "http://thegamesdb.net/api/GetArt.php?id=$id"

        return performRequest(url, xStream) as DataArt
    }

    fun getGame(id: Int? = null, name: String? = null, exactName: String? = null, platform: String? = null): DataGetGame {
        logger.debug { "getGame" }
        val xStream = getXStream()
        xStream.alias("Data", DataGetGame::class.java)
        xStream.processAnnotations(DataGetGame::class.java)
        xStream.alias("Platform", Platform::class.java)
        xStream.alias("Game", Game::class.java)
        xStream.alias("Images", FanartImages::class.java)
        xStream.processAnnotations(FanartImages::class.java)
        xStream.processAnnotations(Fanart::class.java)
        xStream.processAnnotations(Original::class.java)
        xStream.processAnnotations(Boxart::class.java)
        xStream.processAnnotations(Banner::class.java)
        xStream.alias("fanart", Fanart::class.java)

        check(id != null || name != null) { "A name or id must be provided"}

        val url = StringBuilder("http://thegamesdb.net/api/GetGame.php?")

        if (id != null) {
            url.append("id=$id&")
        }

        if (name != null) {
            url.append("name=$name&")
        }

        if (exactName != null) {
            url.append("exactname=$exactName&")
        }

        if (platform != null) {
            url.append("platform=$platform&")
        }

        url.setLength(url.length - 1) // always will end with & so we remove it here

        return performRequest(url.toString(), xStream) as DataGetGame
    }

    fun getGamesList(name: String? = null, genre: String? = null, platform: String? = null): DataGetGamesList {
        logger.debug { "getGamesList" }
        val xStream = getXStream()
        xStream.alias("Data", DataGetGamesList::class.java)
        xStream.processAnnotations(DataGetGamesList::class.java)
        xStream.alias("Platform", Platform::class.java)
        xStream.alias("Game", Game::class.java)
        xStream.processAnnotations(Genres::class.java)
        xStream.processAnnotations(Game::class.java)
        xStream.alias("Images", FanartImages::class.java)
        xStream.processAnnotations(Fanart::class.java)
        xStream.processAnnotations(Original::class.java)
        xStream.processAnnotations(Boxart::class.java)
        xStream.processAnnotations(Banner::class.java)
        xStream.processAnnotations(FanartImages::class.java)
        xStream.alias("fanart", Fanart::class.java)

        check(name != null || genre != null) { "A name or genre must be provided"}

        val url = StringBuilder("http://thegamesdb.net/api/GetGamesList.php?")

        if (name != null) {
            url.append("name=$name&")
        }

        if (genre != null) {
            url.append("genre=$genre&")
        }

        if (platform != null) {
            url.append("platform=$platform&")
        }

        url.setLength(url.length - 1) // always will end with & so we remove it here

        return performRequest(url.toString(), xStream) as DataGetGamesList
    }

    fun downloadImage(baseUrl: String, path: String, destiny: File) {
        downloadImage("$baseUrl$path", destiny)
    }

    fun downloadImage(url: String, destiny: File) {
        HttpClients.createDefault().use { httpClient ->
            val httpGet = HttpGet(url)
            httpClient.execute(httpGet).use { response ->
                response.entity.content.use { stream ->
                    readStream(destiny, stream)
                }
            }
        }
    }

    private fun readStream(destiny: File, stream: InputStream?): Int {
        return FileOutputStream(destiny).use { fos ->
            IOUtils.copy(stream, fos)
        }
    }


}

// Sample api tests
//fun main(args: Array<String>) {
//    println("; time: ${measureTimeMillis { print("platformsList: ${TheGamesDBScraper.platformsList()}") }}")
//    println("; time: ${measureTimeMillis { print("platformGames: ${TheGamesDBScraper.platformGames("microsoft xbox 360")}") }}")
//    println("; time: ${measureTimeMillis { print("getPlatform: ${TheGamesDBScraper.getPlatform(15)}") }}")
//    println("; time: ${measureTimeMillis { print("getArt: ${TheGamesDBScraper.getArt(15)}") }}")
//    println("; time: ${measureTimeMillis { print("getGame: ${TheGamesDBScraper.getGame(id=15)}") }}")
//    println("; time: ${measureTimeMillis { print("getGamesList: ${TheGamesDBScraper.getGamesList(name="donkey")}") }}")
//    TheGamesDBScraper.downloadImage("http://thegamesdb.net/banners/", "fanart/original/15-2.jpg", File("g:/15-2.jpg"))
//    println("xStream time: ${measureTimeMillis { XStream(StaxDriver()) }}")
//}

/**
 * Classes used to represent the data from:
 * http://thegamesdb.net/api/GetGamesList.php?name=x-men
 *
 * Official documentation:
 * http://wiki.thegamesdb.net/index.php/GetGamesList
 */
data class DataGetGamesList(
        val baseImgUrl: String?,
        @XStreamImplicit(itemFieldName = "Game")
        var games: List<Game> = listOf()
)


/**
 * Classes used to represent the data from:
 * http://thegamesdb.net/api/GetGame.php?id=2
 *
 * Official documentation:
 * http://wiki.thegamesdb.net/index.php/GetGame
 */
data class DataGetGame(
    val baseImgUrl: String?,
    @XStreamImplicit(itemFieldName = "Game")
    var games: List<Game> = listOf()
)

/**
 * Classes used to represent the data from:
 *http://thegamesdb.net/api/GetArt.php?id=2
 *
 * Official documentation:
 * http://wiki.thegamesdb.net/index.php/GetArt
 */
data class DataArt(
    val baseImgUrl: String?,
    var Images: FanartImages?
)

/**
 * Classes used to represent the data from:
 * http://thegamesdb.net/api/GetPlatform.php?id=15
 *
 * Official documentation:
 * http://wiki.thegamesdb.net/index.php/GetPlatform
 */
data class DataGetPlatform(
    val baseImgUrl: String?,
    var Platform: Platform?
)

/**
 * Classes used to represent the data from:
 * http://thegamesdb.net/api/PlatformGames.php?platform=xpto
 *
 * Official documentation:
 * http://wiki.thegamesdb.net/index.php/PlatformGames
 */
data class DataPlatformGames(
    @XStreamImplicit(itemFieldName = "Game")
    var games: List<Game> = listOf()
)
data class Game(
        val id: Int?,
        val GameTitle: String?,
        val ReleaseDate: String?,
        val AlternateTitles: AlternateTitle?,
        val thumb: String?,
        val Overview: String?,
        val ESRB: String?,
        @XStreamAlias("Co-op")
        val CoOp: String?,
        val Players: Int?,
        val Genres: Genres?,
        val Youtube: String?,
        val Publisher: String?,
        val Developer: String?,
        val Similar: Similar?,
        val Platform: String?,
        val PlatformId: Int?,
        val Rating: Float?,
        val Images: FanartImages?
)

data class AlternateTitle(
    var title: String
)
data class Genres(
    @XStreamImplicit(itemFieldName = "genre")
    var genres: List<String> = listOf()
)

data class Similar(
    @XStreamImplicit(itemFieldName = "Game")
    var games: List<Game> = listOf()
)


/**
 * Classes used to represent the data from:
 * http://thegamesdb.net/api/GetPlatformsList.php
 *
 * Official documentation:
 * http://wiki.thegamesdb.net/index.php/GetPlatformsList
*/
data class DataPlatformsList(
    val Platforms: List<Platform> = listOf(),
    val basePlatformUrl: String?
)
data class Platform(
    val id: Int?,
    val name: String?,
    val alias: String?,
    val Platform: String?,
    val console: String?,
    val controller: String?,
    val overview: String?,
    val developer: String?,
    val manufacturer: String?,
    val cpu: String?,
    val memory: String?,
    val graphics: String?,
    val sound: String?,
    val display: String?,
    val media: String?,
    val maxcontrollers: Int?,
    val Rating: Float?,
    val Images: FanartImages?
)

data class FanartImages(
    @XStreamImplicit(itemFieldName = "fanart")
    var games: List<Fanart> = listOf(),

    @XStreamImplicit(itemFieldName = "boxart")
    var boxart: List<Boxart>? = null,
    @XStreamImplicit(itemFieldName = "banner")
    var banner: List<Banner>? = null,
    var consoleart: String? = null,
    var controllerart: String? = null,
    var screenshot: Fanart? = null,
    var clearlogo: String? = null
)
data class Fanart(
    val original: Original?,
    val thumb: String?
)

@XStreamConverter(value= ToAttributedValueConverter::class, strings= ["value"])
data class Original(
    @XStreamAsAttribute
    var height: Int?,
    @XStreamAsAttribute
    var width: Int?,
    @XStreamAsAttribute
    var thumb: String?,
    @XStreamAsAttribute
    var side: String?,
    var value: String?
)
@XStreamConverter(value= ToAttributedValueConverter::class, strings= ["value"])
data class Boxart(
    @XStreamAsAttribute
    var height: Int?,
    @XStreamAsAttribute
    var width: Int?,
    @XStreamAsAttribute
    var thumb: String?,
    @XStreamAsAttribute
    var side: String?,
    var value: String?
)
@XStreamConverter(value= ToAttributedValueConverter::class, strings= ["value"])
data class Banner(
    @XStreamAsAttribute
    var height: Int?,
    @XStreamAsAttribute
    var width: Int?,
    @XStreamAsAttribute
    var thumb: String?,
    @XStreamAsAttribute
    var side: String?,
    var value: String?
)


