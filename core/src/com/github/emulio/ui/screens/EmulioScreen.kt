package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.github.emulio.Emulio
import com.github.emulio.ui.input.InputManager
import com.github.emulio.utils.translate
import mu.KotlinLogging
import java.math.BigInteger

abstract class EmulioScreen(val emulio: Emulio) : Screen {

    private val logger = KotlinLogging.logger { }

	val stage: Stage = Stage()

	val screenWidth = Gdx.graphics.width.toFloat()
	val screenHeight = Gdx.graphics.height.toFloat()

	private val freeFontGeneratorCache = mutableMapOf<FileHandle, FreeTypeFontGenerator>()
	private val fontCache = mutableMapOf<Triple<FileHandle, Int, Color?>, BitmapFont>()

	val freeTypeFontGenerator = getFreeTypeFontGenerator(Gdx.files.internal("fonts/RopaSans-Regular.ttf"))

	fun getColor(rgba: String?): Color = when {
//        // TODO: Color are resources? we need to cache they like in SWT/Swing lib?
        rgba == null -> Color.BLACK
        rgba.length == 6 -> Color(BigInteger(rgba.toUpperCase() + "FF", 16).toInt())
        rgba.length == 8 -> Color(BigInteger(rgba.toUpperCase(), 16).toInt())
        else -> Color.BLACK
    }

	fun getFont(fileHandle: FileHandle, fontSize: Int, fontColor: Color? = null): BitmapFont {
		val triple = Triple(fileHandle, fontSize, fontColor)

        return if (fontCache.containsKey(triple)) {
            fontCache[triple]!!
        } else {
            val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                size = fontSize

                if (fontColor != null) {
                    color = fontColor
                    borderWidth = 0.4f
                    borderColor = fontColor
                }

                shadowColor = Color(0.2f, 0.2f, 0.2f, 0.2f)
                shadowOffsetX = 1
                shadowOffsetY = 1
            }

            getFreeTypeFontGenerator(fileHandle).generateFont(parameter).apply {
                fontCache[triple] = this
            }
        }

	}

	private fun getFreeTypeFontGenerator(fileHandle: FileHandle): FreeTypeFontGenerator {
        return if (freeFontGeneratorCache.containsKey(fileHandle)) {
            freeFontGeneratorCache[fileHandle]!!
        } else {
            FreeTypeFontGenerator(fileHandle).apply { freeFontGeneratorCache[fileHandle] = this  }
        }
	}

	override fun show() {
        logger.debug { "show" }
        stage.root.actions.forEach { it.reset() }

        stage.root.color.a = 0f

        onScreenLoad()

		stage.root.addAction(SequenceAction(
            Actions.fadeIn(0.5f)
        ))
	}

    open fun onScreenLoad() {

    }

    abstract fun release()

    fun createColorTexture(rgba: Int, width: Int = 1, height: Int = 1): Texture {
		val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888).apply {
			setColor(rgba)
			fillRectangle(0, 0, width, height)
		}
		val texture = Texture(pixmap)

		pixmap.dispose()
		return texture
	}

	override fun dispose() {
		stage.dispose()
	}

	fun switchScreen(newScreen: Screen) {
        logger.debug { "switchScreen" }
		stage.root.color.a = 1f

        release()
        dispose()
        emulio.screen = newScreen
	}

    fun showCloseDialog() {

        object : YesNoDialog("Quit Emulio?".translate(), "Are you sure you want to quit emulio?".translate(), emulio) {
            override fun onCancelDialog() {
                logger.debug { "onCancelDialog" }
            }

            override fun onConfirmDialog() {
                Gdx.app.exit()
            }
        }.show(stage)

    }

    fun showInfoDialog(message: String) {
        InfoDialog("Info", message, emulio).show(stage)
    }

    fun showErrorDialog(message: String) {
        InfoDialog("Error", message, emulio).show(stage)
    }


}

fun Button.addClickListener(clickListener: () -> Unit) {
    addListener(object : ClickListener() {
        override fun clicked(event: InputEvent?, x: Float, y: Float) {
            clickListener()
        }
    })
}

abstract class EmulioDialog(title: String, val emulio: Emulio) : Dialog(title, emulio.skin, "default"), com.github.emulio.ui.input.InputListener {

    private lateinit var oldProcessor: InputProcessor
    private lateinit var inputController: InputManager

    override fun show(stage: Stage): Dialog {
        oldProcessor = Gdx.input.inputProcessor
        inputController = InputManager(this, emulio.config, stage)

        Gdx.input.inputProcessor = inputController
        return super.show(stage)
    }

    fun closeDialog() {
        hide()
        remove()
        inputController.dispose()
        Gdx.input.inputProcessor = oldProcessor
    }

    override fun onUpButton(): Boolean {
        return false
    }

    override fun onDownButton(): Boolean {
        return false
    }

    override fun onLeftButton(): Boolean {
        return false
    }

    override fun onRightButton(): Boolean {
        return false
    }

    override fun onFindButton(): Boolean {
        return false
    }

    override fun onOptionsButton(): Boolean {
        return false
    }

    override fun onSelectButton(): Boolean {
        return false
    }

    override fun onPageUpButton(): Boolean {
        return false
    }

    override fun onPageDownButton(): Boolean {
        return false
    }

    override fun onExitButton(): Boolean {
        return false
    }

}

abstract class YesNoDialog(title: String, val dialogMessage: String, emulio: Emulio) : EmulioDialog(title, emulio) {

    init {
        initGUI()
    }

    abstract fun onConfirmDialog()
    abstract fun onCancelDialog()

    private fun initGUI() {
        add(Table().apply {
            add(Label(dialogMessage, emulio.skin)).minHeight(100f).expand()
            row()
            add(ImageTextButton("Yes".translate(), emulio.skin, "confirm").apply {
                addClickListener {
                    onConfirmButton()
                }
            }).expandX().right()
            add(ImageTextButton("No".translate(), emulio.skin, "cancel").apply {
                addClickListener {
                    onCancelButton()
                }
            }).expandX().left()
        }).expand()
    }

    override fun onConfirmButton(): Boolean {
        onConfirmDialog()
        closeDialog()
        return false
    }

    override fun onCancelButton(): Boolean {
        onCancelDialog()
        closeDialog()
        return false
    }
}

class InfoDialog(title: String, val dialogMessage: String, emulio: Emulio) : EmulioDialog(title, emulio) {

    init {
        initGUI()
    }

    private fun initGUI() {
        add(Table().apply {
            add(Label(dialogMessage, emulio.skin)).minHeight(100f).expand()
            row()
            add(ImageTextButton("Ok".translate(), emulio.skin, "confirm").apply {
                addClickListener {
                    onConfirmButton()
                }
            }).expandX().right()
        }).expand()
    }

    override fun onConfirmButton(): Boolean {
        closeDialog()
        return false
    }

    override fun onCancelButton(): Boolean {
        closeDialog()
        return false
    }
}

