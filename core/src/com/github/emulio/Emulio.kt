package com.github.emulio

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Game
import com.github.emulio.model.EmulioConfig
import com.github.emulio.model.Platform
import com.github.emulio.model.theme.Theme
import com.github.emulio.ui.screens.SplashScreen
import java.io.File


class Emulio : Game() {

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

    fun getLanguageFile(): File {
        val languageFile = File(config.languagePath)
        check(languageFile.exists(), {"Unable to find language file. ${languageFile.absolutePath}"})
        return languageFile
    }
}

