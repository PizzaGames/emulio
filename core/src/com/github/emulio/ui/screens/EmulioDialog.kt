package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.github.emulio.Emulio
import com.github.emulio.model.AnyInputConfig
import com.github.emulio.model.InputConfig
import com.github.emulio.ui.input.InputManager
import com.github.emulio.utils.translate

abstract class EmulioDialog(title: String, open val emulio: Emulio, styleName: String = "default") : Dialog(title, emulio.skin, styleName), com.github.emulio.ui.input.InputListener {

    private lateinit var oldProcessor: InputProcessor
    private lateinit var inputController: InputManager

    private lateinit var overlay: Image

    val screenWidth = Gdx.graphics.width.toFloat()
    val screenHeight = Gdx.graphics.height.toFloat()

    override fun show(stage: Stage): Dialog {
        oldProcessor = Gdx.input.inputProcessor
        if (oldProcessor is InputManager) {
            (oldProcessor as InputManager).pause()
        }

        inputController = InputManager(this, emulio, stage)

        Gdx.input.inputProcessor = inputController
        overlay = Image(createColorTexture(0x000000AA)).apply {
            setFillParent(true)
            color.a = 0f
            addAction(Actions.fadeIn(0.5f))
        }
        stage.addActor(overlay)
        return super.show(stage)
    }

    fun closeDialog(skipAnimation: Boolean = false) {
        hide()
        remove()
        overlay.actions.forEach { reset() }
        overlay.actions.clear()

        if (skipAnimation) {
            overlay.remove()
        } else {
            overlay.addAction(SequenceAction(Actions.fadeOut(0.5f), Actions.run { overlay.remove() }))
        }


        inputController.dispose()
        if (oldProcessor is InputManager) {
            (oldProcessor as InputManager).resume()
        }
        Gdx.input.inputProcessor = oldProcessor
    }

    override fun onUpButton(input: InputConfig) {
    }

    override fun onDownButton(input: InputConfig) {
    }

    override fun onLeftButton(input: InputConfig) {
    }

    override fun onRightButton(input: InputConfig) {
    }

    override fun onFindButton(input: InputConfig) {
    }

    override fun onOptionsButton(input: InputConfig) {
    }

    override fun onSelectButton(input: InputConfig) {
    }

    override fun onPageUpButton(input: InputConfig) {
    }

    override fun onPageDownButton(input: InputConfig) {
    }

    override fun onExitButton(input: InputConfig) {
    }

}

open class YesNoDialog(title: String, val dialogMessage: String, emulio: Emulio,
                           private val cancelCallback: () -> Unit = {},
                           private val confirmCallback: () -> Unit = {}) : EmulioDialog(title, emulio) {

    init {
        initGUI()
    }

    open fun onConfirmDialog() {
        confirmCallback()
    }
    open fun onCancelDialog() {
        cancelCallback()
    }

    private fun initGUI() {
        contentTable.add(Table().apply {
            add(Label(dialogMessage, emulio.skin)).align(Align.center).minHeight(100f).expandX()
            row()

            add(Button(emulio.skin).apply {
                val lastConfig = emulio.data["lastInput"] as InputConfig
                val imgSize = 25f
                add(Image(Texture(Gdx.files.internal(getButtonImagePath(lastConfig.name, lastConfig.confirm)), true).apply {
                    setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap)
                }).apply {
                    setScaling(Scaling.fit)
                    setAlign(Align.center)
                }).height(imgSize).width(imgSize)
                add(Label("Yes".translate(), emulio.skin, "title-small"))

                addClickListener {
                    onConfirmButton(AnyInputConfig)
                }
            }).expandX().right()

            add(Button(emulio.skin).apply {
                val lastConfig = emulio.data["lastInput"] as InputConfig
                val imgSize = 25f
                add(Image(Texture(Gdx.files.internal(getButtonImagePath(lastConfig.name, lastConfig.cancel)), true).apply {
                    setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap)
                }).apply {
                    setScaling(Scaling.fit)
                    setAlign(Align.center)
                }).height(imgSize).width(imgSize)
                add(Label("No".translate(), emulio.skin, "title-small"))

                addClickListener {
                    onCancelButton(AnyInputConfig)
                }
            }).expandX().left()

        }).expand().fill()
    }

    override fun onConfirmButton(input: InputConfig) {
        onConfirmDialog()
        closeDialog()
    }

    override fun onCancelButton(input: InputConfig) {
        onCancelDialog()
        closeDialog()
    }
}

class InfoDialog(title: String, val dialogMessage: String, emulio: Emulio) : EmulioDialog(title, emulio) {

    init {
        initGUI()
    }

    private fun initGUI() {
        contentTable.add(Table().apply {
            add(Label(dialogMessage, emulio.skin)).minHeight(100f).expand().fill()
            row()

            add(Button(emulio.skin).apply {
                val lastConfig = emulio.data["lastInput"] as InputConfig
                val imgSize = 25f
                add(Image(Texture(Gdx.files.internal(getButtonImagePath(lastConfig.name, lastConfig.confirm)), true).apply {
                    setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap)
                }).apply {
                    setScaling(Scaling.fit)
                    setAlign(Align.center)
                }).height(imgSize).width(imgSize)
                add(Label("Ok".translate(), emulio.skin, "title-small"))

                addClickListener {
                    onConfirmButton(AnyInputConfig)
                }
            })
        }).expand().fill()
    }

    override fun onConfirmButton(input: InputConfig) {
        closeDialog()
    }

    override fun onCancelButton(input: InputConfig) {
        closeDialog()
    }
}

