package com.github.emulio.ui.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.controllers.mappings.Xbox
import com.github.emulio.model.EmulioConfig
import mu.KotlinLogging


class InputManager(val listener: InputListener, val config: EmulioConfig) {
	
	private val logger = KotlinLogging.logger {}
	
	val input = Gdx.input
	
	val keyboardCfg = config.keyboardConfig
	val gamepadsCfg = config.gamepadConfig
	
	var controllers = Controllers.getControllers()
	
	fun reloadControllers() {
		controllers = Controllers.getControllers()
	}
	
	fun update(delta: Float) {
//		updateControllers(delta)
		updateKey(delta)
	}

	var joyAlreadyPressed = false
	var joyPressedElapsed = 0.0f
	
	fun updateController(controller: Controller, delta: Float) {
		val gamepad = gamepadsCfg[controller.name] ?: return

//		if (!anyJoyPressed(controller)) {
//			joyAlreadyPressed = false
//			joyPressedElapsed = 0.0f
//			return
//		}
//
//		joyPressedElapsed += delta
//
//		if (joyAlreadyPressed && joyPressedElapsed < 0.09f) {
//			return
//		}
		
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

//		joyAlreadyPressed = true
//		joyPressedElapsed = 0.0f

	}

	private fun anyJoyPressed(controller: Controller): Boolean {

		return controller.getButton(-1)
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
		controllers.forEach { controller ->
			updateController(controller, delta)
		}
	}

	var keyAlreadyPressed = false
	var keyPressedElapsed = 0.0f
	
	private fun updateKey(delta: Float) {

		if (!anyKeyPressed()) {
			keyAlreadyPressed = false
			keyPressedElapsed = 0.0f
			return
		}

		keyPressedElapsed += delta

		if (keyAlreadyPressed && keyPressedElapsed < 0.09f) {
			return
		}

		if (input.isKeyJustPressed(keyboardCfg.confirm)) {
			listener.onConfirmButton()
		} else if (input.isKeyJustPressed(keyboardCfg.cancel)) {
			listener.onCancelButton()
		} else if (input.isKeyJustPressed(keyboardCfg.find)) {
			listener.onFindButton()
		} else if (input.isKeyJustPressed(keyboardCfg.options)) {
			listener.onOptionsButton()
		} else if (input.isKeyJustPressed(keyboardCfg.select)) {
			listener.onSelectButton()
		} else if (input.isKeyJustPressed(keyboardCfg.exit)) {
			listener.onExitButton()
		} else if (input.isKeyPressed(keyboardCfg.up)) {
			listener.onUpButton(1f)
		} else if (input.isKeyPressed(keyboardCfg.down)) {
			listener.onDownButton(1f)
		} else if (input.isKeyPressed(keyboardCfg.left)) {
			listener.onLeftButton(1f)
		} else if (input.isKeyPressed(keyboardCfg.right)) {
			listener.onRightButton(1f)
		} else if (input.isKeyPressed(keyboardCfg.pageUp)) {
			listener.onPageUpButton(1f)
		} else if (input.isKeyPressed(keyboardCfg.pageDown)) {
			listener.onPageDownButton(1f)
		}

		keyAlreadyPressed = true
		keyPressedElapsed = 0.0f
	}

	private fun anyKeyPressed() = Gdx.input.isKeyPressed(Input.Keys.ANY_KEY)


}

