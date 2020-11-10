package com.github.emulio.view.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.github.emulio.Emulio
import com.github.emulio.model.config.InputConfig
import com.github.emulio.view.input.InputManager

abstract class EmulioDialog(
        title: String,
        open val emulio: Emulio,
        styleName: String = "default")
    : Dialog(title, emulio.skin, styleName),
            com.github.emulio.view.input.InputListener {

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
        overlay.actions.forEach { _ ->
            reset()
        }
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

    override fun onConfirmButton(input: InputConfig) {}
    override fun onCancelButton(input: InputConfig) {}
    override fun onUpButton(input: InputConfig) {}
    override fun onDownButton(input: InputConfig) {}
    override fun onLeftButton(input: InputConfig) {}
    override fun onRightButton(input: InputConfig) {}
    override fun onFindButton(input: InputConfig) {}
    override fun onOptionsButton(input: InputConfig) {}
    override fun onSelectButton(input: InputConfig) {}
    override fun onPageUpButton(input: InputConfig) {}
    override fun onPageDownButton(input: InputConfig) {}
    override fun onExitButton(input: InputConfig) {}

}

