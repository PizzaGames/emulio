package com.github.emulio.ui.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.Controllers
import com.github.emulio.model.EmulioConfig
import com.github.emulio.model.Xbox
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
		updateControllers(delta)
		updateKey(delta)
	}

	var joyAlreadyPressed = false
	var joyPressedElapsed = 0.0f

	var ConfirmButton = false
	var CancelButton = false
	var FindButton = false
	var OptionsButton = false
	var SelectButton = false
	var ExitButton = false
	var UpButton = false
	var DownButton = false
	var LeftButton = false
	var RightButton = false
	var PageUpButton = false
	var PageDownButton = false


	fun updateController(controller: Controller, delta: Float) {
		val gamepad = gamepadsCfg[controller.name] ?: return


//		if (!anyJoyPressed(controller)) {
//			joyAlreadyPressed = false
//			joyPressedElapsed = 0.0f
//			return
//		}

		joyPressedElapsed += delta

		if (joyAlreadyPressed && joyPressedElapsed < 0.08f) {
			return
		}
		
		if (controller.getButton(gamepad.confirm)) {
			if (!ConfirmButton) {
				listener.onConfirmButton()
			}
			ConfirmButton = true
		} else if (controller.getButton(gamepad.cancel)) {
			if (!CancelButton) {
				listener.onCancelButton()
			}
			CancelButton = true
		} else if (controller.getButton(gamepad.find)) {
			if (!FindButton) {
				listener.onFindButton()
			}
			FindButton = true
		} else if (controller.getButton(gamepad.options)) {
			if (!OptionsButton) {
				listener.onOptionsButton()
			}
			OptionsButton = true
		} else if (controller.getButton(gamepad.select)) {
			if (!SelectButton) {
				listener.onSelectButton()
			}
			SelectButton = true
		} else if (controller.getButton(gamepad.exit)) {
			if (!ExitButton) {
				listener.onExitButton()
			}
			ExitButton = true
		} else if (controller.getButton(gamepad.up)) {
			if (!UpButton) {
				listener.onUpButton(1f)
			}
			UpButton = true
		} else if (controller.getButton(gamepad.down)) {
			if (!DownButton) {
				listener.onDownButton(1f)
			}
			DownButton = true
		} else if (controller.getButton(gamepad.left)) {
			if (!LeftButton) {
				listener.onLeftButton(1f)
			}
			LeftButton = true
		} else if (controller.getButton(gamepad.right)) {
			if (!RightButton) {
				listener.onRightButton(1f)
			}
			RightButton = true
		} else if (controller.getButton(gamepad.pageUp)) {
			if (!PageUpButton) {
				listener.onPageUpButton(1f)
			}
			PageUpButton = true
		} else if (controller.getButton(gamepad.pageDown)) {
			if (!PageDownButton) {
				listener.onPageDownButton(1f)
			}
			PageDownButton = true
		} else {
			joyAlreadyPressed = false
			joyPressedElapsed = 0.0f

			ConfirmButton = false
			CancelButton = false
			FindButton = false
			OptionsButton = false
			SelectButton = false
			ExitButton = false
			UpButton = false
			DownButton = false
			LeftButton = false
			RightButton = false
			PageUpButton = false
			PageDownButton = false
		}
		
		val x = controller.getAxis(gamepad.axisX)
		if (isOutsideDeadzone(x)) {
			if (x > 0) {
				listener.onRightButton(calculateIntensity(x))
			} else {
				listener.onLeftButton(calculateIntensity(x))
			}
		}


		val y = controller.getAxis(gamepad.axisY)
		if (isOutsideDeadzone(y)) {
			if (y > 0f) {
				listener.onUpButton(calculateIntensity(y))
			} else {
				listener.onDownButton(calculateIntensity(y))
			}
		}

		val lt = controller.getAxis(gamepad.lTrigger)
		if (isOutsideDeadzone(lt)) {
			if (lt > 0f) {
				listener.onPageUpButton(calculateIntensity(lt))
			}
		}
		val rt = controller.getAxis(gamepad.rTrigger)
		if (isOutsideDeadzone(rt)) {
			if (rt > 0f) {
				listener.onPageDownButton(calculateIntensity(lt))
			}
		}

		joyAlreadyPressed = true
		joyPressedElapsed = 0.0f

	}

	private fun isOutsideDeadzone(value: Float) = value > 0.15f || value < -0.15f

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

