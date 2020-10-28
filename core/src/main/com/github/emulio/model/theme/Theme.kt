package com.github.emulio.model.theme

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.github.emulio.model.Platform

class Theme {

	var formatVersion: String? = null
	var includeTheme: Theme? = null
	var platform: Platform? = null
	var views: List<View>? = null

	override fun toString(): String {
		return "Theme(formatVersion=$formatVersion, includeTheme=$includeTheme, views=$views)"
	}

	fun getDrawableFromPlatformTheme(): Drawable {
		val systemView = checkNotNull(this.findView("system"), {
			"System tag of theme ${this.platform?.platformName} not found. please check your theme files." })

		val logo = systemView.findViewItem("logo")!! as ViewImage
		val texture = Texture(FileHandle(logo.path), true)
		texture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.MipMap)

		return TextureRegionDrawable(TextureRegion(texture))
	}

	fun findView(name: String): View? {
		views?.forEach { view ->
			if (view.name == name) {
				return view
			}
		}
		return null
	}

}



