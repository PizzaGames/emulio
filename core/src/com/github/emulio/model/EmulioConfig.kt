package com.github.emulio.model

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.utils.SharedLibraryLoader


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
		gamepadConfig = mapOf("Controller (XBOX 360 For Windows)" to InputConfig().apply {
			type = InputType.JOYSTICK
			name = "Controller (XBOX 360 For Windows)"

			confirm = Xbox.A
			cancel = Xbox.B

			up = Xbox.DPAD_UP
			down = Xbox.DPAD_DOWN
			left = Xbox.DPAD_LEFT
			right = Xbox.DPAD_RIGHT


			find = Xbox.Y

			options = Xbox.START
			select = Xbox.GUIDE

			pageUp = Xbox.L_BUMPER
			pageDown = Xbox.R_BUMPER

			//exit = Xbox.ESCAPE

			axisTrigger = 4

			axisX = Xbox.L_STICK_VERTICAL_AXIS
			axisY = Xbox.L_STICK_HORIZONTAL_AXIS
		})
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
	
	var axisTrigger: Int = -1
	
	var find: Int = -1
	
	var options: Int = -1
	var select: Int = -1
	
	var pageUp: Int = -1
	var pageDown: Int = -1

	var exit: Int = -1
}


/*******************************************************************************
 * Copyright 2011 See AUTHORS file.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/** Mappings for the Xbox series of controllers. Works only on desktop so far.

 * See [this
   * image](https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/360_controller.svg/450px-360_controller.svg.png) which describes each button and axes.

 * All codes are for buttons expect the L_STICK_XXX, R_STICK_XXX, L_TRIGGER and R_TRIGGER codes, which are axes.

 * @author badlogic
 */
object Xbox {
	// Buttons
	val A: Int
	val B: Int
	val X: Int
	val Y: Int
	val GUIDE: Int
	val L_BUMPER: Int
	val R_BUMPER: Int
	val BACK: Int
	val START: Int
	val DPAD_UP: Int
	val DPAD_DOWN: Int
	val DPAD_LEFT: Int
	val DPAD_RIGHT: Int
	val L_STICK: Int
	val R_STICK: Int

	// Axes
	/** left trigger, -1 if not pressed, 1 if pressed  */
	val L_TRIGGER: Int
	/** right trigger, -1 if not pressed, 1 if pressed  */
	val R_TRIGGER: Int
	/** left stick vertical axis, -1 if up, 1 if down  */
	val L_STICK_VERTICAL_AXIS: Int
	/** left stick horizontal axis, -1 if left, 1 if right  */
	val L_STICK_HORIZONTAL_AXIS: Int
	/** right stick vertical axis, -1 if up, 1 if down  */
	val R_STICK_VERTICAL_AXIS: Int
	/** right stick horizontal axis, -1 if left, 1 if right  */
	val R_STICK_HORIZONTAL_AXIS: Int

	init {
		if (SharedLibraryLoader.isWindows || SharedLibraryLoader.isLinux) {
			A = 0
			B = 1
			X = 2
			Y = 3
			GUIDE = 8
			L_BUMPER = 4
			R_BUMPER = 5
			BACK = 6
			START = 7
			DPAD_UP = -1
			DPAD_DOWN = -1
			DPAD_LEFT = -1
			DPAD_RIGHT = -1
			L_TRIGGER = 2
			R_TRIGGER = 5
			L_STICK_VERTICAL_AXIS = 1
			L_STICK_HORIZONTAL_AXIS = 0
			L_STICK = 9
			R_STICK_VERTICAL_AXIS = 4
			R_STICK_HORIZONTAL_AXIS = 3
			R_STICK = 10
		} else if (SharedLibraryLoader.isMac) {
			A = 11
			B = 12
			X = 13
			Y = 14
			GUIDE = 10
			L_BUMPER = 8
			R_BUMPER = 9
			BACK = 5
			START = 4
			DPAD_UP = 0
			DPAD_DOWN = 1
			DPAD_LEFT = 2
			DPAD_RIGHT = 3
			L_TRIGGER = 0
			R_TRIGGER = 1
			L_STICK_VERTICAL_AXIS = 3
			L_STICK_HORIZONTAL_AXIS = 2
			L_STICK = -1
			R_STICK_VERTICAL_AXIS = 5
			R_STICK_HORIZONTAL_AXIS = 4
			R_STICK = -1
		} else {
			A = -1
			B = -1
			X = -1
			Y = -1
			GUIDE = -1
			L_BUMPER = -1
			R_BUMPER = -1
			L_TRIGGER = -1
			R_TRIGGER = -1
			BACK = -1
			START = -1
			DPAD_UP = -1
			DPAD_DOWN = -1
			DPAD_LEFT = -1
			DPAD_RIGHT = -1
			L_STICK_VERTICAL_AXIS = -1
			L_STICK_HORIZONTAL_AXIS = -1
			L_STICK = -1
			R_STICK_VERTICAL_AXIS = -1
			R_STICK_HORIZONTAL_AXIS = -1
			R_STICK = -1
		}
	}

	/** @return whether the [Controller] is an Xbox controller
	 */
	fun isXboxController(controller: Controller): Boolean {
		return controller.name.contains("Xbox")
	}
}

