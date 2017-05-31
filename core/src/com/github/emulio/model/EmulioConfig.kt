package com.github.emulio.model

import com.badlogic.gdx.Input
import com.badlogic.gdx.Input.Keys


class EmulioConfig {
	lateinit var graphicsConfig: GraphicsConfig
	lateinit var uiConfig: UIConfig
	lateinit var keyboardConfig: InputConfig
	lateinit var gamepadConfig: Map<String, InputConfig>
	var debug: Boolean = false
	
	fun loadDefaults() {
		graphicsConfig = GraphicsConfig()
		uiConfig = UIConfig()
		keyboardConfig = InputConfig().apply {
			type = InputType.KEYBOARD
			name = "Keyboard"
			
			confirm = Keys.Z
			cancel = Keys.X
			
			up = Keys.UP
			down = Keys.DOWN
			left = Keys.LEFT
			right = Keys.RIGHT
			
			
			find = Keys.F3
			
			options = Keys.BACKSPACE
			select = Keys.P
			
			pageUp = Keys.PAGE_UP
			pageDown = Keys.PAGE_DOWN
			
			exit = Keys.ESCAPE
		}
		gamepadConfig = emptyMap()
		debug = true
	}
}

class UIConfig {
	var themeName: String? = null
	var screenSaver: Boolean? = null
	var transitionsType: String? = null
}

class GraphicsConfig {
	var screenWidth: Int? = null
	var screenHeight: Int? = null
	var fullscreen: Boolean? = null
	var vsync: Boolean? = null
}

enum class InputType {
	KEYBOARD,
	JOYSTICK,
	OTHER,
}

class InputConfig {
	lateinit var type: InputType
	lateinit var name: String
	
	var confirm: Int = -1
	var cancel: Int = -1
	
	var up: Int = -1
	var down: Int = -1
	var left: Int = -1
	var right: Int = -1
	
	var axisX: Int = -1
	var axisY: Int = -1
	
	var lTrigger: Int = -1
	var rTrigger: Int = -1
	
	var find: Int = -1
	
	var options: Int = -1
	var select: Int = -1
	
	var pageUp: Int = -1
	var pageDown: Int = -1

	var exit: Int = -1
}

