package com.github.emulio

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import mu.KotlinLogging




class Emulio : ApplicationAdapter() {

	val logger = KotlinLogging.logger { }

	val FONT_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;,{}\"´`'<>"

	lateinit var batch: SpriteBatch

    lateinit var img: Texture
	lateinit var font: BitmapFont

    override fun create() {
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")



        //Gdx.graphics.setFullscreenMode()
    }



	override fun render() {

        Gdx.gl.glClearColor(0xFF.toGLColor(), 0xFF.toGLColor(), 0xFF.toGLColor(), 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.begin()
        batch.draw(img, 50f, 50f)
        batch.end()


    }

    override fun dispose() {
        batch.dispose()
        img.dispose()
    }
}

//private fun GL20.glClearColor(argb: Int) {
//
//	val alpha = (argb >> 24) and 0xFF
//	val r =  (argb >> 16) and 0xFF
//	val g =  (argb >> 8) and 0xFF
//	val b =  (argb) and 0xFF
//
//	this.glClearColor(r.toGLColor(), g.toGLColor(), b.toGLColor(), alpha.toGLColor())
//}
private fun GL20.glClearColor(r: Int, g: Int, b: Int, alpha: Int) {
	this.glClearColor(r.toGLColor(), g.toGLColor(), b.toGLColor(), alpha.toGLColor())
}
private fun Int.toGLColor(): Float {
	return this / 255.0f
}
