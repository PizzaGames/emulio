package com.github.emulio.model

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.scenes.scene2d.ui.Image
import jdk.internal.util.xml.impl.Input
import org.apache.batik.util.XBLConstants
import java.io.File


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
		gamepadConfig = mapOf("Xbox Controller" to InputConfig().apply {
			type = InputType.JOYSTICK
			name = "Xbox Controller"

			confirm = Xbox.A
			cancel = Xbox.B

			usePov = true
			up = Xbox.POV_UP
			down = Xbox.POV_DOWN
			left = Xbox.POV_LEFT
			right = Xbox.POV_RIGHT

			find = Xbox.Y

			options = Xbox.START
			select = Xbox.BACK

			pageUp = Xbox.L_BUMPER
			pageDown = Xbox.R_BUMPER

			//exit = Xbox.ESCAPE

			axisLeftTrigger = Xbox.AXIS_LEFT_TRIGGER
            axisRightTrigger = Xbox.AXIS_RIGHT_TRIGGER

			axisX = Xbox.AXIS_LEFT_X
			axisY = Xbox.AXIS_LEFT_Y


		})
		debug = true
	}
}

class UIConfig {
	var themeName: String? = "simple"
	var screenSaver: Boolean? = null
}

class GraphicsConfig {
	var screenWidth: Int? = 1280
	var screenHeight: Int? = 720
	var fullscreen: Boolean? = false
	var vsync: Boolean? = true
}

enum class InputType {
	KEYBOARD,
	JOYSTICK,
	OTHER,
}

open class InputConfig {

	lateinit var type: InputType
	lateinit var name: String
	
	var confirm: Int = -1
	var cancel: Int = -1

	var usePov = false
	var up: Int = -1
	var down: Int = -1
	var left: Int = -1
	var right: Int = -1
	
	var axisX: Int = -1
	var axisY: Int = -1

    var axisLeftTrigger: Int = -1
    var axisRightTrigger: Int = -1

	var find: Int = -1
	
	var options: Int = -1
	var select: Int = -1
	
	var pageUp: Int = -1
	var pageDown: Int = -1

	var exit: Int = -1

    var configImages: Map<Int, String> = emptyMap()
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

	val A = 0
	val B = 1
	val X = 2
	val Y = 3
	val L_BUMPER = 4
	val R_BUMPER = 5
	val BACK = 6
	val START = 7
	val L_STICK = 8
	val R_STICK = 9
	val POV_UP = 10
	val POV_RIGHT = 11
	val POV_DOWN = 12
	val POV_LEFT = 13
	val AXIS_LEFT_Y = 1 //-1 is up | +1 is down
	val AXIS_LEFT_X = 0 //-1 is left | +1 is right
	val AXIS_RIGHT_X = 2 //-1 is left | +1 is right
	val AXIS_RIGHT_Y = 3 //-1 is up | +1 is down
    val AXIS_LEFT_TRIGGER = 4 //-1 for released | 1 for pressed
    val AXIS_RIGHT_TRIGGER = 5 //-1 for released | 1 for pressed

	/** @return whether the [Controller] is an Xbox controller
	 */
	fun isXboxController(controller: Controller): Boolean {
		return controller.name.contains("Xbox")
	}
}

object AnyInputConfig : InputConfig()