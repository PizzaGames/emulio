package com.github.emulio.ui.input

interface InputListener {
	
	fun onConfirmButton(): Boolean
	fun onCancelButton(): Boolean
	
	fun onUpButton(): Boolean
	fun onDownButton(): Boolean
	fun onLeftButton(): Boolean
	fun onRightButton(): Boolean
	
	fun onFindButton(): Boolean
	
	fun onOptionsButton(): Boolean
	fun onSelectButton(): Boolean
	
	fun onPageUpButton(): Boolean
	fun onPageDownButton(): Boolean
	
	fun onExitButton(): Boolean
}

