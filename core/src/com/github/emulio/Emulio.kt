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
import io.reactivex.Observable
import javafx.application.Platform
import mu.KotlinLogging

class Emulio : ApplicationAdapter() {

	val logger = KotlinLogging.logger { }

	lateinit var stage: Stage

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
			size = 30
			color = Color(0x37424AFF)
		})

		val lbLoading = Label("Initializing main interface", Label.LabelStyle().apply {
			font = francoisFont
		})
		lbLoading.setPosition(10f, 2f)

		stage.addActor(table)
		stage.addActor(lbLoading)


		val platform: Observable<Platform> = Observable.create({

		})


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
