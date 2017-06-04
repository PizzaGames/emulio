package com.github.emulio.ui.screens

import com.badlogic.gdx.Game
import com.badlogic.gdx.Screen
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.github.emulio.Emulio

abstract class EmulioScreen(val emulio: Emulio) : Screen {

	val stage: Stage = Stage()

	override fun show() {
		stage.root.color.a = 0f
		stage.root.addAction(Actions.fadeIn(0.5f))
	}

	fun switchScreen(newScreen: Screen) {
		stage.root.color.a = 1f
		val sequenceAction = SequenceAction()
		sequenceAction.addAction(Actions.fadeOut(0.5f))
		sequenceAction.addAction(Actions.run({ emulio.screen = newScreen }))
		stage.root.addAction(sequenceAction)
	}
}