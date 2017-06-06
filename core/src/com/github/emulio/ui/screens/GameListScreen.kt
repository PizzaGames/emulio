package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.github.emulio.Emulio
import com.github.emulio.model.Platform
import com.github.emulio.ui.input.InputListener
import com.github.emulio.ui.input.InputManager

class GameListScreen(emulio: Emulio, val platform: Platform) : EmulioScreen(emulio), InputListener {

	private val inputController: InputManager = InputManager(this, emulio.config, stage)

	init {
		Gdx.input.inputProcessor = inputController
	}

	override fun hide() {
	}

	override fun render(delta: Float) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		stage.act(Math.min(Gdx.graphics.deltaTime, 1 / 30f))
		stage.draw()

		inputController.update(delta)
	}

	override fun pause() {
	}

	override fun resume() {
	}

	override fun resize(width: Int, height: Int) {
	}

	override fun dispose() {
	}

	override fun onConfirmButton(): Boolean {
		return true
	}

	override fun onCancelButton(): Boolean {
		switchScreen(PlatformsScreen(emulio, platform))
		return true
	}

	override fun onUpButton(): Boolean {
		return true
	}

	override fun onDownButton(): Boolean {
		return true
	}

	override fun onLeftButton(): Boolean {
		return true
	}

	override fun onRightButton(): Boolean {
		return true
	}

	override fun onFindButton(): Boolean {
		return true
	}

	override fun onOptionsButton(): Boolean {
		return true
	}

	override fun onSelectButton(): Boolean {
		return true
	}

	override fun onPageUpButton(): Boolean {
		return true
	}

	override fun onPageDownButton(): Boolean {
		return true
	}

	override fun onExitButton(): Boolean {
		return true
	}

}