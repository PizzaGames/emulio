package com.github.emulio

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.github.emulio.model.EmulioOptions
import com.github.emulio.model.Platform
import com.github.emulio.model.config.EmulioConfig
import com.github.emulio.model.theme.Theme
import com.github.emulio.ui.screens.SplashScreen
import mu.KotlinLogging
import java.io.InputStream

class Emulio(val options: EmulioOptions) : Game() {

    val logger = KotlinLogging.logger { }

    val workdir = options.workdir
    val data: MutableMap<String, Any> = mutableMapOf()
    val skin: Skin by lazy {
        Skin(Gdx.files.internal("skin/emulio-skin.json"))
    }

    var games: MutableMap<Platform, MutableList<com.github.emulio.model.Game>>? = null

    lateinit var theme: MutableMap<Platform, Theme>
    lateinit var platforms: List<Platform>
    lateinit var config: EmulioConfig

	override fun create() {
        showMotd()
		screen = SplashScreen(this)
    }

    private fun showMotd() {
        logger.info {
            """
                                          ___                  
                                         /\_ \    __           
                   __    ___ ___   __  __\//\ \  /\_\    ___   
                 /'__`\/' __` __`\/\ \/\ \ \ \ \ \/\ \  / __`\ 
                /\  __//\ \/\ \/\ \ \ \_\ \ \_\ \_\ \ \/\ \L\ \
                \ \____\ \_\ \_\ \_\ \____/ /\____\\ \_\ \____/
                 \/____/\/_/\/_/\/_/\/___/  \/____/ \/_/\/___/ 
    
                 - Welcome to the next level, starting emulio.
                 - Remember to check the log files once in a while.
                 - Be sure to have a emulio-platforms.yaml file on
                   the workdir '${options.workdir.canonicalPath}'
    
                 - Enjoy!
            """
        }
    }

    fun listGames(platform: Platform): List<com.github.emulio.model.Game> {
        return games!![platform]?.toList() ?: emptyList()
    }

    fun languageStream(): InputStream {
        val languageStream = Emulio::class.java.getResourceAsStream(config.languagePath)
        check(languageStream != null) {"Unable to find language file. ${config.languagePath}"}

        return languageStream
    }
}


