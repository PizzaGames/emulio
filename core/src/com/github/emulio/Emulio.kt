package com.github.emulio

import com.badlogic.gdx.Game
import com.github.emulio.model.EmulioConfig
import com.github.emulio.model.Platform
import com.github.emulio.model.theme.Theme
import com.github.emulio.ui.screens.SplashScreen
import java.io.InputStream


class Emulio(val minimizeApplication: () -> Unit, val restoreApplication: () -> Unit) : Game() {

	override fun create() {
		screen = SplashScreen(this)
	}

	var games: MutableMap<Platform, MutableList<com.github.emulio.model.Game>>? = null

	lateinit var theme: MutableMap<Platform, Theme>
	lateinit var platforms: List<Platform>
	lateinit var config: EmulioConfig

    fun listGames(platform: Platform): List<com.github.emulio.model.Game> {
        return games!![platform]?.toList() ?: emptyList()
    }

    fun getLanguageStream(): InputStream {

        val languageStream = Emulio::class.java.getResourceAsStream(config.languagePath)

        check(languageStream != null, {"Unable to find language file. ${config.languagePath}"})
        return languageStream
    }
}

