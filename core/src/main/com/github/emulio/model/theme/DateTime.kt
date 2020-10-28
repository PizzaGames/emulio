package com.github.emulio.model.theme

import java.util.*

class DateTime : Text {

	constructor()

	constructor(copy: Text) : super(copy)

	constructor(copy: DateTime) {
		date = copy.date
		text = copy.text
		forceUpperCase = copy.forceUpperCase
		color = copy.color
		fontPath = copy.fontPath
		fontSize = copy.fontSize
		alignment = copy.alignment
	}

	var date: Date? = null

	override fun toString(): String {
		return "DateTime(date=$date, color=$color, fontPath=$fontPath, fontSize=$fontSize)"
	}
}