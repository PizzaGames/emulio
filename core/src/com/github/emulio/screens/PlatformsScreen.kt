package com.github.emulio.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.github.emulio.Emulio
import mu.KotlinLogging
import com.github.emulio.utils.gdxutils.*

class PlatformsScreen(val emulio: Emulio): Screen {

	val logger = KotlinLogging.logger { }

	val stage: Stage

	init {
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

		val lbLoading = Label("Initializing main interface", Label.LabelStyle().apply {
			font = francoisFont
		})
		lbLoading.setPosition(10f, 5f)

		stage.addActor(table)
		stage.addActor(lbLoading)

		// load main configurations/games and all stuff.. from mongo?

		lbLoading.setText("TOOOOOLS")

	}

	override fun hide() {

	}

	override fun show() {

	}

	override fun render(delta: Float) {
		Gdx.gl.glClearColor(0x6F, 0xBB, 0xDB, 0xFF)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
		stage.act(Math.min(Gdx.graphics.deltaTime, 1 / 30f))
		stage.draw()
	}

	override fun pause() {

	}

	override fun resume() {

	}

	override fun resize(width: Int, height: Int) {

	}

	override fun dispose() {

	}

}
