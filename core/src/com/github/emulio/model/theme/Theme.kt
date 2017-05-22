package com.github.emulio.model.theme

import java.io.File
import java.util.*

class Theme {
	var formatVersion: String? = null
	var includeTheme: Theme? = null

	var views: List<View>? = null
}


class View {
	var name: String? = null
	var viewItems: List<ViewItem>? = null
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
}

class Image : ViewItem() {
	var path: File? = null
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
}

class DateTime : ViewItem() {
	var date: Date? = null
	var color: String? = null
	var fontPath: File? = null
	var fontSize: Int? = null
}

class Rating : ViewItem() {
	var filledPath: File? = null
	var unfilledPath: File? = null
}

class TextList : ViewItem()

class HelpSystem : ViewItem()





