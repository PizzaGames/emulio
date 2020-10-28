package com.github.emulio.model.config.controller

import com.badlogic.gdx.controllers.PovDirection

object PlaystationController {

    val SQUARE = 3
    val CROSS = 2
    val TRIANGLE = 0
    val CIRCLE = 1
    val L1 = 6
    val L2 = 4
    val L3 = 10
    val R1 = 7
    val R2 = 5
    val R3 = 11
    val START = 9
    val SELECT = 8
    val POV_UP = PovDirection.north
    val POV_RIGHT = PovDirection.west
    val POV_DOWN = PovDirection.south
    val POV_LEFT = PovDirection.east
    val AXIS_LEFT_Y = 1 //-1 is up | +1 is down
    val AXIS_LEFT_X = 0 //-1 is left | +1 is right
    val AXIS_RIGHT_X = 3 //-1 is left | +1 is right
    val AXIS_RIGHT_Y = 2 //-1 is up | +1 is down

    fun isPlaystationController(controllerName: String): Boolean {
        return when {
            controllerName.toLowerCase().contains("playstation") -> true
            controllerName.toLowerCase().contains("twin usb") -> true
            else -> false
        }
    }
}