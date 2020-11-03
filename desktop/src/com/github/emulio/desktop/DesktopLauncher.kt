package com.github.emulio.desktop

import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics
import com.badlogic.gdx.graphics.Color
import com.github.emulio.Emulio
import com.github.emulio.model.EmulioOptions
import com.github.emulio.model.config.GraphicsConfig
import com.github.emulio.yaml.EmulioConfigYaml
import mu.KotlinLogging
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import java.io.File

val logger = KotlinLogging.logger { }

object DesktopLauncher {

    @JvmStatic fun main(arg: Array<String>) {
        val options = getEmulioOptions(arg) ?: return

        val configFile = File(options.workdir, "emulio-config.yaml")

        val graphicsConfig = if (configFile.exists()) {
            EmulioConfigYaml.read(configFile).graphicsConfig
        } else {
            GraphicsConfig().apply {
                screenWidth = 1280
                screenHeight = 720
                fullscreen = false
                vsync = true
            }
        }

        overrideResolution(graphicsConfig, options)

        val config = Lwjgl3ApplicationConfiguration().apply {
            setResizable(false)
            setDecorated(true)
            setInitialBackgroundColor(Color(0x000000FF))
            setTitle("Emulio")

            if (!graphicsConfig.fullscreen!!) {
                setWindowedMode(graphicsConfig.screenWidth!!, graphicsConfig.screenHeight!!)
            } else {
                val displayMode = Lwjgl3ApplicationConfiguration.getDisplayModes().firstOrNull {
                    (it.width == graphicsConfig.screenWidth && it.height == graphicsConfig.screenHeight)
                } ?: Lwjgl3ApplicationConfiguration.getDisplayMode()

                setFullscreenMode(displayMode)
            }
        }

        config.setWindowIcon(Files.FileType.Internal, "images/32-icon.png")

		Lwjgl3Application(Emulio(options), config)
    }

    private fun overrideResolution(gc: GraphicsConfig, options: EmulioOptions) {
        val overrideScreenSize = options.screenSize
        if (overrideScreenSize != null) {
            gc.screenWidth = overrideScreenSize.first
            gc.screenHeight = overrideScreenSize.second
        }

        if (options.fullscreen) {
            gc.fullscreen = true
        }
    }

    private fun getEmulioOptions(arg: Array<String>): EmulioOptions? {
        val parser = DefaultParser()
        val options = Options().apply {
            addOption("h", "help", false, "Help message")
            addOption("w", "workdir", true,
                    "Override the default workdir \n(default: ${File(".").absolutePath})")
            addOption("x", "width", true, "Override the screen width")
            addOption("y", "height", true, "Override the screen height")
            addOption("f", "fullscreen", false, "Override the fullscreen value")
            addOption("l", "languagefile", true, "Sets language file as the main emulio language")
            addOption(null, "forceWorkdirCreation", false, "Force workdir creation upon start")

        }

        val cmdLine = parser.parse(options, arg)

        if (cmdLine.hasOption("h")) {
            val help = HelpFormatter()
            help.printHelp("emulio", options)
            return null
        }

        val workdir = if (cmdLine.hasOption("w")) {
            File(cmdLine.getOptionValue("w"))
        } else {
            File(".").absoluteFile
        }

        if (!workdir.exists()) {
            workdir.mkdirs()
        }

        if (!workdir.isDirectory) {
            if (cmdLine.hasOption("forceWorkdirCreation")) {
                workdir.mkdirs()
            } else {
                error { "Workdir must exist to continue. Check your parameters. (workdir: ${workdir.absolutePath})" }
            }
        }

        val screenSize = if (cmdLine.hasOption("x") && cmdLine.hasOption("y")) {
            cmdLine.getOptionValue("x").toInt() to cmdLine.getOptionValue("y").toInt()
        } else {
            null
        }


        logger.debug { "workdir: ${workdir.absolutePath}" }

        return EmulioOptions(
                workdir,
                DesktopLauncher::minimizeApplication,
                DesktopLauncher::restoreApplication,
                screenSize,
                cmdLine.hasOption("f"))
    }

    private fun minimizeApplication() {
        (Gdx.graphics as Lwjgl3Graphics).window.iconifyWindow()
    }

    private fun restoreApplication() {
        (Gdx.graphics as Lwjgl3Graphics).window.restoreWindow()
    }

}