package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Scaling
import com.github.emulio.Emulio
import com.github.emulio.model.Game
import com.github.emulio.model.Platform
import com.github.emulio.model.theme.HelpSystem
import com.github.emulio.model.theme.View
import com.github.emulio.model.theme.ViewImage
import com.github.emulio.runners.GameScanner
import com.github.emulio.ui.input.InputListener
import com.github.emulio.ui.input.InputManager
import com.github.emulio.ui.reactive.GdxScheduler
import com.github.emulio.utils.gdxutils.Subscribe
import io.reactivex.schedulers.Schedulers
import mu.KotlinLogging


class PlatformsScreen(emulio: Emulio, initialPlatform: Platform = emulio.platforms[0]): EmulioScreen(emulio), InputListener {

	val logger = KotlinLogging.logger { }

	private val inputController: InputManager

	private var whiteTexture: Texture
	private var grayTexture: Texture
	private var loadingFont: BitmapFont
	private var gameCountFont: BitmapFont

	private lateinit var lbCount: Label
	private lateinit var lbLoading: Label
	private lateinit var root: Group
	private lateinit var bgBlack: Image
	private lateinit var groupPlatforms: Group

	private var bgLastPlatform: Image? = null
	private var currentPlatform = initialPlatform
	private var currentIdx: Int

	private val interpolation = Interpolation.fade
	private val slideInterpolation = Interpolation.fade
	private val slideDuration = 0.3f
	private val platformDisabledAlpha = 0.15f

	private var widthPerPlatform = screenWidth / 3f
	private val expandWidth = screenWidth / 4f

	private val groupPlatformsHeight = screenHeight / 3.5f
	private var platformImages = arrayListOf<Image>()
	private var platformOriginalX = arrayListOf<Float>()

	private var platformOriginalWidth = arrayListOf<Float>()
	val paddingWidth = expandWidth / 3
	private var platformWidth = widthPerPlatform - paddingWidth * 2


	init {
		currentIdx = emulio.platforms.indexOf(initialPlatform)

		inputController = InputManager(this, emulio.config, stage)
		Gdx.input.inputProcessor = inputController

		whiteTexture = createColorTexture(0xFFFFFFDD.toInt())
		grayTexture = createColorTexture(0xCCCCCCDD.toInt())

		loadingFont = freeTypeFontGenerator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
			size = 16
			color = Color.WHITE
			color.a = 0.5f
		})

		gameCountFont = freeTypeFontGenerator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
			size = 26
			color = Color.BLACK
		})

		initGUI()
		updatePlatformBg(initialPlatform)

		if (currentIdx != 0) {
			groupPlatforms.x = -(widthPerPlatform * (currentIdx + 1))
		}

		updatePlatform(emulio.platforms, if (currentIdx == 0) { emulio.platforms.size - 1 } else { currentIdx - 1 }, currentIdx)

		if (emulio.games == null) {
			observeGameScanner(emulio.platforms)
		}
	}

	private fun initGUI() {
		initBlackBackground()

		initRoot()
		initLogoSmall()
		initLoading()

		initGroupPlatforms()
		initGroupPlatformCount()
	}

	private lateinit var groupCount: Table

	private fun initGroupPlatformCount() {
		groupCount = Table()
		groupCount.addActor(Image(grayTexture).apply {
			setFillParent(true)
		})

		val lbCount = Label("", Label.LabelStyle().apply {
			font = gameCountFont
		})

		groupCount.add(lbCount)

		groupCount.width = screenWidth
		groupCount.height = 50f
		groupCount.color.a = 0f

		groupCount.y = groupPlatforms.y - groupCount.height
		groupCount.x = 0f

		this.lbCount = lbCount

		root.addActor(groupCount)
	}

	private fun updatePlatformBg(platform: Platform) {
		groupCount.color.a = 0f
		groupCount.clearActions()

		val text = if (emulio.games != null) {
			val gamesCount = emulio.games!![platform]?.size ?: 0
			if (gamesCount == 0) {
				"Loading..."
			} else {
				"$gamesCount games available"
			}
		} else {
			"Loading..."
		}
		lbCount.setText(text)

		val action = SequenceAction(
				Actions.delay(0.8f),
				Actions.fadeIn(0.5f, interpolation)
		)

		groupCount.addAction(action)

		val platformTheme = getTheme(platform)
		val systemView = checkNotNull(platformTheme.getViewByName("system"), { "System tag of theme ${platform.platformName} not found." })
		val background = systemView.getItemByName("background")!! as ViewImage
		val backgroundTexture = Texture(FileHandle(background.path!!))
		initBgPlatform(backgroundTexture)


		//TODO
//		val helpSystem = systemView.getItemByName("help")
//		if (helpSystem != null) {
//			helpSystem as HelpSystem
//
//			val textColor = helpSystem.textColor
//			if (textColor != null) {
//				val argb = Integer.parseInt(textColor, 16)
//				loadingFont.color = Color()
//			}
//		} else {
//			loadingFont.color = Color.WHITE
//		}



		groupPlatforms.zIndex = 10
		lbLoading.zIndex = 10
		logo.zIndex = 10
		groupCount.zIndex = 9
	}
	
	private fun initGroupPlatforms() {


		groupPlatforms = Group().apply {
			addActor(Image(whiteTexture).apply {
				setFillParent(true)
			})
			width = (emulio.platforms.size + 4) * widthPerPlatform
			height = groupPlatformsHeight
			x = -(widthPerPlatform)
			y = (screenHeight - height) / 2
		}

		var currentX = 0f

		currentX += initPlatform(emulio.platforms[Math.max(emulio.platforms.size - 2, 0)], widthPerPlatform, paddingWidth, currentX)
		currentX += initPlatform(emulio.platforms.last(), widthPerPlatform, paddingWidth, currentX)

		emulio.platforms.forEach { platform ->
			currentX += initPlatform(platform, widthPerPlatform, paddingWidth, currentX)
		}

		currentX += initPlatform(emulio.platforms[0], widthPerPlatform, paddingWidth, currentX)
		currentX += initPlatform(emulio.platforms[(if (emulio.platforms.size > 1) { 1 } else { 0 })], widthPerPlatform, paddingWidth, currentX)

		root.addActor(groupPlatforms)
	}
	
	private fun initPlatform(platform: Platform, widthPerPlatform: Float, paddingWidth: Float, currentX: Float): Float {
		val theme = getTheme(platform)
		val systemView = checkNotNull(theme.getViewByName("system"), { "System tag of theme ${platform.platformName} not found. please check your theme files." })
		
		val image = getImageFromSystem(systemView, 1f).apply {
			//height = Math.max(groupPlatforms.height - paddingHeight, height)
			width = platformWidth //widthPerPlatform - paddingWidth * 2

			color.a = platformDisabledAlpha

			x = currentX + ((widthPerPlatform - width) / 2)
			y = (groupPlatforms.height - height) / 2

			name = platform.platformName

			setScaling(Scaling.fit)
		}

		if (image.height > groupPlatformsHeight - 20f) {
			image.apply {
				height = groupPlatformsHeight - 20f
				x = currentX + ((widthPerPlatform - width) / 2)
				y = (groupPlatforms.height - height) / 2
			}
		}
		
		platformImages.add(image)
		platformOriginalX.add(image.x)
		platformOriginalWidth.add(image.width)

		groupPlatforms.addActor(image)

		return widthPerPlatform
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

	private fun initLoading() {
		lbLoading = Label("", Label.LabelStyle().apply {
			font = loadingFont
		})

		lbLoading.x = 20f
		lbLoading.y = 20f
		root.addActor(lbLoading)
	}

	private lateinit var logo: Image

	private fun initLogoSmall() {
		logo = Image(Texture("images/logo-small.png")).apply {
			x = screenWidth
			y = screenHeight - height - 20f
			addAction(Actions.moveTo(screenWidth - width - 20f, y, 0.5f, interpolation))
		}
		root.addActor(logo)
	}

	private fun initBgPlatform(backgroundTexture: Texture) {
		if (bgLastPlatform != null) {
			val bg = bgLastPlatform!!
			bg.addAction(SequenceAction(Actions.fadeOut(0.5f, interpolation), Actions.run { bg.remove() }))
		}

		val bgPlatform = Image(backgroundTexture).apply {
			setScaling(Scaling.fill)
			setFillParent(true)
			color.a = 0f
			addAction(Actions.fadeIn(0.5f, interpolation))
			zIndex = 0
		}
		root.addActor(bgPlatform)
		bgLastPlatform = bgPlatform
	}

	private fun initBlackBackground() {
		bgBlack = Image(createColorTexture(0x000000FF)).apply {
			x = 0f
			y = 0f
			height = screenHeight
			width = screenWidth
			zIndex = 0
		}
		stage.addActor(bgBlack)
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

	private fun observeGameScanner(platforms: List<Platform>) {

		val gamesMap = mutableMapOf<Platform, MutableList<Game>>()
		emulio.games = gamesMap

		GameScanner(platforms)
				.fullScan()
				.subscribeOn(Schedulers.computation())
				.observeOn(GdxScheduler)
				.Subscribe(
						onNext = { game ->
							lbLoading.setText("Scanning games from ${game.platform.platformName.capitalize()}")

							var games = gamesMap[game.platform]
							if (games == null) {
								games = mutableListOf(game)
								gamesMap[game.platform] = games
							} else {
								games.add(game)
							}

							if (currentPlatform == game.platform) {

								val size = games.size
								val text = if (size == 0) {
									"No games available"
								} else {
									"$size games available"
								}
								lbCount.setText(text)
							}
						},
						onError = { ex ->
							onError(ex)
						},
						onComplete = {
							lbLoading.addAction(Actions.fadeOut(0.5f))

						})
	}

	private fun onError(exception: Throwable) {

		lbLoading.setText(exception.message ?: "An internal error have occurred, please check your configuration files.")
		lbLoading.setPosition(10f, 20f)

		logger.error(exception, { "An internal error have occurred, please check your configuration files." })
		// Exit app on keypress?
	}

	override fun onConfirmButton(): Boolean {
		switchScreen(GameListScreen(emulio, currentPlatform))
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

	private fun updatePlatform(platforms: List<Platform>, lastIdx: Int, currentIndex: Int) {
		val children = platformImages
		val currentPlatform = platforms[this.currentIdx]
		
		children.forEachIndexed { i, image ->
			val originalX = platformOriginalX[i]
			val originalWidth = platformOriginalWidth[i]
			if (image.name == currentPlatform.platformName) {
				image.x = originalX - (expandWidth / 2f)
				image.width = Math.max((screenWidth / 3), originalWidth + expandWidth)
				image.color.a = 1f
			} else {
				image.x = originalX
				image.width = originalWidth
				image.color.a = platformDisabledAlpha
			}
		}

		this.currentPlatform = currentPlatform
	}

	private fun showPreviousPlatform() {
		val platforms = emulio.platforms

		val lastIdx = currentIdx

		if (currentIdx == 0) {
			currentIdx = platforms.size - 1

			updatePlatform(platforms, lastIdx, currentIdx)

			val widthPerPlatform = screenWidth / 3
			groupPlatforms.clearActions()
			groupPlatforms.x = -(widthPerPlatform * (platforms.size + 1))
			groupPlatforms.addAction(Actions.moveTo(-(widthPerPlatform * (platforms.size)), groupPlatforms.y, slideDuration, slideInterpolation))
		} else {
			currentIdx--

			updatePlatform(platforms, lastIdx, currentIdx)

			val widthPerPlatform = screenWidth / 3
			groupPlatforms.addAction(Actions.moveTo(-(widthPerPlatform * (currentIdx + 1)), groupPlatforms.y, slideDuration, slideInterpolation))
		}

		updatePlatformBg(currentPlatform)
	}
	
	private fun showNextPlatform() {
		val platforms = emulio.platforms
		val lastIdx = currentIdx
		
		if (currentIdx == platforms.size - 1) {
			currentIdx = 0

			updatePlatform(platforms, lastIdx, currentIdx)

			val widthPerPlatform = screenWidth / 3

			groupPlatforms.clearActions()
			groupPlatforms.x = 0f
			groupPlatforms.addAction(Actions.moveTo(-(widthPerPlatform), groupPlatforms.y, slideDuration, slideInterpolation))
		} else {
			currentIdx++

			updatePlatform(platforms, lastIdx, currentIdx)

			val widthPerPlatform = screenWidth / 3
			groupPlatforms.addAction(Actions.moveTo(-(widthPerPlatform * (currentIdx + 1)), groupPlatforms.y, slideDuration, slideInterpolation))
		}

		updatePlatformBg(currentPlatform)
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


