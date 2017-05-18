package com.github.emulio.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.github.emulio.Emulio
import com.github.emulio.config.Config


object DesktopLauncher {
    @JvmStatic fun main(arg: Array<String>) {
        val desktopConfig = LwjglApplicationConfiguration()

        //detect current resolution and stick with it
        //config.fullscreen = true;


        LwjglApplication(Emulio({
            config: Config ->

            println("aooo takeda")
            desktopConfig.apply {
                fullscreen = config.fullscreen
                width = config.width
                height = config.height
            }





        }), desktopConfig)
    }
}
