package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
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
		
		val theme = emulio.theme[platform]!!
		
		
		buildBasicView(theme.getViewByName("basic")!!)
		//buildDetailedView(theme.getViewByName("detailed")!!)
		
		

	}
	
	private fun buildBasicView(basicView: View) {
		buildCommonComponents(basicView)
		
		val gamelistView = basicView.getItemByName("gamelist") as TextList
		stage.addActor(buildTextList(gamelistView))
		
	}
	
	
	
	private fun buildCommonComponents(view: View) {
		val backgroundView = view.getItemByName("background") as ViewImage?
		if (backgroundView != null) {
			stage.addActor(buildImage(backgroundView).apply {
				setScaling(Scaling.stretch)
				setPosition(0f, 0f)
				setSize(screenWidth, screenHeight)
			})
		} else {
			val lightGrayTexture = createColorTexture(0xc5c6c7FF.toInt())
			stage.addActor(Image(lightGrayTexture).apply {
				setFillParent(true)
			})
		}
		
		val footer = view.getItemByName("footer") as ViewImage?
		if (footer != null) {
			stage.addActor(buildImage(footer, Scaling.stretch))
		}
		
		val header = view.getItemByName("header") as ViewImage?
		if (header != null) {
			stage.addActor(buildImage(header, Scaling.stretch))
		}
		
		initRoot()
		initLogoSmall()
		
		val systemName1 = view.getItemByName("system_name_1")?.let { it as Text }
		if (systemName1 != null) {
			stage.addActor(buildTextField(systemName1))
		}
		
		val systemName2 = view.getItemByName("system_name_2")?.let { it as Text }
		if (systemName2 != null) {
			stage.addActor(buildTextField(systemName2))
		}
		
		val logo = view.getItemByName("logo") as ViewImage?
		if (logo != null) {
			stage.addActor(buildImage(logo))
		}
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
	
	private fun buildImage(image: ViewImage, scaling: Scaling = Scaling.fit): Image {
		val texture = Texture(FileHandle(image.path!!), true)
		texture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap)

		return Image(texture).apply {
			setScaling(scaling)

			setSize(image)
			setPosition(image)

			setOrigin(image)
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
	
	private fun buildTextList(gamelistView: TextList): List<String> {
		
		return List<String>(List.ListStyle().apply {
			val fnt = getFont(gamelistView)
			font = fnt
			
			fontColorSelected = getColor(gamelistView.selectedColor)
			fontColorUnselected = getColor(gamelistView.primaryColor)
			//TODO secondary Color?
			
			val selectorTexture = createColorTexture(getColor(gamelistView.selectorColor).toIntBits())
			selection = TextureRegionDrawable(TextureRegion(selectorTexture))
		}).apply {
			setSize(screenWidth, 200f)
			setPosition(0f, 100f)
			setItems("Alo", "Mundo")
		}
	
	}
	
	private fun Widget.setOrigin(viewItem: ViewItem) {
		if (viewItem.originX != null && viewItem.originY != null) {
			val originX = viewItem.originX!!
			val originY = viewItem.originY!!
			
			val offsetX = if (originX == 0f) {
				0f
			} else {
				width * originX
			}
			
			val offsetY = if (originY == 0f) {
				0f
			} else if (originY == 1f) {
				height
			} else {
				height * (1f - viewItem.originY!!)
			}
			
			setOrigin(offsetX, offsetY)

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
			width = Math.min(width, screenWidth * viewItem.maxSizeX!!)
		}
		if (viewItem.maxSizeY != null) {
			height = Math.min(height, screenHeight * viewItem.maxSizeY!!)
		}
		setSize(width, height)
	}
	
	private fun Widget.setPosition(view: ViewItem) {
		val x = screenWidth * view.positionX!!
		val y = (screenHeight * (1f - view.positionY!!)) - height
		
		setPosition(x, y)
	}
	
	private fun  getTextFieldStyle(textView: Text): TextField.TextFieldStyle {
		return TextField.TextFieldStyle().apply {
			val fnt = getFont(textView)
			font = fnt
			fontColor = Color(fnt.color)
		}
	}
	
	private fun getFont(textView: Text): BitmapFont {
		return getFont(getFontPath(textView), getFontSize(textView.fontSize), getColor(textView.textColor ?: textView.color))
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
			y = (height / 2) - 5f
			addAction(Actions.moveTo(screenWidth - width - 15f, y, 0.5f, interpolation))
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