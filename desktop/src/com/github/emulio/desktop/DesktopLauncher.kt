package com.github.emulio.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.graphics.Color
import com.github.emulio.Emulio
import org.lwjgl.opengl.GL11

object DesktopLauncher {
    @JvmStatic fun main(arg: Array<String>) {
        val config = LwjglApplicationConfiguration().apply {
            samples = 16
            width = 1280
            height = 720
            title = "Emulio"
            backgroundFPS = 5
            foregroundFPS = 30
            initialBackgroundColor = Color(0x6FBBDBFF)
        }
		
		LwjglApplication(Emulio(), config)
	
		
    }
}
