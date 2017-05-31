package com.github.emulio.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.ControllerListener
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.controllers.PovDirection
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.github.emulio.Emulio
import com.github.emulio.model.Game
import com.github.emulio.model.Platform
import com.github.emulio.model.theme.ViewImage
import com.github.emulio.runners.GameScanner
import com.github.emulio.ui.reactive.GdxScheduler
import com.github.emulio.utils.gdxutils.Subscribe
import com.github.emulio.utils.gdxutils.glClearColor
import io.reactivex.schedulers.Schedulers
import mu.KotlinLogging

class PlatformsScreen(val emulio: Emulio): Screen, InputProcessor, ControllerListener {


	val logger = KotlinLogging.logger { }

	val stage: Stage = Stage()
	val lbLoading: Label

	init {

		Controllers.addListener(this)


		val inputMultiplexer = InputMultiplexer()
		inputMultiplexer.addProcessor(stage)
		inputMultiplexer.addProcessor(this)

		Gdx.input.inputProcessor = stage


		
		val platform = emulio.platforms[0]
		val platformTheme = emulio.theme[platform]!!
		
		val systemView = checkNotNull(platformTheme.getViewByName("system"), { "System tag of theme ${platform.platformName} not found." })
		val background = systemView.getItemByName("background")!! as ViewImage
		
		val backgroundTexture = Texture(FileHandle(background.path!!))
		
		

		val table = Table()
		table.setFillParent(true)

		val imgLogo = Image(backgroundTexture)//Image(Texture("images/logo-small.png"))
		table.add(imgLogo)


		val generator = FreeTypeFontGenerator(Gdx.files.internal("fonts/FrancoisOne-Regular.ttf"))
		val francoisFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
			size = 12
			color = Color(0x37424AFF)
		})

		lbLoading = Label("Initializing main interface", Label.LabelStyle().apply {
			font = francoisFont
		})
		lbLoading.setPosition(10f, 5f)

		stage.addActor(table)
		stage.addActor(lbLoading)

		// load main configurations/games and all stuff.. from mongo?

		lbLoading.setText("TOOOOOLS")

		observeGameScanner(emulio.platforms)

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

	private fun observeGameScanner(platforms: List<Platform>) {
		var count = 0

		val start = System.currentTimeMillis()

		val gamesMap = mutableMapOf<Platform, MutableList<Game>>()

		GameScanner(platforms)
				.fullScan()
				.subscribeOn(Schedulers.computation())
				.observeOn(GdxScheduler)
				.Subscribe(
						onNext = { game ->
							lbLoading.setText("Reading game $count (${game.platform.platformName})")
							count++

							val games = gamesMap[game.platform]
							if (games == null) {
								gamesMap[game.platform] = mutableListOf(game)
							} else {
								games.add(game)
							}
						},
						onError = { ex ->
							onError(ex)
						},
						onComplete = {
							lbLoading.setText("All games read: $count in ${System.currentTimeMillis() - start}ms")

							emulio.games = gamesMap


						})
	}

	private fun onError(exception: Throwable) {

		lbLoading.setText(exception.message ?: "An internal error have occurred, please check your configuration files.")
		lbLoading.setPosition(10f, 20f)

		logger.error(exception, { "An internal error have occurred, please check your configuration files." })
		// Exit app on keypress?
	}

	override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun keyTyped(character: Char): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun scrolled(amount: Int): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun keyUp(keycode: Int): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun keyDown(keycode: Int): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun connected(controller: Controller?) {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun buttonUp(controller: Controller?, buttonCode: Int): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun ySliderMoved(controller: Controller?, sliderCode: Int, value: Boolean): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun accelerometerMoved(controller: Controller?, accelerometerCode: Int, value: Vector3?): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun axisMoved(controller: Controller?, axisCode: Int, value: Float): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun disconnected(controller: Controller?) {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun xSliderMoved(controller: Controller?, sliderCode: Int, value: Boolean): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun povMoved(controller: Controller?, povCode: Int, value: PovDirection?): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun buttonDown(controller: Controller?, buttonCode: Int): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

}
