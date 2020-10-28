package com.github.emulio.model.config.controller

/** Mappings for the Xbox series of controllers. Works only on desktop so far.
 * See [this
   * image](https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/360_controller.svg/450px-360_controller.svg.png) which describes each button and axes.
 * All codes are for buttons expect the L_STICK_XXX, R_STICK_XXX, L_TRIGGER and R_TRIGGER codes, which are axes.
 * @author badlogic
 */
object XboxController {

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

	fun isXboxController(controllerName: String): Boolean {
		return controllerName.toLowerCase().contains("xbox")
	}
}