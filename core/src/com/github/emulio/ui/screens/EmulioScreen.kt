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

abstract class EmulioScreen(val emulio: Emulio) : Screen {

	val stage: Stage = Stage()

	val screenWidth = Gdx.graphics.width.toFloat()
	val screenHeight = Gdx.graphics.height.toFloat()

	val freeFontGeneratorCache = mutableMapOf<FileHandle, FreeTypeFontGenerator>()
	val fontCache = mutableMapOf<Triple<FileHandle, Int, Color>, BitmapFont>()

	val freeTypeFontGenerator = getFreeTypeFontGenerator(Gdx.files.internal("fonts/RopaSans-Regular.ttf"))

	fun getColor(rgba: String?): Color {
		if (rgba == null) {
			return Color.BLACK
		}

		if (rgba.length == 6) {
			return Color(Integer.parseInt(rgba + "FF", 16))
		} else if (rgba.length == 8) {
			return Color(Integer.parseInt(rgba, 16))
		} else {
			return Color.BLACK
		}
	}

	fun getFont(fileHandle: FileHandle, fontSize: Int, fontColor: Color): BitmapFont {
		val triple = Triple(fileHandle, fontSize, fontColor)
		if (fontCache.containsKey(triple)) {
			return fontCache[triple]!!
		} else {
			return getFreeTypeFontGenerator(fileHandle).generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
				size = fontSize
				color = fontColor
			}).apply {
				fontCache[triple] = this
			}
		}

	}

	private fun getFreeTypeFontGenerator(fileHandle: FileHandle): FreeTypeFontGenerator {
		if (freeFontGeneratorCache.containsKey(fileHandle)) {
			return freeFontGeneratorCache[fileHandle]!!
		} else {
			return FreeTypeFontGenerator(fileHandle).apply { freeFontGeneratorCache[fileHandle] = this }
		}
	}

	override fun show() {
		stage.root.color.a = 0f
		stage.root.addAction(Actions.fadeIn(0.5f))
	}

	fun createColorTexture(rgba: Int): Texture {
		val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
			setColor(rgba)
			fillRectangle(0, 0, 1, 1)
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