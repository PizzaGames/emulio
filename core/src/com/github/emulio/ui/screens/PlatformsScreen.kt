package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.emulio.Emulio
import com.github.emulio.model.Game
import com.github.emulio.model.Platform
import com.github.emulio.model.theme.View
import com.github.emulio.model.theme.ViewImage
import com.github.emulio.runners.GameScanner
import com.github.emulio.ui.input.InputListener
import com.github.emulio.ui.input.InputManager
import com.github.emulio.ui.reactive.GdxScheduler
import com.github.emulio.utils.gdxutils.Subscribe
import com.github.emulio.utils.gdxutils.glClearColor
import io.reactivex.schedulers.Schedulers
import mu.KotlinLogging


class PlatformsScreen(val emulio: Emulio): Screen, InputListener {


	val logger = KotlinLogging.logger { }

	val stage: Stage = Stage()
	lateinit var lbLoading: Label

	val inputController: InputManager = InputManager(this, emulio.config)


	private var whitePixmap: Pixmap
	private var whiteTexture: Texture
	private var grayPixmap: Pixmap
	private var grayTexture: Texture
	private var outlineFont: BitmapFont
	private var mainFont: BitmapFont

	private var currentIdx: Int = 0

	init {

		Gdx.input.inputProcessor = stage

		whitePixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
		whitePixmap.setColor(0xFFFFFFDD.toInt())
		whitePixmap.fillRectangle(0, 0, 1, 1)
		whiteTexture = Texture(whitePixmap)
		whitePixmap.dispose()

		grayPixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
		grayPixmap.setColor(0xCCCCCCDD.toInt())
		grayPixmap.fillRectangle(0, 0, 1, 1)
		grayTexture = Texture(grayPixmap)
		grayPixmap.dispose()

		val generator = FreeTypeFontGenerator(Gdx.files.internal("fonts/FrancoisOne-Regular.ttf"))
		outlineFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
			size = 16
			color = Color.WHITE
			borderColor = Color.BLACK
			borderWidth = 1f
		})

		mainFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
			size = 20
			color = Color.BLACK
			borderColor = Color.GRAY
			borderWidth = 1f
		})


		stage.addActor(getPlatformByIndex(currentIdx))

		observeGameScanner(emulio.platforms)

	}

	private fun getPlatformByIndex(nextIdx: Int): Table {
		val size = emulio.platforms.size

		this.currentIdx = if (nextIdx == size) {
			0
		} else if (nextIdx == -1) {
			size - 1
		} else {
			nextIdx
		}

		val idx = this.currentIdx
		val nextIdx = if (idx == size - 1) {
			0
		} else {
			idx + 1
		}
		val prevIdx = if (idx == 0) {
			size - 1
		} else {
			idx - 1
		}

		val platform = emulio.platforms[idx]
		val nextPlatform = emulio.platforms[nextIdx]
		val previousPlatform = emulio.platforms[prevIdx]

		return getPlatformTable(platform, nextPlatform, previousPlatform)
	}

	private fun getPlatformTable(platform: Platform, nextPlatform: Platform, previousPlatform: Platform): Table {
		val platformTheme = getTheme(platform)
		val nextPlatformTheme = getTheme(nextPlatform)
		val previousPlatformTheme = getTheme(previousPlatform)

		val systemView = checkNotNull(platformTheme.getViewByName("system"), { "System tag of theme ${platform.platformName} not found." })
		val nextSystemView = checkNotNull(nextPlatformTheme.getViewByName("system"), { "System tag of theme ${platform.platformName} not found." })
		val previeousSystemView = checkNotNull(previousPlatformTheme.getViewByName("system"), { "System tag of theme ${platform.platformName} not found." })

		val background = systemView.getItemByName("background")!! as ViewImage
		val backgroundTexture = Texture(FileHandle(background.path!!))

		val backgroundImage = Image(backgroundTexture)
		stage.addActor(backgroundImage)

		val root = Table()
		root.setFillParent(true)
		root.add(Image(Texture("images/logo-small.png"))).expand().top().right().pad(10f)

		lbLoading = Label("Initializing main interface", Label.LabelStyle().apply {
			font = outlineFont
		})


		root.row()
		// platforms
		val platformTable = Table()
		platformTable.background(TextureRegionDrawable(TextureRegion(whiteTexture)))

		//FIXME mudar calculo e mecanica de resize para ficar fora do table, e utilizar cubic?
//		platformTable.add(Image(Texture(FileHandle(getLogo(previeousSystemView))))).maxWidth(100f).expandX()
		platformTable.add(Image(Texture(FileHandle(getLogo(systemView))))).maxHeight(150f).expandX()
//		platformTable.add(Image(Texture(FileHandle(getLogo(nextSystemView))))).maxWidth(100f).expandX()

		root.add(platformTable).expandX().fillX().height(200f)

		root.row()
		// gamecount
		val gameCount = Table()
		gameCount.background(TextureRegionDrawable(TextureRegion(grayTexture)))
		gameCount.add(Label("9999 games found", Label.LabelStyle().apply {
			font = mainFont
		}))
		root.add(gameCount).expandX().fillX().height(50f)

		root.row()
		root.add(lbLoading).expand().bottom().right().pad(10f)
		return root
	}

	private fun getLogo(systemView: View) = getLogoFromSystem(systemView).path

	private fun getLogoFromSystem(systemView: View) = systemView.getItemByName("logo")!! as ViewImage

	private fun getTheme(platform: Platform) = emulio.theme[platform]!!

	override fun hide() {

	}

	override fun show() {

	}

	override fun render(delta: Float) {
		Gdx.gl.glClearColor(0x6F, 0xBB, 0xDB, 0xFF)
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

	override fun onConfirmButton(): Boolean {
		lbLoading.setText("KeyPressed ${System.currentTimeMillis()}")
		return true
	}

	override fun onCancelButton(): Boolean {
		lbLoading.setText("KeyPressed ${System.currentTimeMillis()}")
		return true
	}

	override fun onUpButton(intensity: Float): Boolean {
		lbLoading.setText("KeyPressed ${System.currentTimeMillis()}")
		return true
	}

	override fun onDownButton(intensity: Float): Boolean {
		lbLoading.setText("KeyPressed ${System.currentTimeMillis()}")
		return true
	}

	override fun onLeftButton(intensity: Float): Boolean {
		stage.clear()
		stage.addActor(getPlatformByIndex(currentIdx - 1))
		return true
	}

	override fun onRightButton(intensity: Float): Boolean {
		stage.clear()
		stage.addActor(getPlatformByIndex(currentIdx + 1))
		return true
	}

	override fun onFindButton(): Boolean {
		lbLoading.setText("KeyPressed ${System.currentTimeMillis()}")
		return true
	}

	override fun onOptionsButton(): Boolean {
		lbLoading.setText("KeyPressed ${System.currentTimeMillis()}")
		return true
	}

	override fun onSelectButton(): Boolean {
		lbLoading.setText("KeyPressed ${System.currentTimeMillis()}")
		return true
	}

	override fun onPageUpButton(intensity: Float): Boolean {
		lbLoading.setText("KeyPressed ${System.currentTimeMillis()}")
		return false
	}

	override fun onPageDownButton(intensity: Float): Boolean {
		lbLoading.setText("KeyPressed ${System.currentTimeMillis()}")
		return false
	}

	override fun onExitButton(): Boolean {
		lbLoading.setText("KeyPressed ${System.currentTimeMillis()}")
		return false
	}


	inner class Rectangle(x: Float, y: Float, width: Float, height: Float, color: Color) : Actor() {

		private var texture: Texture? = null

		init {
			createTexture(width.toInt(), height.toInt(), color)

			setX(x)
			setY(y)
			setWidth(width)
			setHeight(height)
		}

		private fun createTexture(width: Int, height: Int, color: Color) {
			val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)
			pixmap.setColor(color)
			pixmap.fillRectangle(0, 0, width, height)
			texture = Texture(pixmap)
			pixmap.dispose()
		}

		override fun draw(batch: Batch?, parentAlpha: Float) {
			val color = color
			batch!!.setColor(color.r, color.g, color.b, color.a * parentAlpha)
			batch.draw(texture, x, y, width, height)
		}
	}

}
