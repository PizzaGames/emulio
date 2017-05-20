package com.github.emulio.runners

import com.github.emulio.model.Game
import com.github.emulio.model.Platform
import com.github.emulio.xml.XMLReader
import mu.KotlinLogging
import java.io.File

class GameScanner(val platforms: List<Platform>) : Runnable {
	val logger = KotlinLogging.logger { }

	override fun run() {
		platforms.forEach { platform ->
			logger.info { "Analysing platform ${platform.platformName}" }

			val xmlReader = XMLReader()

			val romsPath = platform.romsPath
			if (romsPath.isDirectory) {
				val gameList = File(romsPath, "gamelist.xml")
				if (gameList.isFile) {
					logger.info { "reading [${gameList.absolutePath}]" }
					val games = xmlReader.parseGameList(gameList, romsPath)

					logger.debug { "gamelist read, scanning for new games"  }

					val foundPaths = games.map { it.path.absolutePath }.toHashSet()

					val start = System.currentTimeMillis()
					val scannedGames = scanGames(romsPath, foundPaths)

					logger.info { "scannedGames: ${scannedGames.size} / ${games.size}, time to scan: ${System.currentTimeMillis() - start}ms" }
				}
			}

		}
	}

	private fun scanGames(root: File, foundPaths: HashSet<String>, scannedGames: MutableList<Game> = mutableListOf()): List<Game> {

		if (root.isDirectory) {
			root.listFiles().forEach { scanGames(it, foundPaths, scannedGames) }
		} else {
			if (root.isFile && !foundPaths.contains(root.absolutePath)) {
				scannedGames += Game(null, null, root.absoluteFile, root.name, null, null, null, null, null, null, null)
			}
		}

		return scannedGames
	}

}