package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Scaling
import com.github.emulio.Emulio
import com.github.emulio.model.Game
import com.github.emulio.model.Platform
import com.github.emulio.model.theme.*
import com.github.emulio.ui.input.InputListener
import com.github.emulio.ui.input.InputManager

class GameListScreen(emulio: Emulio, val platform: Platform) : EmulioScreen(emulio), InputListener {

	private val inputController: InputManager = InputManager(this, emulio.config, stage)

	private val interpolation = Interpolation.fade

	private lateinit var root: Group
	private lateinit var logo: Image
	
	private var games: kotlin.collections.List<Game>
	
	init {
		Gdx.input.inputProcessor = inputController
		
		games = emulio.games!![platform]?.toList() ?: emptyList<Game>()
		
		initGUI()
	}

	private fun initGUI() {
		//TODO read the header, footer, background
		val lightGrayTexture = createColorTexture(0xc5c6c7FF.toInt())
		stage.addActor(Image(lightGrayTexture).apply {
			setFillParent(true)
		})

		initRoot()
		//initLogoSmall()

		val theme = emulio.theme[platform]!!
		val basicView = theme.getViewByName("basic")!!

		val systemName1 = basicView.getItemByName("system_name_1") as Text
		stage.addActor(buildTextField(systemName1))
		
		val systemName2 = basicView.getItemByName("system_name_2") as Text
		stage.addActor(buildTextField(systemName2))

		val logo = basicView.getItemByName("logo") as ViewImage
		stage.addActor(buildImage(logo))
		
//		val darkGrayTexture = createColorTexture(0x97999BFF.toInt())
//		stage.addActor(Image(darkGrayTexture).apply {
//			setFillParent(true)
//		})
		
//		val textList = basicView.getItemByName("gamelist") as TextList
//		stage.addActor(buildGameList(textList))

	}
	
	private fun buildGameList(textList: TextList): List<Game> {
		return List<Game>(List.ListStyle(
			getFont(textList),
			getColor(textList.selectedColor),
			getColor(textList.primaryColor),
			null
		)).apply {

			val g = games.toTypedArray()

		}
	}
	
	private fun buildImage(logo: ViewImage): Image {
		val texture = Texture(FileHandle(logo.path!!), true)

		return Image(texture).apply {
			setScaling(Scaling.fill)

			setSize(logo)
			setPosition(logo)
			setOrigin(logo)
		}
	}
	
	private fun buildTextField(textView: Text): TextField {

		val text = if (textView.forceUpperCase) {
			textView.text?.toUpperCase() ?: ""
		} else {
			textView.text ?: ""
		}

		return TextField(text, getTextFieldStyle(textView)).apply {
			setAlignment(when(textView.alignment) {
				TextAlignment.LEFT -> Align.left
				TextAlignment.RIGHT -> Align.right
				TextAlignment.CENTER -> Align.center
				TextAlignment.JUSTIFY -> Align.left //TODO
			})
			setSize(textView)
			setPosition(textView)
		}
	}
	
	private fun Widget.setOrigin(viewItem: ViewItem) {
		if (viewItem.originX != null && viewItem.originY != null) {
			val offsetX = width * viewItem.originX!!
			val offsetY = height * (1f - viewItem.originY!!)
//			setOrigin(offsetX, offsetY)

			x += offsetX
			y += offsetY
		}
	}
	
	private fun Widget.setSize(viewItem: ViewItem) {
		var width = if (viewItem.sizeX != null) {
			screenWidth * viewItem.sizeX!!
		} else {
			this.width
		}

		var height = if (viewItem.sizeY != null) {
			screenHeight * viewItem.sizeY!!
		} else {
			this.height
		}
		
		if (viewItem.maxSizeX != null) {
			width = Math.max(width, screenWidth * viewItem.maxSizeX!!)
		}
		if (viewItem.maxSizeY != null) {
			height = Math.max(height, screenHeight * viewItem.maxSizeY!!)
		}
		setSize(width, height)
	}
	
	private fun Widget.setPosition(view: ViewItem) {
		val x = screenWidth * view.positionX!!
		val y = (screenHeight * (1f - view.positionY!!)) - height
		
		setPosition(x, y)
	}
	
	private fun  getTextFieldStyle(textView: Text): TextField.TextFieldStyle {
		return TextField.TextFieldStyle(
				getFont(textView),
				getColor(textView),
				null, null, null)
	}
	
	private fun getColor(textView: Text) = getColor(textView.color ?: "000000FF")
	
	private fun getFont(textView: Text): BitmapFont {
		return getFont(getFontPath(textView), getFontSize(textView.fontSize), getColor(textView.color))
	}

	private fun getFontPath(textView: Text): FileHandle {
		if (textView.fontPath != null) {
			return FileHandle(textView.fontPath!!.absolutePath)
		} else{
			return Gdx.files.internal("fonts/RopaSans-Regular.ttf")
		}
	}

	private fun getFontSize(fontSize: Float?): Int {
		if (fontSize == null) {
			return 26
		} else {
			return (fontSize * screenHeight).toInt()
		}
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