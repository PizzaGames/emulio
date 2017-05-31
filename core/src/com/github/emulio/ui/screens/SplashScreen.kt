package com.github.emulio.ui.screens

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
import com.github.emulio.model.EmulioConfig
import com.github.emulio.model.Game
import com.github.emulio.model.Platform
import com.github.emulio.model.theme.Theme
import com.github.emulio.runners.GameScanner
import com.github.emulio.runners.PlatformReader
import com.github.emulio.runners.ThemeReader
import com.github.emulio.ui.reactive.GdxScheduler
import com.github.emulio.utils.gdxutils.*
import com.github.emulio.yaml.YamlUtils
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import mu.KotlinLogging
import java.io.File


class SplashScreen(val emulio: Emulio) : Screen {

	val logger = KotlinLogging.logger { }

	lateinit var platforms: List<Platform>

	val stage: Stage
	val lbLoading: Label

	init {
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

		// load main configurations/games and all stuff.. from mongo?
		
		lbLoading.setText("Loading configurations")
		observeConfig()

	}
	
	private fun observeConfig() {
		val observable: Observable<EmulioConfig> = Observable.create({ subscriber ->
			val yamlUtils = YamlUtils()
			val configFile = File("emulio-config.yaml")
			
			if (!configFile.exists()) {
				yamlUtils.saveEmulioConfig(configFile, initializeEmulioConfig())
			}
			
			subscriber.onNext(yamlUtils.parseEmulioConfig(configFile))
			subscriber.onComplete()
		})
		
		observable
				.subscribeOn(Schedulers.computation())
				.observeOn(GdxScheduler)
				.subscribe({ config ->
					
					emulio.config = config
					
					lbLoading.setText("Loading platforms")
					observePlatforms()
				}, { onError(it) })
	}
	
	private fun initializeEmulioConfig(): EmulioConfig {
		return EmulioConfig().apply {
			loadDefaults()
		}
	}
	
	
	private fun observePlatforms() {
		val platformsObservable: Observable<List<Platform>> = Observable.create({ subscriber ->
			val platforms = PlatformReader().invoke()
			subscriber.onNext(platforms)
			subscriber.onComplete()
		})

		platformsObservable
				.subscribeOn(Schedulers.computation())
				.observeOn(GdxScheduler)
				.subscribe({
					onPlatformsLoaded(it)

				}, { onError(it) })
	}

	private fun onError(exception: Throwable) {

		lbLoading.setText(exception.message ?: "An internal error have occurred, please check your configuration files.")
		lbLoading.setPosition(10f, 20f)

		logger.error(exception, { "An internal error have occurred, please check your configuration files." })
		// Exit app on keypress?
	}

	fun onPlatformsLoaded(platforms: List<Platform>) {
		this.platforms = platforms
		emulio.platforms = platforms

		lbLoading.setText("Loading theme")

		val start = System.currentTimeMillis()

		val themesMap = mutableMapOf<Platform, Theme>()

		ThemeReader()
			.readTheme(platforms, File("../../sample-files/theme/simple"))
			.subscribeOn(Schedulers.computation())
			.observeOn(GdxScheduler)
			.Subscribe(
				onNext = { theme ->
					val platform = theme.platform!!
					val platformName = platform.platformName
					logger.debug { "theme read for platform '$platformName'" }
					lbLoading.setText("Loading theme for platform $platformName")
					
					
					
					themesMap.put(platform, theme)
				},
				onError =  { ex ->
					onError(ex)
				},
				onComplete = {
					logger.debug { "theme loaded in ${System.currentTimeMillis() - start}ms " }
					
					lbLoading.setText("Theme loaded")
					emulio.theme = themesMap

					emulio.screen = PlatformsScreen(emulio)
				})
	}



	override fun render(delta: Float) {
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

	override fun hide() {

	}

	override fun show() {

	}

	override fun pause() {

	}

	override fun resume() {

	}

}

