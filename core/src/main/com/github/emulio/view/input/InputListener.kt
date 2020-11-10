package com.github.emulio.view.input

import com.github.emulio.model.config.InputConfig

interface InputListener {
	
	fun onConfirmButton(input: InputConfig)
	fun onCancelButton(input: InputConfig)
	
	fun onUpButton(input: InputConfig)
	fun onDownButton(input: InputConfig)
	fun onLeftButton(input: InputConfig)
	fun onRightButton(input: InputConfig)
	
	fun onFindButton(input: InputConfig)
	
	fun onOptionsButton(input: InputConfig)
	fun onSelectButton(input: InputConfig)
	
	fun onPageUpButton(input: InputConfig)
	fun onPageDownButton(input: InputConfig)
	
	fun onExitButton(input: InputConfig)
}

