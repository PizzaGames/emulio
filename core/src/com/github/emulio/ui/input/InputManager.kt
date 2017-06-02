package com.github.emulio.ui.input

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.controllers.*
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.emulio.model.EmulioConfig
import com.github.emulio.model.InputConfig


class InputManager(val listener: InputListener, val config: EmulioConfig, val stage: Stage) : InputProcessor, ControllerAdapter() {
	
	init {
		Controllers.addListener(this)
		
	}
	
	var elapsedTime = 0f
	var pressedkey: Int = 0
	var pressedButtons: MutableMap<Controller, Int> = mutableMapOf()
	var repeatTriggered = false
	
	fun update(delta: Float) {
		//NOP
		
		elapsedTime += delta
		
		//FIXME improve first triggering this
		if (repeatTriggered || elapsedTime > 0.8f) {
			if (elapsedTime > 0.15f) {
				if (pressedkey != 0) {
					fireKeyboardEvent(config.keyboardConfig, pressedkey)
				}
				
				pressedButtons.forEach { (controller, pressedButton) ->
					if (pressedButton != 0) {
						fireControllerButtonEvent(pressedButton, controller)
					}
				}
				elapsedTime = 0f
				repeatTriggered = true
			}
		}
		
		//TODO repetition for axis and pov
	}
	
	fun dispose() {
		Controllers.removeListener(this)
	}
	
	override fun connected(controller: Controller) {
	
	}
	
	private fun getControllerConfig(controller: Controller) = config.gamepadConfig[controller.name]
	
	override fun buttonUp(controller: Controller, buttonCode: Int): Boolean {
		pressedButtons[controller] = 0
		repeatTriggered = false
		return true
	}
	
	private fun fireControllerButtonEvent(buttonCode: Int, controller: Controller) {
		val gamepad = config.gamepadConfig[controller.name]
		if (gamepad != null) {
			when (buttonCode) {
				gamepad.up -> listener.onUpButton(1f)
				gamepad.down -> listener.onDownButton(1f)
				gamepad.left -> listener.onLeftButton(1f)
				gamepad.right -> listener.onRightButton(1f)
				gamepad.pageUp -> listener.onPageUpButton(1f)
				gamepad.pageDown -> listener.onPageDownButton(1f)
				
				gamepad.confirm -> {
					listener.onConfirmButton()
					pressedButtons[controller] = 0
				}
				
				gamepad.cancel -> {
					listener.onCancelButton()
					pressedButtons[controller] = 0
				}
				
				gamepad.find -> {
					listener.onFindButton()
					pressedButtons[controller] = 0
				}
				
				gamepad.options -> {
					listener.onOptionsButton()
					pressedButtons[controller] = 0
				}
				
				gamepad.select -> {
					listener.onSelectButton()
					pressedButtons[controller] = 0
				}
				
				gamepad.exit -> {
					listener.onExitButton()
					pressedButtons[controller] = 0
				}
				
			}
		}
	}
	
	override fun axisMoved(controller: Controller, axisCode: Int, value: Float): Boolean {
		if (inDeadzone(value)) {
			return true
		}
		
		val gamepad = config.gamepadConfig[controller.name]

		if (gamepad != null) {
			when (axisCode) {
				gamepad.axisX -> {
					if (value > 0) {
						listener.onLeftButton(calculateIntensity(value))
					} else {
						listener.onRightButton(calculateIntensity(value))
					}
				}
				gamepad.axisY -> {
					if (value > 0) {
						listener.onUpButton(calculateIntensity(value))
					} else {
						listener.onDownButton(calculateIntensity(value))
					}
				}
				gamepad.axisTrigger -> {
					if (value > 0) {
					
					} else {
					
					}
				}
			}
		}
	}
	
	override fun disconnected(controller: Controller) {
	
	}
	
	override fun povMoved(controller: Controller, povCode: Int, value: PovDirection?): Boolean {
		TODO()
	}
	
	override fun buttonDown(controller: Controller, buttonCode: Int): Boolean {
		pressedButtons[controller] = buttonCode
		fireControllerButtonEvent(buttonCode, controller)
		
		return true
	}
	
	override fun keyTyped(character: Char): Boolean {
		return stage.keyTyped(character)
	}
	
	override fun scrolled(amount: Int): Boolean {
		return stage.scrolled(amount)
	}
	
	override fun keyUp(keycode: Int): Boolean {
		pressedkey = 0
		repeatTriggered = false
		
		return stage.keyUp(keycode)
	}
	
	private fun fireKeyboardEvent(keyboard: InputConfig, keycode: Int) {
		when (keycode) {
			keyboard.up -> listener.onUpButton(1f)
			keyboard.down -> listener.onDownButton(1f)
			keyboard.left -> listener.onLeftButton(1f)
			keyboard.right -> listener.onRightButton(1f)
			keyboard.pageUp -> listener.onPageUpButton(1f)
			keyboard.pageDown -> listener.onPageDownButton(1f)
			
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
	
	override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
		return stage.touchDragged(screenX, screenY, pointer)
	}
	
	override fun keyDown(keycode: Int): Boolean {
		pressedkey = keycode
		
		val keyboard = config.keyboardConfig
		fireKeyboardEvent(keyboard, keycode)
		
		return stage.keyDown(keycode)
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
	
	private fun inDeadzone(value: Float): Boolean {
		return value < DEADZONE || value > -DEADZONE
	}
	
	private val DEADZONE = 0.15f

	private fun calculateIntensity(value: Float): Float {
		val abs = Math.abs(value)
		if (abs > 0.5f) {
			return 10f
		} else if (abs > DEADZONE) {
			return 1f
		} else {
			return 0f
		}
	}

//private fun processGamepadInput(buttonCode: Int, controller: Controller?): Boolean {
//	return if (controller != null) {
//		val gamepad = config.gamepadConfig?.get(controller.name)
//		if (gamepad != null) {
//			when (buttonCode) {
//				gamepad.confirm -> listener.onConfirmButton()
//				gamepad.cancel -> listener.onCancelButton()
//				gamepad.up -> listener.onUpButton()
//				gamepad.down -> listener.onDownButton()
//				gamepad.left -> listener.onLeftButton()
//				gamepad.right -> listener.onRightButton()
//				gamepad.find -> listener.onFindButton()
//				gamepad.options -> listener.onOptionsButton()
//				gamepad.select -> listener.onSelectButton()
//				gamepad.pageUp -> listener.onPageUpButton()
//				gamepad.pageDown -> listener.onPageDownButton()
//				gamepad.doublePageUp -> listener.onDoublePageUpButton()
//				gamepad.doublePageDown -> listener.onDoublePageDownButton()
//				gamepad.exit -> listener.onExitButton()
//				else -> false
//			}
//		} else {
//			false
//		}
//	} else {
//		false
//	}


//	private val logger = KotlinLogging.logger {}
//	init {
//		Controllers.addListener(object : ControllerAdapter() {
//			override fun povMoved(controller: Controller, povIndex: Int, value: PovDirection?): Boolean {
//				logger.debug { "povMoved $povIndex $value" }
//				return super.povMoved(controller, povIndex, value)
//			}
//		})
//	}
//	val input = Gdx.input
//
//	val keyboardCfg = config.keyboardConfig
//	val gamepadsCfg = config.gamepadConfig
//
//	class ControllerData {
//		lateinit var controller: Controller
//
//		var joyAlreadyPressed = false
//		var joyPressedElapsed = 0.0f
//
//		var ConfirmButton = false
//		var CancelButton = false
//		var FindButton = false
//		var OptionsButton = false
//		var SelectButton = false
//		var ExitButton = false
//		var UpButton = false
//		var DownButton = false
//		var LeftButton = false
//		var RightButton = false
//		var PageUpButton = false
//		var PageDownButton = false
//	}
//
//
//	var controllers = updateControllers()
//
//	fun reloadControllers() {
//		controllers = updateControllers()
//	}
//
//	private fun updateControllers(): List<ControllerData> {
//		return Controllers.getControllers().map {
//			ControllerData().apply {
//				controller = it
//			}
//		}
//	}
//
//	fun update(delta: Float) {
//		updateControllers(delta)
//		updateKey(delta)
//	}
//
//	fun updateController(contData: ControllerData, delta: Float) {
//		val controller = contData.controller
//
//		val gamepad = gamepadsCfg[controller.name] ?: return
//
//
////		if (!anyJoyPressed(controller)) {
////			joyAlreadyPressed = false
////			joyPressedElapsed = 0.0f
////			return
////		}
//
//		contData.joyPressedElapsed += delta
//
//		if (contData.joyAlreadyPressed && contData.joyPressedElapsed < 0.15f) {
//			return
//		}
//
//		if (controller.getButton(gamepad.confirm)) {
//			if (!contData.ConfirmButton) {
//				listener.onConfirmButton()
//			}
//			contData.ConfirmButton = true
//		} else if (controller.getButton(gamepad.cancel)) {
//			if (!contData.CancelButton) {
//				listener.onCancelButton()
//			}
//			contData.CancelButton = true
//		} else if (controller.getButton(gamepad.find)) {
//			if (!contData.FindButton) {
//				listener.onFindButton()
//			}
//			contData.FindButton = true
//		} else if (controller.getButton(gamepad.options)) {
//			if (!contData.OptionsButton) {
//				listener.onOptionsButton()
//			}
//			contData.OptionsButton = true
//		} else if (controller.getButton(gamepad.select)) {
//			if (!contData.SelectButton) {
//				listener.onSelectButton()
//			}
//			contData.SelectButton = true
//		} else if (controller.getButton(gamepad.exit)) {
//			if (!contData.ExitButton) {
//				listener.onExitButton()
//			}
//			contData.ExitButton = true
//		} else if (controller.getButton(gamepad.up)) {
//			if (!contData.UpButton) {
//				listener.onUpButton(1f)
//			}
//			contData.UpButton = true
//		} else if (controller.getButton(gamepad.down)) {
////			if (!contData.DownButton) {
//				listener.onDownButton(1f)
////			}
////			contData.DownButton = true
//		} else if (controller.getButton(gamepad.left)) {
////			if (!contData.LeftButton) {
//				listener.onLeftButton(1f)
////			}
////			contData.LeftButton = true
//		} else if (controller.getButton(gamepad.right)) {
////			if (!contData.RightButton) {
//				listener.onRightButton(1f)
////			}
////			contData.RightButton = true
//		} else if (controller.getButton(gamepad.pageUp)) {
////			if (!contData.PageUpButton) {
//				listener.onPageUpButton(1f)
////			}
////			contData.PageUpButton = true
//		} else if (controller.getButton(gamepad.pageDown)) {
////			if (!contData.PageDownButton) {
//				listener.onPageDownButton(1f)
////			}
////			contData.PageDownButton = true
//		} else {
//			contData.joyAlreadyPressed = false
//			contData.joyPressedElapsed = 0.0f
//
//			contData.ConfirmButton = false
//			contData.CancelButton = false
//			contData.FindButton = false
//			contData.OptionsButton = false
//			contData.SelectButton = false
//			contData.ExitButton = false
//			contData.UpButton = false
//			contData.DownButton = false
//			contData.LeftButton = false
//			contData.RightButton = false
//			contData.PageUpButton = false
//			contData.PageDownButton = false
//		}
//
//		val x = controller.getAxis(gamepad.axisX)
//		if (isOutsideDeadzone(x)) {
//			if (x > 0) {
//				listener.onRightButton(calculateIntensity(x))
//			} else {
//				listener.onLeftButton(calculateIntensity(x))
//			}
//		}
//
//
//		val y = controller.getAxis(gamepad.axisY)
//		if (isOutsideDeadzone(y)) {
//			if (y > 0f) {
//				listener.onUpButton(calculateIntensity(y))
//			} else {
//				listener.onDownButton(calculateIntensity(y))
//			}
//		}
//
//		val lt = controller.getAxis(gamepad.axisTrigger)
//		if (isOutsideDeadzone(lt)) {
//			if (lt > 0f) {
//				listener.onPageDownButton(calculateIntensity(lt))
//			} else {
//				listener.onPageUpButton(calculateIntensity(lt))
//			}
//		}
//
//
//		val povDirection = controller.getPov(0)
//		if (povDirection == PovDirection.north) {
//			listener.onUpButton(1f)
//		} else if (povDirection == PovDirection.south) {
//			listener.onDownButton(1f)
//		} else if (povDirection == PovDirection.west) {
//			listener.onRightButton(1f)
//		} else if (povDirection == PovDirection.east) {
//			listener.onLeftButton(1f)
//		}
//
//		contData.joyAlreadyPressed = true
//		contData.joyPressedElapsed = 0.0f
//
//	}
//
//	private fun isOutsideDeadzone(value: Float) = value > 0.15f || value < -0.15f
//
//
//	private val DEADZONE = 0.1f //TODO deadzone
//
//
//	private fun calculateIntensity(value: Float): Float {
//		val abs = Math.abs(value)
//		if (abs > 0.5f) {
//			return 10f
//		} else if (abs > DEADZONE) {
//			return 1f
//		} else {
//			return 0f
//		}
//	}
//
//	private fun updateControllers(delta: Float) {
//		controllers.forEach { data ->
//			updateController(data, delta)
//		}
//	}
//
//	var keyAlreadyPressed = false
//	var keyPressedElapsed = 0.0f
//
//	private fun updateKey(delta: Float) {
//
//		if (!anyKeyPressed()) {
//			keyAlreadyPressed = false
//			keyPressedElapsed = 0.0f
//			return
//		}
//
//		keyPressedElapsed += delta
//
//		if (keyAlreadyPressed && keyPressedElapsed < 0.15f) {
//			return
//		}
//
//		if (input.isKeyJustPressed(keyboardCfg.confirm)) {
//			listener.onConfirmButton()
//		} else if (input.isKeyJustPressed(keyboardCfg.cancel)) {
//			listener.onCancelButton()
//		} else if (input.isKeyJustPressed(keyboardCfg.find)) {
//			listener.onFindButton()
//		} else if (input.isKeyJustPressed(keyboardCfg.options)) {
//			listener.onOptionsButton()
//		} else if (input.isKeyJustPressed(keyboardCfg.select)) {
//			listener.onSelectButton()
//		} else if (input.isKeyJustPressed(keyboardCfg.exit)) {
//			listener.onExitButton()
//		} else if (input.isKeyPressed(keyboardCfg.up)) {
//			listener.onUpButton(1f)
//		} else if (input.isKeyPressed(keyboardCfg.down)) {
//			listener.onDownButton(1f)
//		} else if (input.isKeyPressed(keyboardCfg.left)) {
//			listener.onLeftButton(1f)
//		} else if (input.isKeyPressed(keyboardCfg.right)) {
//			listener.onRightButton(1f)
//		} else if (input.isKeyPressed(keyboardCfg.pageUp)) {
//			listener.onPageUpButton(1f)
//		} else if (input.isKeyPressed(keyboardCfg.pageDown)) {
//			listener.onPageDownButton(1f)
//		}
//
//		keyAlreadyPressed = true
//		keyPressedElapsed = 0.0f
//	}
//
//	private fun anyKeyPressed() = Gdx.input.isKeyPressed(Input.Keys.ANY_KEY)
//
//	fun dispose() {
//
//	}
//
	
}

