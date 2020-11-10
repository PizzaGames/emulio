package com.github.emulio.service.scanner

import com.github.emulio.model.Game
import com.github.emulio.model.Platform
import com.github.emulio.service.xml.XMLReader
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import mu.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class GameScannerService(private val platforms: List<Platform>) {

	private val logger = KotlinLogging.logger { }
	private val reader = XMLReader()
	
	fun fullScan() : Flowable<Game> {
		var games = Flowable.empty<Game>()

		platforms.forEach { platform ->
			logger.info { "Scanning games for platform ${platform.platformName}" }

			val romsPath = platform.romsPath
			if (romsPath.isDirectory) {
				val gameList = File(romsPath, "gamelist.xml")
				val pathSet = mutableSetOf<String>()

				val listGamesFlowable = if (gameList.isFile) {
					readGameList(gameList, reader, romsPath, pathSet, platform)
				} else {
					Flowable.empty()
				}

				val filesObservable: Flowable<Game> = Flowable.create({ emitter ->
					scanFiles(romsPath, emitter, pathSet, platform)
					emitter.onComplete()
				}, BackpressureStrategy.BUFFER)

				games = games.concatWith(listGamesFlowable).concatWith(filesObservable)
			}
		}

		return games
	}

	private fun readGameList(gameList: File, xmlReader: XMLReader, romsPath: File, pathSet: MutableSet<String>, platform: Platform): Flowable<Game> {
		logger.info { "reading [${gameList.absolutePath}]" }
		val gamesObservable = xmlReader.parseGameList(gameList, romsPath, pathSet, platform)

		logger.debug { "Game list read, scanning for new games" }
		return gamesObservable
	}


	private fun scanFiles(root: File, observableEmitter: FlowableEmitter<Game>, pathSet: MutableSet<String>, platform: Platform) {
		val extensions = platform.romsExtensions.toSet()

		Files.walk(root.toPath()).filter { path ->
			!pathSet.contains(path.toAbsolutePath().toString()) && extensions.contains(path.extension)
		}.forEach { path ->
			observableEmitter.onNext(Game(path.toFile(), platform))
		}
	}

	private val Path.extension: String
		get() {
			val name = fileName?.toString() ?: return ""
			val idx = name.lastIndexOf(".")
			if (idx == -1) {
				return ""
			}
			return name.substring(idx)
		}
}


