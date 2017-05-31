package com.github.emulio.ui.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.controllers.mappings.Xbox
import com.github.emulio.model.EmulioConfig
import mu.KotlinLogging


class InputManager(val listener: InputListener, val config: EmulioConfig) {
	
	private val logger = KotlinLogging.logger {}
	
	val input = Gdx.input
	
	val keyboardCfg = config.keyboardConfig!!
	val gamepadsCfg = config.gamepadConfig!!
	
	var controllers = Controllers.getControllers()
	var controllersSize = controllers.size
	
	
	fun reloadControllers() {
		controllers = Controllers.getControllers()
		var controllersSize = controllers.size
	}
	
	
	val elapseMap: MutableMap<Int, Float> = mutableMapOf()
	
	fun update(delta: Float) {
		
		updateControllers(delta)
		updateKey(delta)
		
//		elapsed += delta
//		if (elapsed > 0.5f) {
//			logger.debug { "update: $delta" }
//			elapsed = 0.0f
//		}
//
	}
	
	fun updateController(controller: Controller) {
		val gamepad = gamepadsCfg[controller.name] ?: return
		
		if (controller.getButton(gamepad.confirm)) {
			listener.onConfirmButton()
		} else if (controller.getButton(gamepad.cancel)) {
			listener.onCancelButton()
		} else if (controller.getButton(gamepad.find)) {
			listener.onFindButton()
		} else if (controller.getButton(gamepad.options)) {
			listener.onOptionsButton()
		} else if (controller.getButton(gamepad.select)) {
			listener.onSelectButton()
		} else if (controller.getButton(gamepad.exit)) {
			listener.onExitButton()
		}
		
		if (controller.getButton(gamepad.up)) {
			listener.onUpButton(1f)
		} else if (controller.getButton(gamepad.down)) {
			listener.onDownButton(1f)
		} else if (controller.getButton(gamepad.left)) {
			listener.onLeftButton(1f)
		} else if (controller.getButton(gamepad.right)) {
			listener.onRightButton(1f)
		} else if (controller.getButton(gamepad.pageUp)) {
			listener.onPageUpButton(1f)
		} else if (controller.getButton(gamepad.pageDown)) {
			listener.onPageDownButton(1f)
		}
		
		val x = controller.getAxis(gamepad.axisX)
		if (x > 0f) {
			listener.onRightButton(calculateIntensity(x))
		} else {
			listener.onLeftButton(calculateIntensity(x))
		}
		
		val y = controller.getAxis(gamepad.axisY)
		if (y > 0f) {
			listener.onUpButton(calculateIntensity(x))
		} else {
			listener.onDownButton(calculateIntensity(x))
		}
		
		val lt = controller.getAxis(gamepad.lTrigger)
		if (lt > 0f) {
			listener.onPageUpButton(calculateIntensity(lt))
		}
		val rt = controller.getAxis(gamepad.rTrigger)
		if (rt > 0f) {
			listener.onPageDownButton(calculateIntensity(lt))
		}
	}
	
	private val DEADZONE = 0.1f //TODO deadzone
	

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
	
	private fun updateControllers(delta: Float) {
		//TODO
//		for (i in 0..controllersSize) {
//			updateController(controllers[i])
//		}
	}
	
	private var keyPressed: Int = -1
	private var keyElapsed: Float = 0.0f
	
	private fun updateKey(delta: Float) {
		
//		if (keyElapsed < 0.5f && keyPressed != -1) {
//			keyElapsed += delta
//			return
//		}

		if (input.isKeyJustPressed(keyboardCfg.confirm)) {
			keyPressed = keyboardCfg.confirm
			listener.onConfirmButton()
		} else if (input.isKeyJustPressed(keyboardCfg.cancel)) {
			keyPressed = keyboardCfg.cancel
			listener.onCancelButton()
		} else if (input.isKeyPressed(keyboardCfg.up)) {
			keyPressed = keyboardCfg.up
			listener.onUpButton(1f)
		} else if (input.isKeyPressed(keyboardCfg.down)) {
			keyPressed = keyboardCfg.down
			listener.onDownButton(1f)
		} else if (input.isKeyPressed(keyboardCfg.left)) {
			keyPressed = keyboardCfg.left
			listener.onLeftButton(1f)
		} else if (input.isKeyPressed(keyboardCfg.right)) {
			keyPressed = keyboardCfg.right
			listener.onRightButton(1f)
		} else if (input.isKeyJustPressed(keyboardCfg.find)) {
			keyPressed = keyboardCfg.find
			listener.onFindButton()
		} else if (input.isKeyJustPressed(keyboardCfg.options)) {
			keyPressed = keyboardCfg.options
			listener.onOptionsButton()
		} else if (input.isKeyJustPressed(keyboardCfg.select)) {
			keyPressed = keyboardCfg.select
			listener.onSelectButton()
		} else if (input.isKeyJustPressed(keyboardCfg.pageUp)) {
			keyPressed = keyboardCfg.pageUp
			listener.onPageUpButton(1f)
		} else if (input.isKeyJustPressed(keyboardCfg.pageDown)) {
			keyPressed = keyboardCfg.pageDown
			listener.onPageDownButton(1f)
		} else if (input.isKeyJustPressed(keyboardCfg.exit)) {
			keyPressed = keyboardCfg.exit
			listener.onExitButton()
		} else {
			keyPressed = -1
			keyElapsed = 0.0f
		}
		
//		if (keyPressed != -1) {
//			keyElapsed += delta
//		}
//		if(Gdx.input.isKeyPressed(Keys.ANY_KEY)) {
//			// your actions
//			jump();
//		}
	}
	
	/*
	override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
		return true
	}
	
	override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
		return true
	}
	
	override fun keyTyped(character: Char): Boolean {
		return true
	}
	
	override fun scrolled(amount: Int): Boolean {
		return true
	}
	
	override fun keyUp(keycode: Int): Boolean {
		logger.debug { "keyUp: $keycode" }
		
		val keyboard = config.keyboardConfig ?: return true
		
		return when (keycode) {
			keyboard.confirm -> listener.onConfirmButton()
			keyboard.cancel -> listener.onCancelButton()
			keyboard.up -> listener.onUpButton()
			keyboard.down -> listener.onDownButton()
			keyboard.left -> listener.onLeftButton()
			keyboard.right -> listener.onRightButton()
			keyboard.find -> listener.onFindButton()
			keyboard.options -> listener.onOptionsButton()
			keyboard.select -> listener.onSelectButton()
			keyboard.pageUp -> listener.onPageUpButton()
			keyboard.pageDown -> listener.onPageDownButton()
			keyboard.doublePageUp -> listener.onDoublePageUpButton()
			keyboard.doublePageDown -> listener.onDoublePageDownButton()
			keyboard.exit -> listener.onExitButton()
			else -> false
		}
	}
	
	override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
		return true
	}
	
	override fun keyDown(keycode: Int): Boolean {
		return true
	}
	
	override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
		return true
	}
	
	override fun connected(controller: Controller?) {
	}
	
	override fun buttonUp(controller: Controller?, buttonCode: Int): Boolean {
		
		return processGamepadInput(buttonCode, controller)
	}
	
	private fun processGamepadInput(buttonCode: Int, controller: Controller?): Boolean {
		return if (controller != null) {
			val gamepad = config.gamepadConfig?.get(controller.name)
			if (gamepad != null) {
				when (buttonCode) {
					gamepad.confirm -> listener.onConfirmButton()
					gamepad.cancel -> listener.onCancelButton()
					gamepad.up -> listener.onUpButton()
					gamepad.down -> listener.onDownButton()
					gamepad.left -> listener.onLeftButton()
					gamepad.right -> listener.onRightButton()
					gamepad.find -> listener.onFindButton()
					gamepad.options -> listener.onOptionsButton()
					gamepad.select -> listener.onSelectButton()
					gamepad.pageUp -> listener.onPageUpButton()
					gamepad.pageDown -> listener.onPageDownButton()
					gamepad.doublePageUp -> listener.onDoublePageUpButton()
					gamepad.doublePageDown -> listener.onDoublePageDownButton()
					gamepad.exit -> listener.onExitButton()
					else -> false
				}
			} else {
				false
			}
		} else {
			false
		}
	}
	
	override fun ySliderMoved(controller: Controller?, sliderCode: Int, value: Boolean): Boolean {
		return true
	}
	
	override fun accelerometerMoved(controller: Controller?, accelerometerCode: Int, value: Vector3?): Boolean {
		return true
	}
	
	override fun axisMoved(controller: Controller?, axisCode: Int, value: Float): Boolean {
		//TODO trigger repeat???
		return processGamepadInput(translateAxisToButtonCode(axisCode, value), controller)
	}
	
	override fun disconnected(controller: Controller?) {
	}
	
	override fun xSliderMoved(controller: Controller?, sliderCode: Int, value: Boolean): Boolean {
		return true
	}
	
	override fun povMoved(controller: Controller?, povCode: Int, value: PovDirection?): Boolean {
		return processGamepadInput(translatePovToButtonCode(povCode, value), controller)
	}
	
	private fun  translatePovToButtonCode(povCode: Int, value: PovDirection?): Int {
		return povCode + (value?.ordinal ?: -1)
	}
	
	private fun  translateAxisToButtonCode(axisCode: Int, value: Float): Int {
		//TODO intensity??
		val x = if (value > 0.1f) { 1 } else { 0 }
		return axisCode + x
	}
	
	override fun buttonDown(controller: Controller?, buttonCode: Int): Boolean {
		return true
	}
	*/
	
	
}

