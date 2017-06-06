package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.github.emulio.Emulio
import com.github.emulio.model.Platform
import com.github.emulio.ui.input.InputListener
import com.github.emulio.ui.input.InputManager

class GameListScreen(emulio: Emulio, val platform: Platform) : EmulioScreen(emulio), InputListener {

	private val inputController: InputManager = InputManager(this, emulio.config, stage)

	private val interpolation = Interpolation.fade

	private lateinit var root: Group
	private lateinit var logo: Image

	init {
		Gdx.input.inputProcessor = inputController
		initGUI()
	}

	private fun initGUI() {
		val blackTexture = createColorTexture(0x000000FF)
		stage.addActor(Image(blackTexture).apply {
			setFillParent(true)
		})

		initRoot()
		initLogoSmall()

	}

	private fun initRoot() {
		root = Group().apply {
			width = screenWidth
			height = screenHeight
			x = 0f
			y = 0f
		}
		stage.addActor(root)
	}

	private fun initLogoSmall() {
		logo = Image(Texture("images/logo-small.png")).apply {
			x = screenWidth
			y = screenHeight - height - 20f
			addAction(Actions.moveTo(screenWidth - width - 20f, y, 0.5f, interpolation))
		}
		root.addActor(logo)
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
		super.dispose()
		inputController.dispose()
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