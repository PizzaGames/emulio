package com.github.emulio.model.theme

import com.github.emulio.model.Platform
import java.io.File
import java.util.*

class Theme {
	var formatVersion: String? = null
	var includeTheme: Theme? = null
	var platform: Platform? = null

	var views: List<View>? = null

	override fun toString(): String {
		return "Theme(formatVersion=$formatVersion, includeTheme=$includeTheme, views=$views)"
	}
	
	fun getViewByName(name: String): View? {
		views?.forEach { view ->
			if (view.name == name) {
				return view
			}
		}
		return null
	}

}


class View {
	var name: String? = null
	var viewItems: MutableList<ViewItem>? = null

	override fun toString(): String {
		return "View(name=$name, viewItems=$viewItems)"
	}
	
	fun getItemByName(name: String): ViewItem? {
		viewItems?.forEach { item ->
			if (item.name == name) {
				return item
			}
		}
		
		return null
	}
	
}


open class ViewItem {
	var name: String? = null
	var positionX: Float? = null
	var positionY: Float? = null
	var maxSizeX: Float? = null
	var maxSizeY: Float? = null
	var sizeX: Float? = null
	var sizeY: Float? = null
	var originX: Float? = null
	var originY: Float? = null
	var extra: Boolean? = null
	var horizontalMargin: Float? = null
	var verticalMargin: Float? = null
	var textColor: String? = null
	var iconColor: String? = null

	override fun toString(): String {
		return "ViewItem(name=$name, positionX=$positionX, positionY=$positionY, maxSizeX=$maxSizeX, maxSizeY=$maxSizeY, sizeX=$sizeX, sizeY=$sizeY, originX=$originX, originY=$originY, extra=$extra, horizontalMargin=$horizontalMargin, verticalMargin=$verticalMargin, textColor=$textColor, iconColor=$iconColor)"
	}

}

open class ViewImage : ViewItem() {
	var path: File? = null

	override fun toString(): String {
		return "Image(path=$path)"
	}

}

enum class TextAlignment {
	CENTER,
	LEFT,
	RIGHT,
	JUSTIFY
}

class Text : ViewItem() {
	var text: String? = null
	var forceUpperCase: Boolean? = null
	var color: String? = null
	var fontPath: File? = null
	var fontSize: Int? = null
	var alignment: TextAlignment? = null

	override fun toString(): String {
		return "Text(text=$text, forceUpperCase=$forceUpperCase, color=$color, fontPath=$fontPath, fontSize=$fontSize, alignment=$alignment)"
	}
}

class DateTime : ViewItem() {
	var date: Date? = null
	var color: String? = null
	var fontPath: File? = null
	var fontSize: Int? = null

	override fun toString(): String {
		return "DateTime(date=$date, color=$color, fontPath=$fontPath, fontSize=$fontSize)"
	}
}

class Rating : ViewItem() {
	var filledPath: File? = null
	var unfilledPath: File? = null

	override fun toString(): String {
		return "Rating(filledPath=$filledPath, unfilledPath=$unfilledPath)"
	}
}

class TextList : ViewItem()


class HelpSystem : ViewItem() {
	override fun toString(): String {
		return "HelpSystem()"
	}
}

class Container : ViewItem() {
	override fun toString(): String {
		return "Container()"
	}
}

class NinePatch : ViewImage() {
	override fun toString(): String {
		return "NinePatch()"
	}
}



