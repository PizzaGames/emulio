package com.github.emulio.model.theme

class TextList : Text {
	constructor()

	constructor(copy: Text) : super(copy)

	constructor(copy: TextList) {
		selectorColor = copy.selectorColor
		selectedColor = copy.selectedColor
		primaryColor = copy.primaryColor
		secondaryColor = copy.secondaryColor
		text = copy.text
		forceUpperCase = copy.forceUpperCase
		color = copy.color
		fontPath = copy.fontPath
		fontSize = copy.fontSize
		alignment = copy.alignment
	}

	var selectorColor: String? = null
	var selectedColor: String? = null
	var primaryColor: String? = null
	var secondaryColor: String? = null
}