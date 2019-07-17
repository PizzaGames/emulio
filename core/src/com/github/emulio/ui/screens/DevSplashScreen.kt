package com.github.emulio.ui.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.github.emulio.Emulio
import mu.KotlinLogging
import kotlin.math.min

class DevSplashScreen(emulio:Emulio) : EmulioScreen(emulio) {
    val logger = KotlinLogging.logger { }

    init {
        logger.debug { "create()" }

        stage.addActor(Image(Texture("images/libgdx.png")).apply {
            color.a = 0f
            x = (screenWidth - width) / 2
            y = (screenHeight - height) / 2

            addAction(SequenceAction(
                    Actions.delay(1f),
                    Actions.fadeIn(0.250f),
                    Actions.delay(1f),
                    Actions.fadeOut(0.250f),
                    Actions.run {
                        onCompleteAnimation()
                    }
            ))
        })

    }

    private fun onCompleteAnimation() {
        switchScreen(EmulioSplashScreen(emulio))
    }

    override fun release() {
        logger.debug { "release()" }
    }

    override fun hide() {
        logger.debug { "hide()" }
    }

    override fun render(delta: Float) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(min(Gdx.graphics.deltaTime, 1 / 30f))
        stage.draw()
    }

    override fun pause() {
        logger.debug { "pause()" }
    }

    override fun resume() {
        logger.debug { "resume()" }
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }
}