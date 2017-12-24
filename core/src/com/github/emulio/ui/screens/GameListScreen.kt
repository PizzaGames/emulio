package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
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
		
		
		buildBasicView(theme.findView("basic")!!)
		//buildDetailedView(theme.findView("detailed")!!)
		
		

	}

    private lateinit var listView: List<String>
    private lateinit var listScrollPane: ScrollPane

    private fun buildBasicView(basicView: View) {
		buildCommonComponents(basicView)
		
		val gamelistView = basicView.findViewItem("gamelist") as TextList
        listView = buildBasicList(gamelistView)

        listScrollPane = ScrollPane(listView, Skin()).apply {

            setFlickScroll(true)
            setScrollBarPositions(false, true)

            setForceScroll(false, true)
            setSmoothScrolling(true)
            isTransform = true

            setSize(gamelistView)
            setPosition(gamelistView)
        }






        stage.addActor(listScrollPane)
		
	}
	
	
	
	private fun buildCommonComponents(view: View) {
		val backgroundView = view.findViewItem("background") as ViewImage?
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
		
		val footer = view.findViewItem("footer") as ViewImage?
		if (footer != null) {
			stage.addActor(buildImage(footer, Scaling.stretch))
		}
		
		val header = view.findViewItem("header") as ViewImage?
		if (header != null) {
			stage.addActor(buildImage(header, Scaling.stretch))
		}
		
		initRoot()
		initLogoSmall()
		
		val systemName1 = view.findViewItem("system_name_1")?.let { it as Text }
		if (systemName1 != null) {
			stage.addActor(buildTextField(systemName1))
		}
		
		val systemName2 = view.findViewItem("system_name_2")?.let { it as Text }
		if (systemName2 != null) {
			stage.addActor(buildTextField(systemName2))
		}
		
		val logo = view.findViewItem("logo") as ViewImage?
		if (logo != null) {
			stage.addActor(buildImage(logo))
		}
	}
	
	private fun buildDetailedList(textList: TextList): List<Game> {
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
	
	private fun buildBasicList(gamelistView: TextList): List<String> {
		
		return List<String>(List.ListStyle().apply {

            //FIXME apparently there is a problem related with ttf fonts?
            fontColorUnselected = Color.WHITE//getColor(gamelistView.primaryColor)
            fontColorSelected = Color.WHITE//getColor(gamelistView.selectedColor)

            font = getFont(
                    getFontPath(gamelistView),
                    getFontSize(gamelistView.fontSize),
                    getColor(gamelistView.primaryColor))

			val selectorTexture = createColorTexture(0x393a3bFF)
			selection = TextureRegionDrawable(TextureRegion(selectorTexture))

		}).apply {
            setSize(gamelistView)


            //setBounds(20f, 400f, screenWidth * 0.8f, 200f)


            gamelistView.forceUpperCase
            games.forEach { game ->
                items.add(game.name ?: game.path.name)
            }

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
			
			val offsetY = when (originY) {
                0f -> 0f
                1f -> height
                else -> height * (1f - viewItem.originY!!)
            }
			
			setOrigin(offsetX, offsetY)

			x += offsetX
			y += offsetY
		}
	}
	
	private fun Actor.setSize(viewItem: ViewItem) {
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
	
	private fun Actor.setPosition(view: ViewItem) {
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
	
	private fun getFont(textView: Text): BitmapFont =
            getFont(getFontPath(textView), getFontSize(textView.fontSize), getColor(textView.textColor ?: textView.color))

	private fun getFontPath(textView: Text): FileHandle {
        return if (textView.fontPath != null) {
            FileHandle(textView.fontPath!!.absolutePath)
        } else{
            Gdx.files.internal("fonts/RopaSans-Regular.ttf")
        }
	}

	private fun getFontSize(fontSize: Float?): Int {
        return if (fontSize == null) {
            30
        } else {
            (fontSize * screenHeight).toInt()
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
        selectPrevious()
		return true
	}

    private fun selectPrevious() {
        val prevIndex = listView.selectedIndex - 1
        if (prevIndex < 0) {
            listView.selectedIndex = listView.items.size - 1
        } else {
            listView.selectedIndex = prevIndex
        }
    }

    private fun selectNext() {
        val nextIndex = listView.selectedIndex + 1
        if (nextIndex >= listView.items.size) {
            listView.selectedIndex = 0
        } else {
            listView.selectedIndex = nextIndex
        }
    }

    override fun onDownButton(): Boolean {
        selectNext()
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