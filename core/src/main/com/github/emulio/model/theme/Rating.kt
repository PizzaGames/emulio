package com.github.emulio.model.theme

import java.io.File

class Rating : Text {

	constructor()

	constructor(copy: Text) : super(copy)

	constructor(copy: Rating) {
		filledPath = copy.filledPath
		unfilledPath = copy.unfilledPath
		text = copy.text
		forceUpperCase = copy.forceUpperCase
		color = copy.color
		fontPath = copy.fontPath
		fontSize = copy.fontSize
		alignment = copy.alignment
	}

	var filledPath: File? = null
	var unfilledPath: File? = null

	override fun toString(): String {
		return "Rating(filledPath=$filledPath, unfilledPath=$unfilledPath)"
	}
}