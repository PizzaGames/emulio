package com.github.emulio.view.screens.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

object FontCache {

    private val freeFontGeneratorCache = mutableMapOf<FileHandle, FreeTypeFontGenerator>()
    private val fontCache = mutableMapOf<Triple<FileHandle, Int, Color?>, BitmapFont>()

    fun freeTypeFontGenerator() = getFreeTypeFontGenerator(Gdx.files.internal("fonts/RopaSans-Regular.ttf"))

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
}