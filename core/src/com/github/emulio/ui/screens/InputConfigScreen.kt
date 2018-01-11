package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.emulio.Emulio
import com.github.emulio.ui.input.InputListener
import com.github.emulio.ui.input.InputManager
import com.github.emulio.utils.translate
import mu.KotlinLogging

class InputConfigScreen(emulio: Emulio, val backCallback: () -> EmulioScreen) : EmulioScreen(emulio), InputListener {
    
    private val logger = KotlinLogging.logger { }

    private val inputController: InputManager = InputManager(this, emulio.config, stage)

    init {
        Gdx.input.inputProcessor = inputController

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

        val lbTitle = Label("Input configurations".translate(), emulio.skin, "title").apply {
            setPosition(10f, screenHeight - height - 10f)
        }
        stage.addActor(lbTitle)

//        stage.addActor(Image(createColorTexture(0x000000FF)).apply {
//            width = screenWidth
//            height = 10f
//
//            setPosition(0f,)
//        })

        val root = Table().apply {

            width = screenWidth
            height = screenHeight - hudHeight - lbTitle.height - 15f

            background = TextureRegionDrawable(TextureRegion(createColorTexture(0xEFEFEFFF.toInt())))

            setPosition(0f, hudHeight)
        }

         stage.addActor(root)
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
        val txtSelect = buildText("Select".translate().toUpperCase(), helpFont, imgA.x + imgWidth + padding, y)
        stage.addActor(txtSelect)

        val imgDPadUpDown = buildImage("images/resources/help/dpad_all_128_128.png", imgWidth, imgHeight, txtSelect.x + txtSelect.width + (padding * 3), imageY)
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
        switchScreen(backCallback.invoke())
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