package com.github.emulio.runners

import com.github.emulio.model.Game
import com.github.emulio.model.Platform
import com.github.emulio.xml.XMLReader
import io.reactivex.*
import mu.KotlinLogging
import java.io.File
import java.nio.file.Files

class GameScanner(val platforms: List<Platform>) : Function0<Flowable<Game>> {
	val logger = KotlinLogging.logger { }
	
	fun fullScan() : Flowable<Game> {
		return invoke()
	}
	
	override fun invoke(): Flowable<Game> {
		
		var games = Flowable.empty<Game>()
		
		platforms.forEach { platform ->
			logger.info { "Sanning games for platform ${platform.platformName}" }

			val xmlReader = XMLReader()

			val romsPath = platform.romsPath
			if (romsPath.isDirectory) {
				val gameList = File(romsPath, "gamelist.xml")
				if (gameList.isFile) {
					logger.info { "reading [${gameList.absolutePath}]" }
					val pathSet = mutableSetOf<String>()
					val gamesObservable = xmlReader.parseGameList(gameList, romsPath, pathSet, platform)
					
					logger.debug { "gamelist read, scanning for new games"  }
					
					val filesObservable: Flowable<Game> = Flowable.create({ emitter ->
						scanFiles(romsPath, emitter, pathSet, platform)
						emitter.onComplete()
					}, BackpressureStrategy.BUFFER)

					games = games.concatWith(gamesObservable).concatWith(filesObservable)
				}
			}
		}
		
		return games
	}
	

	private fun scanFiles(root: File, observableEmitter: FlowableEmitter<Game>, pathSet: MutableSet<String>, platform: Platform) {
		Files.walk(root.toPath()).filter({ path ->
			!pathSet.contains(path.toAbsolutePath().toString())
		}).forEach { path ->
			observableEmitter.onNext(Game(path.toFile(), platform))
		}
//		if (root.isDirectory) {
//			root.listFiles().forEach {
//				scanFiles(it, observableEmitter, pathSet, platform)
//			}
//		} else {
//			if (root.isFile) {
//				if (!pathSet.contains(root.absolutePath)) {
//					println(root.absolutePath)
//					observableEmitter.onNext(Game(root, platform))
//				}
//			}
//		}
	}

}