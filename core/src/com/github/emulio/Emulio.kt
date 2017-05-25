package com.github.emulio


import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.github.emulio.model.Platform
import com.github.emulio.runners.GameScanner
import com.github.emulio.runners.PlatformReader
import com.github.emulio.ui.reactive.GdxScheduler
import com.github.emulio.xml.XMLReader
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import mu.KotlinLogging
import java.io.File

class Emulio : ApplicationAdapter() {

	val logger = KotlinLogging.logger { }

	lateinit var platforms: List<Platform>
	lateinit var stage: Stage
	lateinit var lbLoading: Label

	override fun create() {
		logger.debug { "create()" }

		stage = Stage()
		Gdx.input.inputProcessor = stage

		val table = Table()
		table.setFillParent(true)

		val imgLogo = Image(Texture("images/logo.png"))
		table.add(imgLogo)


		val generator = FreeTypeFontGenerator(Gdx.files.internal("fonts/FrancoisOne-Regular.ttf"))
		val francoisFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
			size = 20
			color = Color(0x37424AFF)
		})

		lbLoading = Label("Initializing main interface", Label.LabelStyle().apply {
			font = francoisFont
		})
		lbLoading.setPosition(10f, 5f)

		stage.addActor(table)
		stage.addActor(lbLoading)
		

		lbLoading.setText("Loading platforms")

		observePlatforms()
	}

	private fun observePlatforms() {
		val platformsObservable: Observable<List<Platform>> = Observable.create({ subscriber ->
			val platforms = PlatformReader().invoke()
			
			subscriber.onNext(platforms)
			Thread.sleep(1000)
			subscriber.onComplete()
		})

		platformsObservable
				.subscribeOn(Schedulers.computation())
				.observeOn(GdxScheduler)
				.subscribe({ onPlatformsLoaded(it) }, { onPlatformsError(it) })
	}

	private fun onPlatformsError(exception: Throwable) {
		lbLoading.setText("Error loading platforms. Please check your \"emulio-platforms.yaml\" file\nPress any key to continue...")
		lbLoading.setPosition(10f, 20f)

		logger.error(exception, { "Error ocurred parsing emulio-platforms.yaml, please check your configuration files." })
		// Exit app on keypress
	}

	fun onPlatformsLoaded(platforms: List<Platform>) {
		this.platforms = platforms

		val gamelistObservable = XMLReader().parseGameList(File("/home/marcelo-frau/workspace/emulio/sample-files/Atari 2600/gamelist.xml"), File("/home/marcelo-frau/workspace/emulio/sample-files/Atari 2600/"), platform)

		var count = 0
		gamelistObservable
				.subscribeOn(Schedulers.computation())
				.observeOn(GdxScheduler)
				.subscribe({
					lbLoading.setText("Reading game $count")
					count++
				}, { onPlatformsError(it) })

	}

	override fun render() {
		Gdx.gl.glClearColor(0x6F, 0xBB, 0xDB, 0xFF)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
		stage.act(Math.min(Gdx.graphics.deltaTime, 1 / 30f))
		stage.draw()
	}

	override fun resize(width: Int, height: Int) {
		stage.viewport.update(width, height, true)
	}

	override fun dispose() {
		stage.dispose()
	}

}

private fun GL20.glClearColor(r: Int, g: Int, b: Int, alpha: Int) {
	this.glClearColor(r.toGLColor(), g.toGLColor(), b.toGLColor(), alpha.toGLColor())
}
private fun Int.toGLColor(): Float {
	return this / 255.0f
}
