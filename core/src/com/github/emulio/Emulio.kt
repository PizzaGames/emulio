package com.github.emulio

import com.badlogic.gdx.Game
import com.github.emulio.model.EmulioConfig
import com.github.emulio.model.Platform
import com.github.emulio.model.theme.Theme
import com.github.emulio.ui.screens.SplashScreen


class Emulio : Game() {

	override fun create() {
		setScreen(SplashScreen(this))
	}

	var games: MutableMap<Platform, MutableList<com.github.emulio.model.Game>>? = null

	lateinit var theme: MutableMap<Platform, Theme>
	lateinit var platforms: List<Platform>
	lateinit var config: EmulioConfig
	
}