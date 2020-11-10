package com.github.emulio.service.scrapers.igdb

import com.api.igdb.apicalypse.APICalypse
import com.api.igdb.request.IGDBWrapper
import com.api.igdb.request.TwitchAuthenticator
import com.api.igdb.request.platforms
import com.api.igdb.utils.TwitchToken
import mu.KotlinLogging
import proto.Platform
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

val logger = KotlinLogging.logger { }

object IGDBScrapper {

    private const val clientID = "rezh0dl5q9j2q8zyaphj6obqzygqy9"
    private const val clientSecret = "56rca1bne2xa6qkhxbxum2cd8e10ee"

    private var cachedToken: TwitchToken? = null

    fun getPlatforms(): List<Platform> {
        IGDBWrapper.setCredentials(clientID, getToken().access_token)
        val platforms = IGDBWrapper.platforms(APICalypse().fields("*"))

        return platforms
    }


    private fun getToken(): TwitchToken {
        val token = if (cachedToken != null) {
            getTokenNotExpired()
        } else {
            requestToken()
        }

        cachedToken = token

        return token
    }

    private fun getTokenNotExpired(): TwitchToken {
        val token = checkNotNull(cachedToken)
        val now = LocalDateTime.now()
        val expirationDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(token.getExpiresUnix()), ZoneId.systemDefault())

        return if (now.isAfter(expirationDate)) {
            requestToken()
        } else {
            token
        }
    }

    private fun requestToken(): TwitchToken {
        return checkNotNull(
                TwitchAuthenticator.requestTwitchToken(
                        clientID,
                        clientSecret))
    }

}

fun main() {
    val platforms = IGDBScrapper.getPlatforms()

    logger.info { "platforms: $platforms" }

}