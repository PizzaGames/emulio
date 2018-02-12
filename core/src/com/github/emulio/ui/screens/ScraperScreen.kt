package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.github.emulio.Emulio
import com.github.emulio.model.AnyInputConfig
import com.github.emulio.model.InputConfig
import com.github.emulio.model.Platform
import com.github.emulio.ui.input.*
import com.github.emulio.utils.translate
import mu.KotlinLogging

class ScraperScreen(emulio: Emulio, private val backCallback: () -> EmulioScreen, private val firstRun: Boolean = false) : EmulioScreen(emulio), InputListener {

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

    private lateinit var platformsScrollList: EmlScrollList<Platform>
    private lateinit var scraperWindow: ScraperWindow

    init {
        Gdx.input.inputProcessor = inputController

        options = listOf("Scrap Platform".translate(), "Background jobs".translate())

        // we need to cache this font!
        val mainFont = FreeTypeFontGenerator(Gdx.files.internal("fonts/RopaSans-Regular.ttf")).generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = 36
            color = Color.WHITE
        })

        logger.debug { "initializing ScraperScreen" }

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
                    onLeftButton(AnyInputConfig)
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
                    onRightButton(AnyInputConfig)
                }
            })
        }
        stage.addActor(imgNext)

        root.left().top().pad(20f)

        selector = Image(createColorTexture(0x878787FF.toInt()))
        selector.color.a = 0f

        stage.addActor(selector)

        buildScrapPlatformPage(mainFont, emulio)

        stage.addActor(root)

    }

    private fun buildScrapPlatformPage(mainFont: BitmapFont?, emulio: Emulio) {
        root.clearChildren()

        val platformsGDXList = EmlGDXList(emulio.platforms , mainFont,screenWidth / 2) {
            platform -> platform.name
        }
        val platformsGDXScroll = EmlGDXScroll(platformsGDXList.listView)

        root.add(platformsGDXScroll.scroll)

        platformsScrollList = EmlScrollList(platformsGDXScroll, platformsGDXList)

        val selectorTexture = createColorTexture(0x878787FF.toInt())
        val lightTexture = createColorTexture(0xADADADFF.toInt())

//        val platformDetail = Window("alo mundo", emulio.skin, "naked").apply {
//            pad(5f)


//            add(Label("Platform detail", Label.LabelStyle(mainFont, Color.WHITE).apply {
//                background = TextureRegionDrawable(TextureRegion(selectorTexture))
//            })).expand().center()

//            row()

//            add(Label("Total:", Label.LabelStyle(mainFont, Color.DARK_GRAY).apply {
//                background = TextureRegionDrawable(TextureRegion(lightTexture))
//            })).expand().fillX()
//            row()
//
//            add(Label("Scraped:", Label.LabelStyle(mainFont, Color.DARK_GRAY).apply {
//                background = TextureRegionDrawable(TextureRegion(lightTexture))
//            })).expand().fillX()
//            row()
//
//            add(Label("Missing:", Label.LabelStyle(mainFont, Color.DARK_GRAY).apply {
//                background = TextureRegionDrawable(TextureRegion(lightTexture))
//            })).expand().fillX()
//        }

//        platformDetail.background = TextureRegionDrawable(TextureRegion(selectorTexture))

        scraperWindow = ScraperWindow(stage,emulio.skin)
        scraperWindow.actors.forEach { actor ->
            root.add(actor)
        }
        updateScraperWindow()
    }

    private fun updateScraperWindow() {
        val platform = emulio.platforms[platformsScrollList.selectedIndex]
        scraperWindow.view.setPlatform(platform, emulio.theme[platform]!!)
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
        platformsScrollList.scroll(-1)

        updateScraperWindow()
    }

    override fun onDownButton(input: InputConfig) {
        updateHelp(input)
        platformsScrollList.scroll(1)

        updateScraperWindow()
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