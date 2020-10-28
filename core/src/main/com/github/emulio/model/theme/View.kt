package com.github.emulio.model.theme

class View {
	var name: String? = null
	var viewItems: MutableList<ViewItem>? = null

	override fun toString(): String {
		return "View(name=$name, viewItems=$viewItems)"
	}

	fun findViewItem(name: String): ViewItem? {
		viewItems?.forEach { item ->
			if (item.name == name) {
				return item
			}
		}
		return null
	}

}