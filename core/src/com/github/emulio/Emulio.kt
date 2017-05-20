package com.github.emulio

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.github.emulio.model.Game
import com.github.emulio.xml.XMLReader
import com.github.emulio.yaml.YamlReader
import io.reactivex.Flowable
import java.io.File
import io.reactivex.schedulers.Schedulers
import mu.KotlinLogging


class Emulio : ApplicationAdapter() {

	val logger = KotlinLogging.logger { }

    lateinit var batch: SpriteBatch
    lateinit var img: Texture

    override fun create() {
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")
        
        val f = File("")
        f.lastModified()

		val source = Flowable.fromCallable {

			val startAll = System.currentTimeMillis()

			logger.info { "Reading platform file" }
			val platforms = YamlReader().parsePlatforms(File("emulio-platforms.yaml"))

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

			logger.info { "time to scan all: ${System.currentTimeMillis() - startAll}ms" }

			"Done"
		}


		val runBackground = source.subscribeOn(Schedulers.io())

		val showForeground = runBackground.observeOn(Schedulers.single())

		showForeground.subscribe({ println(it) }, { it.printStackTrace() })

		//Thread.sleep(2000)

        //Gdx.graphics.setFullscreenMode()
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

	override fun render() {
        Gdx.gl.glClearColor(1f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.begin()
        batch.draw(img, 0f, 0f)
        batch.end()
    }

    override fun dispose() {
        batch.dispose()
        img.dispose()
    }
}
