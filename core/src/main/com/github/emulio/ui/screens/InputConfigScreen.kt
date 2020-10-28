package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.ControllerListener
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.controllers.PovDirection
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
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
import com.badlogic.gdx.utils.Timer
import com.github.emulio.Emulio
import com.github.emulio.model.config.DummyInputConfig
import com.github.emulio.model.config.InputConfig
import com.github.emulio.ui.input.InputListener
import com.github.emulio.ui.input.InputManager
import com.github.emulio.utils.translate
import com.github.emulio.yaml.YamlUtils
import mu.KotlinLogging
import java.io.File

class InputConfigScreen(emulio: Emulio, private val backCallback: () -> EmulioScreen, private val firstRun: Boolean = false) : EmulioScreen(emulio), InputListener {

    private val logger = KotlinLogging.logger { }

    private val inputController: InputManager = InputManager(this, emulio, this.stage)

    private val lbCurrent: Label
    private val lbNext: Label
    private val lbPrevious: Label
    private val imgNext: Image
    private val imgPrevious: Image
    private val imgController: Image

    private val controllerNames: List<String>
    private var currentIndex = 0

    private val populatedControllers: MutableMap<String, InputConfig?>

    private val inputItems: InputConfigScreen.InputItems
    private val selector: Image
    private val root: Table

    private val lbPressToConfirm: Label

    init {
        Gdx.input.inputProcessor = inputController

        populatedControllers = mutableMapOf("Keyboard" to emulio.config.keyboardConfig)

        Controllers.getControllers().forEach { controller ->
            populatedControllers[controller.name] = emulio.config.gamepadConfig[controller.name]
        }

        controllerNames = populatedControllers.keys.toList()
        currentIndex = controllerNames.indexOf((emulio.data["lastInput"] as InputConfig).name)

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
            txtConfirm = "Redefine".translate().toUpperCase(),
            txtAllDirection = "Navigate".translate().toUpperCase(),

            alpha = 0.8f,
            txtColor = Color.DARK_GRAY
        ))

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

        val name = controllerNames[currentIndex]
        val inputConfig = populatedControllers[name]!!

        root.left().top().pad(20f)
        inputItems = InputItems(root).apply {
            buildInputItem(name, inputConfig.confirm, "Confirm Action".translate(), mainFont).apply {
                imgconfirm = first
                txtconfirm = second
            }
            buildInputItem(name, inputConfig.cancel, "Cancel Action".translate(), mainFont).apply {
                imgcancel = first
                txtcancel = second
            }
            buildInputItem(name, inputConfig.find, "Find".translate(), mainFont).apply {
                imgfind = first
                txtfind = second
            }
            buildInputItem(name, inputConfig.options, "Menu".translate(), mainFont).apply {
                imgoptions = first
                txtoptions = second
            }
            buildInputItem(name, inputConfig.select, "Select".translate(), mainFont).apply {
                imgselect = first
                txtselect = second
            }
            buildInputItem(name, inputConfig.pageUp, "Page Up".translate(), mainFont).apply {
                imgpageUp = first
                txtpageUp = second
            }
            buildInputItem(name, inputConfig.pageDown, "Page Down".translate(), mainFont).apply {
                imgpageDown = first
                txtpageDown = second
            }
            buildInputItem(name, inputConfig.exit, "Exit".translate(), mainFont).apply {
                imgexit = first
                txtexit = second
            }

        }

        selector = Image(createColorTexture(0x878787FF.toInt()))
        selector.color.a = 0f

        stage.addActor(selector)
        Timer.post(object : Timer.Task() {
            override fun run() {
                selector.apply {

                    selector.color.a = 1f

                    height = mainFont.lineHeight + 5f

                    val (img, txt) = findCurrentItems(currentActionIdx)

                    txt.color = Color.WHITE

                    width = img.width + txt.width + 50f
                    val xy = findCoordinatesFromItem(root, img)

                    setPosition(xy.x, xy.y)

                }
            }

        })


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

    private fun findCoordinatesFromItem(root: Table, img: Image) =
            root.localToStageCoordinates(Vector2(img.x, img.y))

    private fun findCurrentItems(index: Int): Pair<Image, Label> {
        return inputItems.let {
            when (index) {
                it.idxconfirm -> it.imgconfirm to it.txtconfirm
                it.idxcancel -> it.imgcancel to it.txtcancel
                it.idxfind -> it.imgfind to it.txtfind
                it.idxselect -> it.imgselect to it.txtselect
                it.idxexit -> it.imgexit to it.txtexit
                it.idxoptions -> it.imgoptions to it.txtoptions
                it.idxpageUp -> it.imgpageUp to it.txtpageUp
                it.idxpageDown -> it.imgpageDown to it.txtpageDown
                else -> error("Invalid state")
            }
        }
    }

    var currentActionIdx = 0


    private fun InputItems.buildInputItem(name: String, action: Int, text: String, mainFont: BitmapFont): Pair<Image, Label> {
        val img = Image(Texture(Gdx.files.internal(getButtonImagePath(name, action)), true).apply {
            setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap)
        }).apply {
            setScaling(Scaling.fit)
            setAlign(Align.center)
        }
        val lbl = Label(text, Label.LabelStyle(mainFont, Color.WHITE)).apply {
            color = Color.DARK_GRAY
        }

        root.add(img).height(mainFont.lineHeight + 5f)
        root.add(lbl).align(Align.left)


        root.row().padTop(10f)
        return Pair(img, lbl)
    }

    class InputItems(val root: Table) {
        val idxconfirm = 0
        lateinit var imgconfirm: Image
        lateinit var txtconfirm: Label

        val idxcancel = 1
        lateinit var imgcancel: Image
        lateinit var txtcancel: Label

        val idxfind = 2
        lateinit var imgfind: Image
        lateinit var txtfind: Label

        val idxoptions = 3
        lateinit var imgoptions: Image
        lateinit var txtoptions: Label

        val idxselect = 4
        lateinit var imgselect: Image
        lateinit var txtselect: Label

        val idxpageUp = 5
        lateinit var imgpageUp: Image
        lateinit var txtpageUp: Label

        val idxpageDown = 6
        lateinit var imgpageDown: Image
        lateinit var txtpageDown: Label

        val idxexit = 7
        lateinit var imgexit: Image
        lateinit var txtexit: Label
    }

    private fun obtainImage(idx: Int): String {
        val name = controllerNames[idx]
        return when {
            isKeyboard(name) -> "images/controllers/keyboard.png"
            isXboxController(name) -> "images/controllers/xbox.png"
            isPlaystationController(name) -> "images/controllers/playstation.png"
            else -> "images/controllers/snes.png"
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

        updateInputItems(populatedControllers[name]!!)

        currentIndex = previousIndex

    }

    private fun selectNextController() {

        val nextIndex = obtainNextIndex(currentIndex)
        val name = controllerNames[nextIndex]

        lbNext.setText(name)

        animateSelector(imgNext, 5f)
        animateLabel(lbCurrent, screenWidth, lbNext, name)
        animateController(obtainImage(nextIndex))

        val inputConfig = populatedControllers[name]
        if (inputConfig != null) {
            updateInputItems(inputConfig)
        }


        currentIndex = nextIndex
    }

    private fun updateInputItems(inputConfig: InputConfig) {
        val name = inputConfig.name

        inputItems.apply {
            root.addAction(SequenceAction(Actions.fadeOut(0.5f),
                Actions.run {
                    updateInputItem(name, inputConfig.confirm, imgconfirm)
                    updateInputItem(name, inputConfig.cancel, imgcancel)
                    updateInputItem(name, inputConfig.select, imgselect)
                    updateInputItem(name, inputConfig.options, imgoptions)
                    updateInputItem(name, inputConfig.find, imgfind)
                    updateInputItem(name, inputConfig.pageUp, imgpageUp)
                    updateInputItem(name, inputConfig.pageDown, imgpageDown)
                    updateInputItem(name, inputConfig.exit, imgexit)
                },
                Actions.fadeIn(0.5f)
            ))
        }
    }

    private fun updateInputItem(controllerName: String, action: Int, img: Image) {
        val texture = Texture(Gdx.files.internal(getButtonImagePath(controllerName, action)), true).apply {
            setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap)
        }
        img.drawable = TextureRegionDrawable(TextureRegion(texture))
        img.color.a = 1f
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

    override fun onConfirmButton(input: InputConfig) {
        updateHelp()

        inputController.pause()
        Controllers.removeListener(inputController)

        lbPressToConfirm.addAction(Actions.alpha(1f, 0.1f))
        val (img, _) = findCurrentItems(currentActionIdx)
        img.addAction(SequenceAction(
                Actions.alpha(0f, 0.1f),
                Actions.delay(0.5f),
                Actions.run {
                    Gdx.input.inputProcessor = inputConsumerProcessor
                    Controllers.addListener(controllerConsumerListener)
                }
        ))
    }

    private val inputConsumerProcessor: InputProcessor = object : InputProcessor {
        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            return false
        }

        override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
            return false
        }

        override fun keyTyped(character: Char): Boolean {
            return false
        }

        override fun scrolled(amount: Int): Boolean {
            return false
        }

        override fun keyUp(keycode: Int): Boolean {
            keyPressed(keycode)
            return false
        }

        override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
            return false
        }

        override fun keyDown(keycode: Int): Boolean {
            return false
        }

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            return false
        }

    }

    private val controllerConsumerListener: ControllerListener = object : ControllerListener {
        override fun connected(controller: Controller?) {
            return
        }

        override fun buttonUp(controller: Controller?, buttonCode: Int): Boolean {
            if (controller == null) return false
            buttonPressed(controller, buttonCode)
            return false
        }

        override fun ySliderMoved(controller: Controller?, sliderCode: Int, value: Boolean): Boolean {
            return false
        }

        override fun accelerometerMoved(controller: Controller?, accelerometerCode: Int, value: Vector3?): Boolean {
            return false
        }

        override fun axisMoved(controller: Controller?, axisCode: Int, value: Float): Boolean {
            return false
        }

        override fun disconnected(controller: Controller?) {
        }

        override fun xSliderMoved(controller: Controller?, sliderCode: Int, value: Boolean): Boolean {
            return false
        }

        override fun povMoved(controller: Controller?, povCode: Int, value: PovDirection?): Boolean {
            return false
        }

        override fun buttonDown(controller: Controller?, buttonCode: Int): Boolean {
            return false
        }

    }

    private fun buttonPressed(controller: Controller, buttonCode: Int) {
        val inputConfig = populatedControllers[controller.name] ?: return
        processPressed(inputConfig, buttonCode)
    }

    private fun keyPressed(keyCode: Int) {
        val pressedConfig = emulio.config.keyboardConfig
        processPressed(pressedConfig, keyCode)
    }

    private fun processPressed(pressedConfig: InputConfig, keyCode: Int) {
        val desiredConfig = populatedControllers[controllerNames[currentIndex]]

        if (pressedConfig != desiredConfig) return

        val (img, _) = findCurrentItems(currentActionIdx)
        val name = pressedConfig.name

        val texture = Texture(Gdx.files.internal(getButtonImagePath(name, keyCode)), true).apply {
            setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap)
        }
        img.drawable = TextureRegionDrawable(TextureRegion(texture))
        img.addAction(Actions.alpha(1f, 0.2f))

        lbPressToConfirm.color.a = 0f

        changeConfig(pressedConfig, keyCode, currentActionIdx)

        Controllers.removeListener(controllerConsumerListener)
        Gdx.input.inputProcessor = inputController
        Controllers.addListener(inputController)
        inputController.resume()

    }

    private fun changeConfig(input: InputConfig, btnCode: Int, index: Int) {
        inputItems.apply {
            when (index) {
                idxconfirm -> input.confirm = btnCode
                idxcancel -> input.cancel = btnCode
                idxfind -> input.find = btnCode
                idxselect -> input.select = btnCode
                idxexit -> input.exit = btnCode
                idxoptions -> input.options = btnCode
                idxpageUp -> input.pageUp = btnCode
                idxpageDown -> input.pageDown = btnCode
                else -> error("Invalid state")
            }
        }

    }

    override fun onCancelButton(input: InputConfig) {
        updateHelp(input)
        stage.root.addAction(SequenceAction(Actions.fadeOut(1f, Interpolation.fade), Actions.run {
            switchScreen(backCallback.invoke())
        }))

        saveConfig()
    }

    private fun saveConfig() {
        val configFile = File(emulio.workdir, "emulio-config.yaml")
        YamlUtils.saveEmulioConfig(configFile, emulio.config)
    }

    override fun onUpButton(input: InputConfig) {
        updateHelp(input)

        nextActionItem(-1)
    }

    private fun nextActionItem(amount: Int) {

        val (_, currTxt) = findCurrentItems(currentActionIdx)
        currTxt.addAction(Actions.color(Color.DARK_GRAY, 0.1f))

        val nextIndex = currentActionIdx + amount

        if (amount < 0) {
            currentActionIdx = if (nextIndex < 0) {
                8 + amount
            } else {
                nextIndex
            }
        }

        if (amount > 0) {
            currentActionIdx = if (nextIndex >= 8) {
                0
            } else {
                nextIndex
            }
        }

        val (img, txt) = findCurrentItems(currentActionIdx)

        txt.addAction(Actions.color(Color.WHITE, 0.1f))

        val coord = findCoordinatesFromItem(root, img)
        selector.addAction(Actions.moveTo(coord.x, coord.y, 0.1f, Interpolation.fade))

    }

    override fun onDownButton(input: InputConfig) {
        updateHelp(input)

        nextActionItem(1)
    }

    override fun onLeftButton(input: InputConfig) {
        updateHelp(input)
        selectPreviousController()
    }

    override fun onRightButton(input: InputConfig) {
        updateHelp(input)
        selectNextController()
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