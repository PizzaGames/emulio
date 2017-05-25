package com.github.emulio.runners

import com.github.emulio.model.Game
import com.github.emulio.model.Platform
import com.github.emulio.xml.XMLReader
import io.reactivex.*
import mu.KotlinLogging
import java.io.File

class GameScanner(val platforms: List<Platform>) : Function0<Observable<Game>> {
	
	val logger = KotlinLogging.logger { }
	
	override fun invoke(): Observable<Game> {
		
		var games = Observable.empty<Game>()
		
		platforms.forEach { platform ->
			logger.info { "Analysing platform ${platform.platformName}" }

			val xmlReader = XMLReader()

			val romsPath = platform.romsPath
			if (romsPath.isDirectory) {
				val gameList = File(romsPath, "gamelist.xml")
				if (gameList.isFile) {
					logger.info { "reading [${gameList.absolutePath}]" }
					val gamesObservable = xmlReader.parseGameList(gameList, romsPath, platform)

					logger.debug { "gamelist read, scanning for new games"  }
					
					val filesObservable: Flowable<File> = Flowable.create({ emitter ->
						scanFiles(romsPath, emitter)
					}, BackpressureStrategy.LATEST)
					
					
					
//					val files: Observable<File> = Observable.create({ emitter ->
//						scanFiles(romsPath, emitter)
//					})

					games = games.concatWith(gamesObservable.filter { game ->
						
						true
					})

//					val start = System.currentTimeMillis()
//					val scannedGames = scanGames(romsPath, foundPaths)
//
//
//					logger.info { "scannedGames: ${scannedGames.size} / ${games.size}, time to scan: ${System.currentTimeMillis() - start}ms" }
				}
			}
		}
		
		return games
	}

	private fun scanFiles(root: File, observableEmitter: FlowableEmitter<File>) {

		if (root.isDirectory) {
			root.listFiles().forEach {
				scanFiles(it, observableEmitter)
			}
		} else {
			if (root.isFile) {
				observableEmitter.onNext(root)
			}
		}
	}

}