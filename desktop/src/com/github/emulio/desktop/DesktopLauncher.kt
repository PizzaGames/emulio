package com.github.emulio.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.github.emulio.Emulio

object DesktopLauncher {
    @JvmStatic fun main(arg: Array<String>) {
        val config = LwjglApplicationConfiguration()

        config.width = 1024
        config.height = 768

        LwjglApplication(Emulio(), config)
    }
}
