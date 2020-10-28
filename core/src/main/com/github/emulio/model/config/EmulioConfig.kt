package com.github.emulio.model.config

import com.badlogic.gdx.Input.Keys
import com.github.emulio.model.config.controller.XboxController

class EmulioConfig {
	lateinit var graphicsConfig: GraphicsConfig
	lateinit var uiConfig: UIConfig
    lateinit var languagePath: String
	lateinit var keyboardConfig: InputConfig
	lateinit var gamepadConfig: Map<String, InputConfig>

    var maxGamesList: Int = -1

	var debug: Boolean = true
	
	fun loadDefaults() {
		graphicsConfig = GraphicsConfig()
		uiConfig = UIConfig()

        languagePath = "/languages/emulio-language-en_US.yaml"
        maxGamesList = 200

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

		val gamepadXboxWired = "Xbox Controller" to InputConfig().apply {
			type = InputType.JOYSTICK
			name = "Xbox Controller"

			confirm = XboxController.A
			cancel = XboxController.B

			usePov = true
			up = XboxController.POV_UP
			down = XboxController.POV_DOWN
			left = XboxController.POV_LEFT
			right = XboxController.POV_RIGHT

			find = XboxController.Y

			options = XboxController.START
			select = XboxController.BACK

			pageUp = XboxController.L_BUMPER
			pageDown = XboxController.R_BUMPER

			axisLeftTrigger = XboxController.AXIS_LEFT_TRIGGER
			axisRightTrigger = XboxController.AXIS_RIGHT_TRIGGER

			axisX = XboxController.AXIS_LEFT_X
			axisY = XboxController.AXIS_LEFT_Y


		}

		val gamepadXboxWireless = "Wireless Xbox Controller" to InputConfig().apply {
			type = InputType.JOYSTICK
			name = "Wireless Xbox Controller"

			confirm = XboxController.A
			cancel = XboxController.B

			usePov = true
			up = XboxController.POV_UP
			down = XboxController.POV_DOWN
			left = XboxController.POV_LEFT
			right = XboxController.POV_RIGHT

			find = XboxController.Y

			options = XboxController.START
			select = XboxController.BACK

			pageUp = XboxController.L_BUMPER
			pageDown = XboxController.R_BUMPER

			axisLeftTrigger = XboxController.AXIS_LEFT_TRIGGER
			axisRightTrigger = XboxController.AXIS_RIGHT_TRIGGER

			axisX = XboxController.AXIS_LEFT_X
			axisY = XboxController.AXIS_LEFT_Y

		}

		gamepadConfig = mapOf(
			gamepadXboxWired,
			gamepadXboxWireless)
		debug = true
	}
}

