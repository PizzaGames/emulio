package com.github.emulio

import com.badlogic.gdx.Game
import com.github.emulio.model.Platform
import com.github.emulio.model.theme.Theme
import com.github.emulio.screens.SplashScreen

class Emulio : Game() {

	override fun create() {
		setScreen(SplashScreen(this))
	}

	lateinit var games: MutableMap<Platform, MutableList<com.github.emulio.model.Game>>
	lateinit var theme: MutableMap<Platform, Theme>
	lateinit var platforms: List<Platform>


}