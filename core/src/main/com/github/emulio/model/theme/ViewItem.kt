package com.github.emulio.model.theme

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