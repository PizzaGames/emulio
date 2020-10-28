package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.github.emulio.Emulio
import com.github.emulio.model.config.DummyInputConfig
import com.github.emulio.model.config.InputConfig
import com.github.emulio.ui.input.InputListener
import com.github.emulio.ui.input.InputManager
import com.github.emulio.utils.translate
import mu.KotlinLogging

class SoundConfigScreen(emulio: Emulio, private val backCallback: () -> EmulioScreen, private val firstRun: Boolean = false) : EmulioScreen(emulio), InputListener {

    private val logger = KotlinLogging.logger { }

    private val inputController: InputManager = InputManager(this, emulio, this.stage)

    private val lbCurrent: Label
    private val lbNext: Label
    private val lbPrevious: Label
    private val imgNext: Image
    private val imgPrevious: Image

    private val options: List<String>
    private var currentIndex = 0

    private val selector: Image
    private val root: Table

    private val lbPressToConfirm: Label

    init {
        Gdx.input.inputProcessor = inputController
        
        options = listOf("Scrap Platform", "Background jobs")

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
        initHelpHuds(0f, hudHeight, HelpItems(
            txtCancel = "Back".translate().toUpperCase(),
            txtConfirm = "Confirm".translate().toUpperCase(),
            txtUpDown = "Choose Platform".translate().toUpperCase(),
            txtLeftRight = "Navigate".translate().toUpperCase(),


            alpha = 0.8f,
            txtColor = Color.DARK_GRAY
        ))

        val lbScreenTitle = Label("Scraper".translate(), emulio.skin, "title").apply {
            setPosition(10f, screenHeight - height - 10f)
        }
        stage.addActor(lbScreenTitle)

        stage.addActor(Image(createColorTexture(0xEFEFEFFF.toInt())).apply {
            width = screenWidth
            height = screenHeight - hudHeight - lbScreenTitle.height - 80f

            setPosition(0f, hudHeight)
        })

        root = Table().apply {
            width = screenWidth
            height = screenHeight - hudHeight - lbScreenTitle.height - 80f - 20f

            setPosition(0f, hudHeight + 20)
        }

        stage.addActor(Image(createColorTexture(0x878787FF.toInt())).apply {
            width = screenWidth
            height = 20f

            setPosition(0f, hudHeight)
        })

        stage.addActor(Image(createColorTexture(0x878787FF.toInt())).apply {
            width = screenWidth
            height = 60f

            setPosition(0f, root.y + root.height)
        })

        lbCurrent = Label(options[currentIndex], Label.LabelStyle().apply {
            font = mainFont
            fontColor = Color.WHITE
        }).apply {
            width = screenWidth
            setAlignment(Align.center)
            setPosition(0f, root.y + root.height + 5f)
        }
        stage.addActor(lbCurrent)

        lbPrevious = Label(options[obtainPreviousIndex(currentIndex)], Label.LabelStyle().apply {
            font = mainFont
            fontColor = Color.WHITE
        }).apply {
            width = screenWidth
            setAlignment(Align.center)
            setPosition(-screenWidth, root.y + root.height + 5f)
        }
        stage.addActor(lbPrevious)

        lbNext = Label(options[obtainNextIndex(currentIndex)], Label.LabelStyle().apply {
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
                    onLeftButton(DummyInputConfig)
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
                    onRightButton(DummyInputConfig)
                }
            })
        }
        stage.addActor(imgNext)

        root.left().top().pad(20f)

        selector = Image(createColorTexture(0x878787FF.toInt()))
        selector.color.a = 0f

        stage.addActor(selector)

        stage.addActor(root)

        // we need to cache this font!
        val confirmFont = FreeTypeFontGenerator(Gdx.files.internal("fonts/RopaSans-Regular.ttf")).generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = 30
            color = Color.WHITE
        })

        lbPressToConfirm = Label("Press the desired key to reconfigure.", Label.LabelStyle(confirmFont, Color.WHITE))
        lbPressToConfirm.setPosition(screenWidth - lbPressToConfirm.width - 10f, root.y + root.height - lbPressToConfirm.height - 10f)
        lbPressToConfirm.color = Color.DARK_GRAY
        lbPressToConfirm.color.a = 0f

        stage.addActor(lbPressToConfirm)

    }


    private fun obtainNextIndex(currentIndex: Int): Int {
        return if (currentIndex == options.size - 1) {
            0
        } else {
            currentIndex + 1
        }
    }

    private fun obtainPreviousIndex(currentIndex: Int): Int {
        return if (currentIndex == 0) {
            options.size - 1
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


    private fun selectPrevious() {

        val previousIndex = obtainPreviousIndex(currentIndex)
        val name = options[previousIndex]

        lbPrevious.setText(name)

        animateSelector(imgPrevious, -5f)
        animateLabel(lbCurrent, -screenWidth, lbPrevious, name)

        updateItem(name)

        currentIndex = previousIndex

    }

    private fun selectNext() {

        val nextIndex = obtainNextIndex(currentIndex)
        val name = options[nextIndex]

        lbNext.setText(name)

        animateSelector(imgNext, 5f)
        animateLabel(lbCurrent, screenWidth, lbNext, name)

        updateItem(name)

        currentIndex = nextIndex
    }

    private fun updateItem(name: String) {

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

    override fun onConfirmButton(input: InputConfig) {
        updateHelp(input)
    }

    override fun onCancelButton(input: InputConfig) {
        updateHelp(input)
        stage.root.addAction(SequenceAction(Actions.fadeOut(1f, Interpolation.fade), Actions.run {
            switchScreen(backCallback.invoke())
        }))
    }

    override fun onUpButton(input: InputConfig) {
        updateHelp(input)
    }

    override fun onDownButton(input: InputConfig) {
        updateHelp(input)
    }

    override fun onLeftButton(input: InputConfig) {
        updateHelp(input)
        selectPrevious()
    }

    override fun onRightButton(input: InputConfig) {
        updateHelp(input)
        selectNext()
    }

    override fun onFindButton(input: InputConfig) {
        updateHelp(input)
    }

    override fun onOptionsButton(input: InputConfig) {
        updateHelp(input)
    }

    override fun onSelectButton(input: InputConfig) {
        updateHelp(input)
    }

    override fun onPageUpButton(input: InputConfig) {
        updateHelp(input)
    }

    override fun onPageDownButton(input: InputConfig) {
        updateHelp(input)
    }

    override fun onExitButton(input: InputConfig) {
        updateHelp(input)
        showCloseDialog()
    }
}