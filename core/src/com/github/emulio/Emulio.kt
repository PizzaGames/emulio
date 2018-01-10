package com.github.emulio

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.github.emulio.model.EmulioConfig
import com.github.emulio.model.Platform
import com.github.emulio.model.theme.Theme
import com.github.emulio.ui.screens.SplashScreen
import java.io.File
import java.io.InputStream
import mu.KotlinLogging


class Emulio(val options: EmulioOptions) : Game() {

    val logger = KotlinLogging.logger { }

	override fun create() {

        logger.info { """
             ______     __    __     __  __     __         __     ______
            /\  ___\   /\ "-./  \   /\ \/\ \   /\ \       /\ \   /\  __ \
            \ \  __\   \ \ \-./\ \  \ \ \_\ \  \ \ \____  \ \ \  \ \ \/\ \
             \ \_____\  \ \_\ \ \_\  \ \_____\  \ \_____\  \ \_\  \ \_____\
              \/_____/   \/_/  \/_/   \/_____/   \/_____/   \/_/   \/_____/

             - Welcome to the next level, starting emulio.
             - Remember to check the log files once in a while.
             - Be sure to have a emulio-platforms.yaml file on
               the workdir '${options.workdir.canonicalPath}'

             - Enjoy!
        """ }

		screen = SplashScreen(this)
//        Gdx.input.isCursorCatched = true
//        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Crosshair)

    }

    val workdir = options.workdir

	var games: MutableMap<Platform, MutableList<com.github.emulio.model.Game>>? = null

	lateinit var theme: MutableMap<Platform, Theme>
	lateinit var platforms: List<Platform>
	lateinit var config: EmulioConfig

    val skin: Skin by lazy {
        Skin(Gdx.files.internal("skin/emulio-skin.json"))
    }

    fun listGames(platform: Platform): List<com.github.emulio.model.Game> {
        return games!![platform]?.toList() ?: emptyList()
    }

    fun getLanguageStream(): InputStream {

        val languageStream = Emulio::class.java.getResourceAsStream(config.languagePath)

        check(languageStream != null, {"Unable to find language file. ${config.languagePath}"})
        return languageStream
    }
}


data class EmulioOptions(
        val workdir: File,
        val minimizeApplication: () -> Unit,
        val restoreApplication: () -> Unit,
        val screenSize: Pair<Int, Int>?,
        val fullscreen: Boolean
)
