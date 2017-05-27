package com.github.emulio.utils.gdxutils

import com.badlogic.gdx.graphics.GL20
import io.reactivex.Flowable

fun <T> Flowable<T>.Subscribe(onNext: (T) -> Unit, onError: (Throwable) -> Unit, onComplete: () -> Unit) {
	subscribe(onNext, onError, onComplete)
}

fun GL20.glClearColor(r: Int, g: Int, b: Int, alpha: Int) {
	this.glClearColor(r.toGLColor(), g.toGLColor(), b.toGLColor(), alpha.toGLColor())
}

fun Int.toGLColor(): Float {
	return this / 255.0f
}