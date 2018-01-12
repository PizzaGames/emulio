package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.github.emulio.Emulio
import com.github.emulio.ui.input.InputListener
import com.github.emulio.ui.input.InputManager
import com.github.emulio.utils.translate
import mu.KotlinLogging

class InputConfigScreen(emulio: Emulio, private val backCallback: () -> EmulioScreen, private val firstRun: Boolean = false) : EmulioScreen(emulio), InputListener {
    
    private val logger = KotlinLogging.logger { }

    private val inputController: InputManager = InputManager(this, emulio.config, this.stage)

    private val lbCurrent: Label
    private val lbNext: Label
    private val lbPrevious: Label
    private val imgNext: Image
    private val imgPrevious: Image
    private val imgController: Image

    private val controllerNames: List<String>
    private var currentIndex = 0

    init {
        Gdx.input.inputProcessor = inputController

        val populatedControllers = mutableListOf("Keyboard")

        Controllers.getControllers().forEach { controller ->
            populatedControllers.add(controller.name)
        }

        controllerNames = populatedControllers
        if (controllerNames.size > 1) {
            currentIndex = 1 // keyboard is not the default appareance
        }


        // we need to cache this font!
        val mainFont = FreeTypeFontGenerator(Gdx.files.internal("fonts/RopaSans-Regular.ttf")).generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = 40
            color = Color.WHITE
        })

        logger.debug { "initializing InputConfigScreen" }

        stage.addActor(Image(createColorTexture(0x6FBBDBFF)).apply {
            setFillParent(true)
        })

        stage.addActor(Image(Texture("images/logo-small.png")).apply {
            x = screenWidth
            y = screenHeight - height - 20f
            addAction(Actions.moveTo(screenWidth - width - 20f, y, 0.5f, Interpolation.fade))
        })

        val hudHeight = screenHeight * 0.065f
        initHelpHuds(0f, hudHeight)

        val lbScreenTitle = Label("Input configurations".translate(), emulio.skin, "title").apply {
            setPosition(10f, screenHeight - height - 10f)
        }
        stage.addActor(lbScreenTitle)

        stage.addActor(Image(createColorTexture(0xEFEFEFFF.toInt())).apply {
            width = screenWidth
            height = screenHeight - hudHeight - lbScreenTitle.height - 80f

            setPosition(0f, hudHeight)
        })

        imgController = Image(Texture(Gdx.files.internal(obtainImage(currentIndex)), true)).apply {
            x = screenWidth - width - 10f
            y = 70f

            color.a = 0f

            addAction(Actions.alpha(0.3f, 2f, Interpolation.fade))
        }
        stage.addActor(imgController)

        val root = Table().apply {
            width = screenWidth
            height = screenHeight - hudHeight - lbScreenTitle.height - 80f

            setPosition(0f, hudHeight)
        }

        stage.addActor(Image(createColorTexture(0x878787FF.toInt())).apply {
            width = screenWidth
            height = 20f

            setPosition(0f, root.y)
        })

        stage.addActor(Image(createColorTexture(0x878787FF.toInt())).apply {
            width = screenWidth
            height = 60f

            setPosition(0f, root.y + root.height)
        })

        lbCurrent = Label(controllerNames[currentIndex], Label.LabelStyle().apply {
            font = mainFont
            fontColor = Color.WHITE
        }).apply {
            width = screenWidth
            setAlignment(Align.center)
            setPosition(0f, root.y + root.height + 5f)
        }
        stage.addActor(lbCurrent)

        lbPrevious = Label(controllerNames[obtainPreviousIndex(currentIndex)], Label.LabelStyle().apply {
            font = mainFont
            fontColor = Color.WHITE
        }).apply {
            width = screenWidth
            setAlignment(Align.center)
            setPosition(-screenWidth, root.y + root.height + 5f)
        }
        stage.addActor(lbPrevious)

        lbNext = Label(controllerNames[obtainNextIndex(currentIndex)], Label.LabelStyle().apply {
            font = mainFont
            fontColor = Color.WHITE
        }).apply {
            width = screenWidth
            setAlignment(Align.center)
            setPosition(screenWidth, root.y + root.height + 5f)
        }
        stage.addActor(lbNext)

        imgPrevious = Image(Texture(Gdx.files.internal("images/previous.png"), true).apply {
            setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap)
        }).apply {
            width = 35f
            height = 35f

            x = 10f
            y = lbCurrent.y + 5f

            color.a = 0.4f

            setScaling(Scaling.stretch)

            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    onLeftButton()
                }
            })
        }
        stage.addActor(imgPrevious)

        imgNext = Image(Texture(Gdx.files.internal("images/next.png"), true).apply {
            setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap)
        }).apply {
            width = 35f
            height = 35f

            x = screenWidth - width - 10f
            y = lbCurrent.y + 5f

            color.a = 0.4f

            setScaling(Scaling.stretch)

            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    onRightButton()
                }
            })
        }
        stage.addActor(imgNext)

        stage.addActor(root)

    }

    private fun obtainImage(idx: Int): String {
        val name = controllerNames[idx]
        return if (name.toLowerCase().contains("keyboard")) {
            "images/controllers/keyboard.png"
        } else if (name.toLowerCase().contains("xbox")) {
            "images/controllers/xbox.png"
        } else {
            "images/controllers/snes.png"
        }

    }


    private fun obtainNextIndex(currentIndex: Int): Int {
        return if (currentIndex == controllerNames.size - 1) {
            0
        } else {
            currentIndex + 1
        }
    }

    private fun obtainPreviousIndex(currentIndex: Int): Int {
        return if (currentIndex == 0) {
            controllerNames.size - 1
        } else {
            currentIndex - 1
        }
    }

    override fun show() {
        super.show()

        Gdx.input.isCursorCatched = false

        if (firstRun) {
            showInfoDialog("Emulio detected that there is no input configured, please configure it in this screen, and go back when done.")
        }
    }

    private fun initHelpHuds(initialY: Float, height: Float) {

        val calculatedHeight = height * 0.55f

        val helpFont = freeTypeFontGenerator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = calculatedHeight.toInt()
            color = Color.WHITE
            color.a = 1f
        })

        // Calculate the size according resolution???
        val imgWidth = calculatedHeight
        val imgHeight = calculatedHeight
        val padding = 5f

        val lineHeight = helpFont.lineHeight

        val y = initialY - 2f + ((height - lineHeight) / 2)
        val imageY = (initialY - 2f + ((height - imgHeight) / 2)) + 2f

        val imgB = buildImage("images/resources/help/button_b_128_128.png", imgWidth, imgHeight, 10f, imageY)
        stage.addActor(imgB)
        val txtBack = buildText("Back".translate().toUpperCase(), helpFont, imgB.x + imgWidth + padding, y)
        stage.addActor(txtBack)

        val imgA = buildImage("images/resources/help/button_a_128_128.png", imgWidth, imgHeight, txtBack.x + txtBack.width + (padding * 3), imageY)
        stage.addActor(imgA)
        val txtSelect = buildText("Redefine".translate().toUpperCase(), helpFont, imgA.x + imgWidth + padding, y)
        stage.addActor(txtSelect)

        val imgDPadUpDown = buildImage("images/resources/help/dpad_leftright_128_128.png", imgWidth, imgHeight, txtSelect.x + txtSelect.width + (padding * 3), imageY)
        stage.addActor(imgDPadUpDown)
        val txtSystem = buildText("Navigate".translate().toUpperCase(), helpFont, imgDPadUpDown.x + imgWidth + padding, y)
        stage.addActor(txtSystem)

        val alpha = 0.4f
        val imgColor = Color.BLACK
        val txtColor = Color.BLACK

        imgB.color = imgColor
        imgB.color.a = alpha
        txtBack.color = txtColor
        txtBack.color.a = alpha
        imgA.color = imgColor
        imgA.color.a = alpha
        txtSelect.color = txtColor
        txtSelect.color.a = alpha
        imgDPadUpDown.color = imgColor
        imgDPadUpDown.color.a = alpha
        txtSystem.color = txtColor
        txtSystem.color.a = alpha

    }

    override fun release() {
        inputController.dispose()
    }

    override fun hide() {
        // do nothing
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(Math.min(Gdx.graphics.deltaTime, 1 / 30f))
        stage.draw()
        inputController.update(delta)
    }


    private fun selectPreviousController() {

        val previousIndex = obtainPreviousIndex(currentIndex)
        val name = controllerNames[previousIndex]

        lbPrevious.setText(name)

        animateSelector(imgPrevious, -5f)
        animateLabel(lbCurrent, -screenWidth, lbPrevious, name)
        animateController(obtainImage(previousIndex))

        currentIndex = previousIndex

    }

    private fun selectNextController() {

        val nextIndex = obtainNextIndex(currentIndex)
        val name = controllerNames[nextIndex]

        lbNext.setText(name)

        animateSelector(imgNext, 5f)
        animateLabel(lbCurrent, screenWidth, lbNext, name)
        animateController(obtainImage(nextIndex))

        currentIndex = nextIndex
    }

    private fun animateController(nextImage: String) {
        imgController.addAction(SequenceAction(
                Actions.fadeOut(0.2f),
                Actions.run {
                    val texture = Texture(Gdx.files.internal(nextImage), true)

                    texture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap)
                    imgController.drawable = TextureRegionDrawable(TextureRegion(texture))
                },
                Actions.alpha(0.4f, 0.2f)
        ))
    }

    private fun animateLabel(current: Label, x: Float, next: Label, newText: String) {
        current.addAction(Actions.moveBy(-x, 0f, 0.5f, Interpolation.fade))
        next.addAction(SequenceAction(
                Actions.moveBy(-x, 0f, 0.5f, Interpolation.fade),
                Actions.run {
                    current.moveBy(x, 0f)
                    next.moveBy(x, 0f)
                    current.setText(newText)
                }
        ))
    }

    private fun animateSelector(img: Image, amount: Float) {
        img.addAction(SequenceAction(
                Actions.moveBy(amount, 0f, 0.1f, Interpolation.fade),
                Actions.moveBy(-amount, 0f, 0.1f, Interpolation.fade)
        ))
    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun resize(width: Int, height: Int) {

    }

    override fun onConfirmButton(): Boolean {
        return true
    }

    override fun onCancelButton(): Boolean {
        stage.root.addAction(SequenceAction(Actions.fadeOut(1f, Interpolation.fade), Actions.run {
            switchScreen(backCallback.invoke())
        }))

        return true
    }

    override fun onUpButton(): Boolean {
        return true
    }

    override fun onDownButton(): Boolean {
        return true
    }

    override fun onLeftButton(): Boolean {
        selectPreviousController()
        return true
    }

    override fun onRightButton(): Boolean {
        selectNextController()
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