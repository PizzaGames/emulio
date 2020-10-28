package com.github.emulio.ui.screens.util

import com.badlogic.gdx.graphics.Color
import java.math.BigInteger

object ColorCache {
    val colorCache = HashMap<String, Color>()

    fun getColor(rgba: String?): Color = when {
        rgba == null -> defaultColor()
        rgba.length == 6 -> cachedColor(rgba.toUpperCase() + "FF")
        rgba.length == 8 -> cachedColor(rgba.toUpperCase())
        else -> defaultColor()
    }

    private fun cachedColor(rgba: String): Color {
        check(rgba.length == 8)
        return colorCache[rgba] ?: storeColor(rgba)
    }

    private fun storeColor(rgba: String): Color {
        val color = createColor(rgba)
        colorCache[rgba] = color
        return color
    }


    private fun createColor(rgba: String) = Color(BigInteger(rgba, 16).toInt())

    private fun defaultColor() = Color.BLACK
}