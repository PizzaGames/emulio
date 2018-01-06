package com.github.emulio.desktop

import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics
import com.badlogic.gdx.graphics.Color
import com.github.emulio.Emulio

object DesktopLauncher {


    @JvmStatic fun main(arg: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration().apply {
            setResizable(false)
            setDecorated(true)
            setInitialBackgroundColor(Color(0x000000FF))
            setTitle("Emulio")
//            setWindowedMode(1280, 720)
            setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode())
        }

        config.setWindowIcon(Files.FileType.Internal, "images/32-icon.png")

		Lwjgl3Application(Emulio({ minimizeApplication() }, { restoreAplication() }), config)
    }

    fun minimizeApplication() {
        (Gdx.graphics as Lwjgl3Graphics).window.iconifyWindow()
    }

    fun restoreAplication() {
        (Gdx.graphics as Lwjgl3Graphics).window.restoreWindow()
    }

}