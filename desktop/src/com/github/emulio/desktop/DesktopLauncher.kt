package com.github.emulio.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.github.emulio.Emulio

object DesktopLauncher {
    @JvmStatic fun main(arg: Array<String>) {
        val config = LwjglApplicationConfiguration()

        config.width = 1280
        config.height = 720
        //config.fullscreen = true

        LwjglApplication(Emulio(), config)
    }
}
