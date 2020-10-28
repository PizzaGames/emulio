package com.github.emulio.model.theme

import java.io.File

open class Text : ViewItem {

	constructor()

	constructor(copy: Text) {
		text = copy.text
		forceUpperCase = copy.forceUpperCase
		color = copy.color
		fontPath = copy.fontPath
		fontSize = copy.fontSize
		alignment = copy.alignment
	}


	var text: String? = null
	var forceUpperCase: Boolean = false
	var color: String? = null
	var fontPath: File? = null
	var fontSize: Float? = null
	var alignment: TextAlignment = TextAlignment.LEFT

	override fun toString(): String {
		return "Text(text=$text, forceUpperCase=$forceUpperCase, color=$color, fontPath=$fontPath, fontSize=$fontSize, alignment=$alignment)"
	}
}