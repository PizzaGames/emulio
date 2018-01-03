package com.github.emulio.ui.input

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.ControllerAdapter
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.controllers.PovDirection
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.emulio.model.EmulioConfig
import com.github.emulio.model.InputConfig
import mu.KotlinLogging


class InputManager(val listener: InputListener, val config: EmulioConfig, val stage: Stage) : InputProcessor, ControllerAdapter() {

	private val logger = KotlinLogging.logger { }
	private val DEADZONE: Float = 0.25f

	private var elapsedPressedButtonTime = 0f
	private var elapsedPressedKeyTime = 0f

    //delays
    val povDelay = 0.25f
    val keyRepeatInterval = 0.10f
    val keyRepeatTriggerDelay = 0.5f

    val controllerRepeatInterval = 0.3f
    val controllerTriggerDelay = 0.25f

	class ControllerValues {
		var button = 0
		var povDirection: PovDirection = PovDirection.center
		var elapsedPov = 0.0f

		var axisUp = 0f
		var axisDown = 0f
		var axisLeft = 0f
		var axisRight = 0f
		var axisLT = 0f
		var axisRT = 0f

		var elapsedAxisUp = 0f
		var elapsedAxisDown = 0f
		var elapsedAxisLeft = 0f
		var elapsedAxisRight = 0f
		var elapsedAxisLT = 0f
		var elapsedAxisRT = 0f

		var axisLeftTriggered = false
		var axisRightTriggered = false
		var axisUpTriggered = false
		var axisDownTriggered = false
		var axisLTTriggered = false
		var axisRTTriggered = false

	}

	private var controllerValues: MutableMap<Controller, ControllerValues> = mutableMapOf()
	private var pressedButton = false
	private var pressedkey: Int = 0

	private var pressedKeyRepeat = false
	private var pressedButtonRepeat = false



	init {
		Controllers.addListener(this)
		reloadControllers()
	}

	fun reloadControllers() {
		Controllers.getControllers().forEach { controller ->
			controllerValues[controller] = ControllerValues()
		}
	}

	fun update(delta: Float) {
		if (pressedkey != 0) {
			updatePressedKey(delta)
		}

		if (pressedButton) {
			updatePressedButton(delta)
		}

		updateControllerAxis(delta)
		updatePov(delta)
	}

	private fun updatePov(delta: Float) {
		controllerValues.forEach { (_, cv) ->
			updatePov(cv, delta)
		}
	}

	fun updatePov(cv: ControllerValues, delta: Float) {

		cv.apply {

            when (povDirection) {
				PovDirection.center -> {
					elapsedPov = 0f
				}
				PovDirection.north -> {
					if (elapsedPov > povDelay) {
						listener.onUpButton()
						elapsedPov = 0f
					}
					elapsedPov += delta
				}
				PovDirection.south -> {
					if (elapsedPov > povDelay) {
						listener.onDownButton()
						elapsedPov = 0f
					}
					elapsedPov += delta
				}
				PovDirection.west -> {
					if (elapsedPov > povDelay) {
						listener.onLeftButton()
						elapsedPov = 0f
					}
					elapsedPov += delta
				}
				PovDirection.east -> {
					if (elapsedPov > povDelay) {
						listener.onRightButton()
						elapsedPov = 0f
					}
					elapsedPov += delta
				}
            }


		}

	}

	private fun updateControllerAxis(delta: Float) {
		controllerValues.forEach { (_, cv) ->
			updateControllerAxis(cv, delta)
		}
	}

	private fun updateControllerAxis(cv: ControllerValues, delta: Float) {
		val delayTime = 0.150f

		updateAxisUpDown(cv, delta, delayTime)
		updateAxisLeftRight(cv, delta, delayTime)

		updateAxisLTRT(cv, delta, delayTime)
	}

	private fun updateAxisLTRT(cv: ControllerValues, delta: Float, delayTime: Float) {
		cv.apply {
			if (axisLT > 0f) {
				elapsedAxisLT += delta
				if (elapsedAxisLT > delayByAxisValue(axisLT, delayTime)) {
					listener.onPageUpButton()
					elapsedAxisLT = 0f
				}
			} else {
				elapsedAxisLT = 0f
			}

			if (axisRT > 0f) {
				elapsedAxisRT += delta

				if (elapsedAxisRT > delayByAxisValue(axisRT, delayTime)) {
					listener.onPageDownButton()
					elapsedAxisRT = 0f
				}
			} else {
				elapsedAxisRT = 0f
			}
		}
	}

	private fun updateAxisUpDown(cv: ControllerValues, delta: Float, delayTime: Float) {
		cv.apply {
			if (axisUp > 0f) {
				elapsedAxisUp += delta

				if (elapsedAxisUp > delayByAxisValue(axisUp, delayTime)) {
					listener.onUpButton()
					elapsedAxisUp = 0f
				}
			} else {
				elapsedAxisUp = 0f
			}

			if (axisDown > 0f) {
				elapsedAxisDown += delta

				if (elapsedAxisDown > delayByAxisValue(axisDown, delayTime)) {
					listener.onDownButton()
					elapsedAxisDown = 0f
				}
			} else {
				elapsedAxisDown = 0f
			}
		}
	}

	private fun updateAxisLeftRight(cv: ControllerValues, delta: Float, delayTime: Float) {
		cv.apply {
			if (axisLeft > 0f) {
				elapsedAxisLeft += delta

				if (elapsedAxisLeft > delayByAxisValue(axisLeft, delayTime)) {
					listener.onLeftButton()
					elapsedAxisLeft = 0f
				}
			} else {
				elapsedAxisLeft = 0f
			}

			if (axisRight > 0f) {
				elapsedAxisRight += delta

				if (elapsedAxisRight > delayByAxisValue(axisRight, delayTime)) {
					listener.onRightButton()
					elapsedAxisRight = 0f
				}
			} else {
				elapsedAxisRight = 0f
			}
		}
	}

	private fun delayByAxisValue(axisValue: Float, delayTime: Float): Float {
		return delayTime * (1 / axisValue)
	}

	fun dispose() {
		//logger.debug { "Disposing InputManager" }
		Controllers.removeListener(this)

	}

	/////////////////////////////////////////////////////////////////////////////
	// Keyboard handling
	//
	private fun updatePressedKey(delta: Float) {
		elapsedPressedKeyTime += delta

		if (pressedKeyRepeat) {
            if (elapsedPressedKeyTime > keyRepeatInterval) {
				fireKeyboardEvent(config.keyboardConfig, pressedkey)
				elapsedPressedKeyTime = 0f
			}
		} else {
            if (elapsedPressedKeyTime > keyRepeatTriggerDelay) {
				pressedKeyRepeat = true
				elapsedPressedKeyTime = 0f
			} else {
				pressedKeyRepeat = false
			}
		}
	}

	override fun keyUp(keycode: Int): Boolean {
		pressedkey = 0
		return stage.keyUp(keycode)
	}

	override fun keyDown(keycode: Int): Boolean {
		pressedkey = keycode
		pressedKeyRepeat = false
		elapsedPressedKeyTime = 0f

		val keyboard = config.keyboardConfig
		fireKeyboardEvent(keyboard, keycode)

		return stage.keyDown(keycode)
	}

	override fun keyTyped(character: Char): Boolean {
		return stage.keyTyped(character)
	}

	private fun fireKeyboardEvent(keyboard: InputConfig, keycode: Int) {
		when (keycode) {
			keyboard.up -> listener.onUpButton()
			keyboard.down -> listener.onDownButton()
			keyboard.left -> listener.onLeftButton()
			keyboard.right -> listener.onRightButton()
			keyboard.pageUp -> listener.onPageUpButton()
			keyboard.pageDown -> listener.onPageDownButton()

			keyboard.confirm -> {
				listener.onConfirmButton()
				pressedkey = 0
			}
			keyboard.cancel -> {
				listener.onCancelButton()
				pressedkey = 0
			}
			keyboard.find -> {
				listener.onFindButton()
				pressedkey = 0
			}
			keyboard.options -> {
				listener.onOptionsButton()
				pressedkey = 0
			}
			keyboard.select -> {
				listener.onSelectButton()
				pressedkey = 0
			}
			keyboard.exit -> {
				listener.onExitButton()
				pressedkey = 0
			}
		}
	}
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Controller, Joystick handling
	//
	private fun updatePressedButton(delta: Float) {
		elapsedPressedButtonTime += delta

		if (pressedButtonRepeat) {
            if (elapsedPressedButtonTime > controllerTriggerDelay) {
				controllerValues.forEach { (controller, cv) ->
					if (cv.button != 0) {
						fireControllerButtonEvent(cv.button, controller)
					}
				}
				elapsedPressedButtonTime = 0f
			}
		} else {

            if (elapsedPressedButtonTime > controllerRepeatInterval) {
				pressedButtonRepeat = true
				elapsedPressedButtonTime = 0f
			} else {
				pressedButtonRepeat = false
			}
		}
	}

	private fun getControllerConfig(controller: Controller) = config.gamepadConfig[controller.name]

	override fun buttonUp(controller: Controller, buttonCode: Int): Boolean {
		controllerValues[controller]!!.button = 0
		pressedButton = controllerValues.map { (_, value) -> value }.any { it.button != 0 } // any button pressed?
		return true
	}

	override fun buttonDown(controller: Controller, buttonCode: Int): Boolean {
		controllerValues[controller]!!.button = buttonCode
		pressedButton = true
		elapsedPressedButtonTime = 0f
		pressedButtonRepeat = false

		fireControllerButtonEvent(buttonCode, controller)
		return true
	}

	private fun fireControllerButtonEvent(buttonCode: Int, controller: Controller) {
		val controllerCfg = getControllerConfig(controller)
		if (controllerCfg != null) {

			if (!controllerCfg.usePov) {
				when (buttonCode) {
					controllerCfg.up -> listener.onUpButton()
					controllerCfg.down -> listener.onDownButton()
					controllerCfg.left -> listener.onLeftButton()
					controllerCfg.right -> listener.onRightButton()
				}
			}

			when (buttonCode) {
				controllerCfg.pageUp -> listener.onPageUpButton()
				controllerCfg.pageDown -> listener.onPageDownButton()

				controllerCfg.confirm -> {
					listener.onConfirmButton()
					controllerValues[controller]!!.button = 0
				}

				controllerCfg.cancel -> {
					listener.onCancelButton()
					controllerValues[controller]!!.button = 0
				}

				controllerCfg.find -> {
					listener.onFindButton()
					controllerValues[controller]!!.button = 0
				}

				controllerCfg.options -> {
					listener.onOptionsButton()
					controllerValues[controller]!!.button = 0
				}

				controllerCfg.select -> {
					listener.onSelectButton()
					controllerValues[controller]!!.button = 0
				}

				controllerCfg.exit -> {
					listener.onExitButton()
					controllerValues[controller]!!.button = 0
				}

			}
		}
	}

	override fun axisMoved(controller: Controller, axisCode: Int, value: Float): Boolean {
		val cfg = getControllerConfig(controller) ?: return true

		if (cfg.axisX == axisCode) { // up or down, left or right
			axisLeftRightMoved(controllerValues[controller]!!, value)
		} else if (cfg.axisY == axisCode) {
			axisUpDownMoved(controllerValues[controller]!!, value)
		} else if (cfg.axisTrigger == axisCode) {
			axisLTRTMoved(controllerValues[controller]!!, value)
		} else {
			// no axis valid value, do something here?
		}
		return true
	}

	private fun axisLTRTMoved(cv: ControllerValues, value: Float) {
		cv.apply {
			if (value > 0.01f && value < 1f) { // Left trigger
				if (!axisLTTriggered) {
					listener.onPageUpButton()
				}

				axisLT = value
				axisRT = -1f

				axisLTTriggered = true
				axisRTTriggered = false
			} else if (value < -0.01f && value >= -1f) { // Right trigger
				if (!axisRTTriggered) {
					listener.onPageDownButton()
				}

				axisLT = -1f
				axisRT = -value

				axisLTTriggered = false
				axisRTTriggered = true
			} else {
				axisLT = -1f
				axisRT = -1f

				axisLTTriggered = false
				axisRTTriggered = false
			}
		}
	}

	private fun axisUpDownMoved(cv: ControllerValues, value: Float) {
		cv.apply {
			if (notInDeadzone(value)) {
				if (value < 0) { // up

					if (!axisUpTriggered) {
						listener.onUpButton()
					}

					axisUp = -value
					axisDown = 0f

					axisUpTriggered = true
					axisDownTriggered = false
				} else if (value > 0) { // down

					if (!axisDownTriggered) {
						listener.onDownButton()
					}

					axisUp = 0f
					axisDown = value

					axisUpTriggered = false
					axisDownTriggered = true
				}
			} else {
				axisUp = 0f
				axisDown = 0f

				axisUpTriggered = false
				axisDownTriggered = false
			}
		}
	}

	private fun axisLeftRightMoved(cv: ControllerValues, value: Float) {
		cv.apply {
			if (notInDeadzone(value)) {
				if (value < 0) { // left
					if (!axisLeftTriggered) {
						listener.onLeftButton()
					}

					axisLeft = -value
					axisRight = 0f

					axisLeftTriggered = true
					axisRightTriggered = false
				} else if (value > 0) { // right

					if (!axisRightTriggered) {
						listener.onRightButton()
					}

					axisLeft = 0f
					axisRight = value

					axisLeftTriggered = false
					axisRightTriggered = true
				}
			} else {
				axisLeft = 0f
				axisRight = 0f

				axisLeftTriggered = false
				axisRightTriggered = false
			}
		}
	}


	private fun notInDeadzone(value: Float): Boolean {
		return Math.abs(value) > DEADZONE
	}

	override fun povMoved(controller: Controller, povCode: Int, value: PovDirection): Boolean {
		controllerValues[controller]!!.apply {
			povDirection = value
			elapsedPov = 0f

			when (value) {
				PovDirection.north -> listener.onUpButton()
				PovDirection.south -> listener.onDownButton()
				PovDirection.west -> listener.onLeftButton()
				PovDirection.east -> listener.onRightButton()
			}
		}
		return true
	}

	override fun connected(controller: Controller) {
		logger.debug { "controller connected ${controller.name}" }

		reloadControllers()
	}

	override fun disconnected(controller: Controller) {
		logger.debug { "controller disconnected ${controller.name}" }

		reloadControllers()
	}
	/////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////
	// Mouse, TouchScreen, TouchPad handling
	//
	override fun scrolled(amount: Int): Boolean {
		return stage.scrolled(amount)
	}

	override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
		return stage.touchDragged(screenX, screenY, pointer)
	}

	override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
		return stage.touchDown(screenX, screenY, pointer, button)
	}

	override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
		return stage.mouseMoved(screenX, screenY)
	}

	override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
		return stage.touchUp(screenX, screenY, pointer, button)
	}
	/////////////////////////////////////////////////////////////////////////////

}


