package com.github.emulio.ui.input

interface InputListener {
	
	fun onConfirmButton(): Boolean
	fun onCancelButton(): Boolean
	
	fun onUpButton(intensity: Float): Boolean
	fun onDownButton(intensity: Float): Boolean
	fun onLeftButton(intensity: Float): Boolean
	fun onRightButton(intensity: Float): Boolean
	
	fun onFindButton(): Boolean
	
	fun onOptionsButton(): Boolean
	fun onSelectButton(): Boolean
	
	fun onPageUpButton(intensity: Float): Boolean
	fun onPageDownButton(intensity: Float): Boolean
	
	fun onExitButton(): Boolean
}

