package com.github.emulio.model.theme

import java.io.File

open class ViewImage : ViewItem() {
	var path: File? = null

	override fun toString(): String {
		return "Image(path=$path)"
	}

}