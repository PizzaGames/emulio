package com.github.emulio.ui.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.controllers.PovDirection
import com.github.emulio.model.EmulioConfig
import mu.KotlinLogging


class InputManager(val listener: InputListener, val config: EmulioConfig) {
	
	private val logger = KotlinLogging.logger {}

//	init {
//		Controllers.addListener(object : ControllerAdapter() {
//			override fun povMoved(controller: Controller?, povIndex: Int, value: PovDirection?): Boolean {
//				logger.debug { "povMoved $povIndex $value" }
//				return super.povMoved(controller, povIndex, value)
//			}
//		})
//	}
	
	val input = Gdx.input
	
	val keyboardCfg = config.keyboardConfig
	val gamepadsCfg = config.gamepadConfig

	class ControllerData {
		lateinit var controller: Controller

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
	}

	
	var controllers = updateControllers()
	
	fun reloadControllers() {
		controllers = updateControllers()
	}

	private fun updateControllers(): List<ControllerData> {
		return Controllers.getControllers().map {
			ControllerData().apply {
				controller = it
			}
		}
	}

	fun update(delta: Float) {
		updateControllers(delta)
		updateKey(delta)
	}




	fun updateController(contData: ControllerData, delta: Float) {
		val controller = contData.controller

		val gamepad = gamepadsCfg[controller.name] ?: return


//		if (!anyJoyPressed(controller)) {
//			joyAlreadyPressed = false
//			joyPressedElapsed = 0.0f
//			return
//		}

		contData.joyPressedElapsed += delta

		if (contData.joyAlreadyPressed && contData.joyPressedElapsed < 0.15f) {
			return
		}
		
		if (controller.getButton(gamepad.confirm)) {
			if (!contData.ConfirmButton) {
				listener.onConfirmButton()
			}
			contData.ConfirmButton = true
		} else if (controller.getButton(gamepad.cancel)) {
			if (!contData.CancelButton) {
				listener.onCancelButton()
			}
			contData.CancelButton = true
		} else if (controller.getButton(gamepad.find)) {
			if (!contData.FindButton) {
				listener.onFindButton()
			}
			contData.FindButton = true
		} else if (controller.getButton(gamepad.options)) {
			if (!contData.OptionsButton) {
				listener.onOptionsButton()
			}
			contData.OptionsButton = true
		} else if (controller.getButton(gamepad.select)) {
			if (!contData.SelectButton) {
				listener.onSelectButton()
			}
			contData.SelectButton = true
		} else if (controller.getButton(gamepad.exit)) {
			if (!contData.ExitButton) {
				listener.onExitButton()
			}
			contData.ExitButton = true
		} else if (controller.getButton(gamepad.up)) {
			if (!contData.UpButton) {
				listener.onUpButton(1f)
			}
			contData.UpButton = true
		} else if (controller.getButton(gamepad.down)) {
//			if (!contData.DownButton) {
				listener.onDownButton(1f)
//			}
//			contData.DownButton = true
		} else if (controller.getButton(gamepad.left)) {
//			if (!contData.LeftButton) {
				listener.onLeftButton(1f)
//			}
//			contData.LeftButton = true
		} else if (controller.getButton(gamepad.right)) {
//			if (!contData.RightButton) {
				listener.onRightButton(1f)
//			}
//			contData.RightButton = true
		} else if (controller.getButton(gamepad.pageUp)) {
//			if (!contData.PageUpButton) {
				listener.onPageUpButton(1f)
//			}
//			contData.PageUpButton = true
		} else if (controller.getButton(gamepad.pageDown)) {
//			if (!contData.PageDownButton) {
				listener.onPageDownButton(1f)
//			}
//			contData.PageDownButton = true
		} else {
			contData.joyAlreadyPressed = false
			contData.joyPressedElapsed = 0.0f

			contData.ConfirmButton = false
			contData.CancelButton = false
			contData.FindButton = false
			contData.OptionsButton = false
			contData.SelectButton = false
			contData.ExitButton = false
			contData.UpButton = false
			contData.DownButton = false
			contData.LeftButton = false
			contData.RightButton = false
			contData.PageUpButton = false
			contData.PageDownButton = false
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

		val lt = controller.getAxis(gamepad.axisTrigger)
		if (isOutsideDeadzone(lt)) {
			if (lt > 0f) {
				listener.onPageDownButton(calculateIntensity(lt))
			} else {
				listener.onPageUpButton(calculateIntensity(lt))
			}
		}


		val povDirection = controller.getPov(0)
		if (povDirection == PovDirection.north) {
			listener.onUpButton(1f)
		} else if (povDirection == PovDirection.south) {
			listener.onDownButton(1f)
		} else if (povDirection == PovDirection.west) {
			listener.onRightButton(1f)
		} else if (povDirection == PovDirection.east) {
			listener.onLeftButton(1f)
		}

		contData.joyAlreadyPressed = true
		contData.joyPressedElapsed = 0.0f

	}

	private fun isOutsideDeadzone(value: Float) = value > 0.15f || value < -0.15f


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
		controllers.forEach { data ->
			updateController(data, delta)
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

		if (keyAlreadyPressed && keyPressedElapsed < 0.15f) {
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

