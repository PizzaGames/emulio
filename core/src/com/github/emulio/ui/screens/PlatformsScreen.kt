package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Scaling
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


class PlatformsScreen(emulio: Emulio, var initialPlatform: Platform = emulio.platforms[0]): EmulioScreen(emulio), InputListener {

	val logger = KotlinLogging.logger { }

	lateinit var lbLoading: Label

	val inputController: InputManager

	private var whitePixmap: Pixmap
	private var whiteTexture: Texture
	private var grayPixmap: Pixmap
	private var grayTexture: Texture
	private var outlineFont: BitmapFont
	private var mainFont: BitmapFont

	private var currentIdx: Int

	private var screenWidth: Int
	private var screenHeight: Int

	private var currentCountLabel: Label? = null
	private var currentPlatform = initialPlatform

	init {
		currentIdx = emulio.platforms.indexOf(initialPlatform)

		inputController = InputManager(this, emulio.config, stage)
		Gdx.input.inputProcessor = inputController

		screenWidth = Gdx.graphics.width
		screenHeight = Gdx.graphics.height

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

		setPlatformByIndex(currentIdx)
		observeGameScanner(emulio.platforms)
	}

	private fun setPlatformByIndex(nextIndex: Int) {
		val size = emulio.platforms.size

		this.currentIdx = if (nextIndex == size) {
			0
		} else if (nextIndex == -1) {
			size - 1
		} else {
			nextIndex
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

		this.currentPlatform = emulio.platforms[currentIdx]

		setPlatformTableByIndex(idx, nextIdx, prevIdx)
	}

	private fun setPlatformTableByIndex(idx: Int, nextIdx: Int, prevIdx: Int) {
		val platform = emulio.platforms[idx]
		val nextPlatform = emulio.platforms[nextIdx]
		val previousPlatform = emulio.platforms[prevIdx]

		setPlatformTable(platform, nextPlatform, previousPlatform)
	}

	private fun setPlatformTable(platform: Platform, nextPlatform: Platform, previousPlatform: Platform) {
		if (initialPlatform == platform) {
			// incomplete animations
		} else {
			stage.clear()
		}

		val platformTheme = getTheme(platform)
		val nextPlatformTheme = getTheme(nextPlatform)
		val previousPlatformTheme = getTheme(previousPlatform)

		val systemView = checkNotNull(platformTheme.getViewByName("system"), { "System tag of theme ${platform.platformName} not found." })
		val nextSystemView = checkNotNull(nextPlatformTheme.getViewByName("system"), { "System tag of theme ${platform.platformName} not found." })
		val previousSystemView = checkNotNull(previousPlatformTheme.getViewByName("system"), { "System tag of theme ${platform.platformName} not found." })

		val background = systemView.getItemByName("background")!! as ViewImage
		val backgroundTexture = Texture(FileHandle(background.path!!))


		val root = Table()
		root.setFillParent(true)

		val drawable = TextureRegionDrawable(TextureRegion(backgroundTexture))
		root.background(drawable)

		val logoSmall = Image(Texture("images/logo-small.png"))
		root.add(logoSmall).expand().top().right().pad(10f)

		lbLoading = Label("", Label.LabelStyle().apply {
			font = outlineFont
		})


		root.row()
		// platforms
		val platformTable = Table()
		platformTable.background(TextureRegionDrawable(TextureRegion(whiteTexture)))

		val maxWidth = (screenWidth / 3f) - 200f
		val barHeight = screenHeight / 4f

		platformTable.add(getImageFromSystem(previousSystemView, 0.3f)).width(maxWidth).expandX()
		platformTable.add(getImageFromSystem(systemView, 1f)).width(screenWidth / 3f + 200f).maxHeight(barHeight - 20f).expandX()
		platformTable.add(getImageFromSystem(nextSystemView, 0.3f)).width(maxWidth).expandX()

		root.add(platformTable).expandX().fillX().height(barHeight)

		root.row()
		// gamecount
		val gameCount = Table()
		gameCount.background(TextureRegionDrawable(TextureRegion(grayTexture)))

		val gamesCount = emulio.games?.get(platform)?.size ?: 0
		currentCountLabel = Label("$gamesCount games found", Label.LabelStyle().apply {
			font = mainFont
		})
		gameCount.add(currentCountLabel)

		root.add(gameCount).expandX().fillX().height(50f)

		root.row()
		root.add(lbLoading).expand().bottom().right().pad(10f)

		stage.addActor(root)

	}
	
	private fun getImageFromSystem(systemView: View, alpha: Float): Image {
		val texture = Texture(FileHandle(getLogo(systemView)), true)
		texture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap)
		
		val image = Image(texture)
		image.color.a = alpha
		image.setScaling(Scaling.fit)
		
		return image
	}
	
	private fun getLogo(systemView: View) = getLogoFromSystem(systemView).path

	private fun getLogoFromSystem(systemView: View) = systemView.getItemByName("logo")!! as ViewImage

	private fun getTheme(platform: Platform) = emulio.theme[platform]!!

	override fun hide() {

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
		inputController.dispose()
	}

	private fun observeGameScanner(platforms: List<Platform>) {
		var count = 0

		val start = System.currentTimeMillis()

		val gamesMap = mutableMapOf<Platform, MutableList<Game>>()
		emulio.games = gamesMap

		GameScanner(platforms)
				.fullScan()
				.subscribeOn(Schedulers.computation())
				.observeOn(GdxScheduler)
				.Subscribe(
						onNext = { game ->
							lbLoading.setText("Reading game $count (${game.platform.platformName})")
							count++

							var games = gamesMap[game.platform]
							if (games == null) {
								games = mutableListOf(game)
								gamesMap[game.platform] = games
							} else {
								games.add(game)
							}

							if (currentPlatform == game.platform && currentCountLabel != null) {
								currentCountLabel!!.setText("${games!!.size} games found")
							}
						},
						onError = { ex ->
							onError(ex)
						},
						onComplete = {
							lbLoading.setText("Games scanned: $count in ${System.currentTimeMillis() - start}ms")

						})
	}

	private fun onError(exception: Throwable) {

		lbLoading.setText(exception.message ?: "An internal error have occurred, please check your configuration files.")
		lbLoading.setPosition(10f, 20f)

		logger.error(exception, { "An internal error have occurred, please check your configuration files." })
		// Exit app on keypress?
	}

	override fun onConfirmButton(): Boolean {
		lbLoading.setText("ConfirmButton ${System.currentTimeMillis()}")
		return true
	}

	override fun onCancelButton(): Boolean {
		lbLoading.setText("CancelButton ${System.currentTimeMillis()}")
		return true
	}

	override fun onUpButton(): Boolean {
		lbLoading.setText("UpButton ${System.currentTimeMillis()}")
		return true
	}

	override fun onDownButton(): Boolean {
		lbLoading.setText("DownButton ${System.currentTimeMillis()}")
		return true
	}

	override fun onFindButton(): Boolean {
		lbLoading.setText("onFindButton ${System.currentTimeMillis()}")
		return true
	}

	override fun onOptionsButton(): Boolean {
		lbLoading.setText("onOptionsButton ${System.currentTimeMillis()}")
		return true
	}

	override fun onSelectButton(): Boolean {
		lbLoading.setText("onSelectButton ${System.currentTimeMillis()}")
		return true
	}

	private fun showPreviousPlatform() {
//		stage.addActor(setPlatformByIndex(currentIdx - 1))
		setPlatformByIndex(currentIdx)
	}

	private fun showNextPlatform() {
		setPlatformByIndex(currentIdx + 1)
	}
	override fun onLeftButton(): Boolean {
		showPreviousPlatform()
		return true
	}

	override fun onRightButton(): Boolean {
		showNextPlatform()
		return true
	}

	override fun onPageUpButton(): Boolean {
		showPreviousPlatform()
		return false
	}

	override fun onPageDownButton(): Boolean {
		showNextPlatform()
		return false
	}

	override fun onExitButton(): Boolean {
		lbLoading.setText("onExitButton ${System.currentTimeMillis()}")
		return false
	}

}


