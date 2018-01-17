package com.github.emulio.scrapers.thegamesdb

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.annotations.XStreamImplicit
import com.thoughtworks.xstream.io.xml.StaxDriver
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import java.net.URLEncoder
import kotlin.system.measureTimeMillis

/**
 * <b>Note:</b>
 *
 * Xstream is used here to parse all xmls, this is used due to easyness to use, but
 * the performance is a little compromized, maybe this can be changed to a better/
 * more performatic parser (but xstream have a good performance, so, make sure you
 * have a good choice)
 */
object TheGamesDBScraper {

    fun platformsList(): DataPlatformsList {

        val xstream = getXStream()
        xstream.alias("Data", DataPlatformsList::class.java)
        xstream.alias("Platform", Platform::class.java)

        return HttpClients.createDefault().use { httpClient ->
            val httpGet = HttpGet("http://thegamesdb.net/api/GetPlatformsList.php")
            httpClient.execute(httpGet).use { response ->
                response.entity.content.use { stream ->
                    xstream.fromXML(stream) as DataPlatformsList
                }
            }
        }
    }

    private fun getXStream() = XStream(StaxDriver()).apply {
        //XStream.setupDefaultSecurity(this)
//        allowTypes()
    }

    fun platformGames(platform: String): DataPlatformGame {
        val xstream = getXStream()
        xstream.alias("Data", DataPlatformGame::class.java)
        xstream.processAnnotations(DataPlatformGame::class.java)

        return HttpClients.createDefault().use { httpClient ->
            val httpGet = HttpGet("http://thegamesdb.net/api/PlatformGames.php?platform=${URLEncoder.encode(platform, "UTF-8")}")

            httpClient.execute(httpGet).use { response ->
                response.entity.content.use { stream ->
                    xstream.fromXML(stream) as DataPlatformGame

                }
            }
        }
    }

    fun getPlatform(id: Int): DataPlatform {
        val xstream = getXStream()
        xstream.alias("Data", DataPlatform::class.java)
        xstream.alias("Platform", Platform::class.java)
        xstream.alias("Images", FanartImages::class.java)
        xstream.processAnnotations(FanartImages::class.java)
        xstream.alias("fanart", Fanart::class.java)

        return HttpClients.createDefault().use { httpClient ->
            val httpGet = HttpGet("http://thegamesdb.net/api/GetPlatform.php?id=$id")

            httpClient.execute(httpGet).use { response ->
                response.entity.content.use { stream ->
                    xstream.fromXML(stream) as DataPlatform
                }
            }
        }
    }

    fun getArt(id: Int): DataArt {
        val xstream = getXStream()
        xstream.alias("Data", DataArt::class.java)
        xstream.alias("Images", FanartImages::class.java)
        xstream.processAnnotations(FanartImages::class.java)
        xstream.alias("fanart", Fanart::class.java)

        return HttpClients.createDefault().use { httpClient ->
            val httpGet = HttpGet("http://thegamesdb.net/api/GetArt.php?id=$id")

            httpClient.execute(httpGet).use { response ->
                response.entity.content.use { stream ->
                    xstream.fromXML(stream) as DataArt
                }
            }
        }
    }


}

fun main(args: Array<String>) {
//    println("; time: ${measureTimeMillis { print("platformsList: ${TheGamesDBScraper.platformsList()}") }}")
//    println("; time: ${measureTimeMillis { print("platformGames: ${TheGamesDBScraper.platformGames("microsoft xbox 360")}") }}")
//    println("; time: ${measureTimeMillis { print("getPlatform: ${TheGamesDBScraper.getPlatform(15)}") }}")
    println("; time: ${measureTimeMillis { print("getArt: ${TheGamesDBScraper.getArt(15)}") }}")


    println("xstream time: ${measureTimeMillis { XStream(StaxDriver()) }}")
}

//documentation of api can be found in: http://wiki.thegamesdb.net/index.php?title=API_Introduction

//missing
//getGame
//getGamesList
//image downloads


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
data class DataPlatform(
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
class DataPlatformGame {
    @XStreamImplicit(itemFieldName = "Game")
    var games: List<Game> = listOf()
}
data class Game(
    val id: Int?,
    val GameTitle: String?,
    val ReleaseDate: String?,
    val thumb: String?
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

//FIXME this class needs to use the boxart and all other necessary attributes.
class FanartImages {
    @XStreamImplicit(itemFieldName = "fanart")
    var games: List<Fanart> = listOf()

    @XStreamImplicit(itemFieldName = "boxart")
    var boxart: List<Boxart>? = null
    @XStreamImplicit(itemFieldName = "banner")
    var banner: List<Banner>? = null
    var consoleart: String? = null
    var controllerart: String? = null
    var screenshot: Fanart? = null
    var clearlogo: String? = null
}
data class Fanart(
    val original: String?, //TODO width, height
    val thumb: String?
)
class Boxart
class Banner

