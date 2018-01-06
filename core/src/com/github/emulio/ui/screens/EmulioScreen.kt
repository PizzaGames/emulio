package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.github.emulio.Emulio
import java.math.BigInteger

abstract class EmulioScreen(val emulio: Emulio) : Screen {

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
                    borderWidth = 0.5f
                    borderColor = fontColor
                }

                shadowColor = Color(0.3f, 0.3f, 0.3f, 0.3f)
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
		stage.root.color.a = 0f
		stage.root.addAction(SequenceAction(
            Actions.fadeIn(0.5f),
            Actions.run { onScreenLoad() })
        )
	}

    open fun onScreenLoad() {

    }

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
		stage.root.color.a = 1f
		val sequenceAction = SequenceAction()
		sequenceAction.addAction(Actions.fadeOut(0.5f))
		sequenceAction.addAction(Actions.run({
			emulio.screen = newScreen
            dispose()
        }))
		stage.root.addAction(sequenceAction)
	}
}

fun main(args: Array<String>) {

    print(Integer.toHexString(BigInteger("97999b" + "FF", 16).toInt()))
}