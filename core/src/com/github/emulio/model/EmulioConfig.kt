package com.github.emulio.model


data class EmulioConfig(
	val graphicsConfig: GraphicsConfig,
    val uiConfig: UIConfig,
	val keyboardConfig: InputConfig,
	val gamepadConfig: Map<String, InputConfig>,
	val debug: Boolean
)

data class UIConfig(
	val themeName: String,
	val screenSaver: Boolean,
	val transitionsType: String
)

data class GraphicsConfig(
	val screenWidth: Int,
	val screenHeight: Int,
	val fullscreen: Boolean,
	val vsync: Boolean
)

enum class InputType {
	KEYBOARD,
	JOYSTICK,
	OTHER,
}

data class InputConfig(
	val type: InputType,
	val name: String,

	val confirm: Int,
	val cancel: Int,

	val up: Int,
	val down: Int,
	val left: Int,
	val right: Int,

	val find: Int,

	val options: Int,
	val select: Int,

	val pageUp: Int,
	val pageDown: Int,

	val doublePageUp: Int,
	val doublePageDown: Int,

	val exit: Int
)

