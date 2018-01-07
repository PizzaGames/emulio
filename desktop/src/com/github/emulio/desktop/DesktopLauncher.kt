package com.github.emulio.desktop

import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics
import com.badlogic.gdx.graphics.Color
import com.github.emulio.Emulio
import com.github.emulio.model.EmulioConfig
import com.github.emulio.model.GraphicsConfig
import com.github.emulio.yaml.YamlUtils
import java.io.File

object DesktopLauncher {

    private fun initializeEmulioConfig(): EmulioConfig {
        return EmulioConfig().apply {
            loadDefaults()
        }
    }

    @JvmStatic fun main(arg: Array<String>) {

        val yamlUtils = YamlUtils()
        val configFile = File("emulio-config.yaml")

        val graphicsConfig = if (configFile.exists()) {
            val emulioConfig = yamlUtils.parseEmulioConfig(configFile)
            emulioConfig.graphicsConfig
        } else {
            GraphicsConfig().apply {
                screenWidth = 1280
                screenHeight = 720
                fullscreen = false
                vsync = true
            }
        }


        val config = Lwjgl3ApplicationConfiguration().apply {
            setResizable(false)
            setDecorated(true)
            setInitialBackgroundColor(Color(0x000000FF))
            setTitle("Emulio")

//            setWindowedMode(1920, 1000)
            if (!graphicsConfig.fullscreen!!) {
                setWindowedMode(1280, 720)
            } else {
                val displayMode = Lwjgl3ApplicationConfiguration.getDisplayModes().firstOrNull {
                    (it.width == graphicsConfig.screenWidth && it.height == graphicsConfig.screenHeight)
                } ?: Lwjgl3ApplicationConfiguration.getDisplayMode()


                setFullscreenMode(displayMode)

            }
        }

        config.setWindowIcon(Files.FileType.Internal, "images/32-icon.png")

		Lwjgl3Application(Emulio(DesktopLauncher::minimizeApplication, DesktopLauncher::restoreAplication), config)
    }

    private fun minimizeApplication() {
        (Gdx.graphics as Lwjgl3Graphics).window.iconifyWindow()
    }

    private fun restoreAplication() {
        (Gdx.graphics as Lwjgl3Graphics).window.restoreWindow()
    }

}