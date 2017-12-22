package com.github.emulio.desktop

import com.badlogic.gdx.Files
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
//            width = 1920
//            height = 1080
            width = 1280
            height = 720
//			fullscreen = true
//            width = 1600
//            height = 1200
            title = "Emulio"
            backgroundFPS = 5
            foregroundFPS = 50
//            initialBackgroundColor = Color(0x6FBBDBFF)
			initialBackgroundColor = Color(0x000000FF)


			addIcon("images/32-icon.png", Files.FileType.Internal)
        }




		LwjglApplication(Emulio(), config)
	
		
    }
}
