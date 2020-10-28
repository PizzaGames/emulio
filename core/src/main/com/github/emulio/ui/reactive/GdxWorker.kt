package com.github.emulio.ui.reactive

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Timer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class GdxWorker : Scheduler.Worker() {

	@Volatile private var disposed: Boolean = false

	override fun isDisposed(): Boolean {
		return disposed
	}

	override fun schedule(runnable: Runnable,
						  delay: Long,
						  timeUnit: TimeUnit): Disposable {

		val disposable = disposable()
		if (!isDisposed) {
			val delaySeconds = timeUnit.toMillis(delay).toFloat() / 1000f
			if (delaySeconds > 0) {
				schedule(runnable, delaySeconds)
			} else {
				run(disposable, runnable)
			}
		}
		return disposable
	}

	private fun run(disposable: Disposable, runnable: Runnable) {
		Gdx.app.postRunnable {
			if (!disposable.isDisposed) {
				runnable.run()
			}
		}
	}

	private fun schedule(runnable: Runnable, delaySeconds: Float) {
		Timer.schedule(object : Timer.Task() {
			override fun run() {
				runnable.run()
			}
		}, delaySeconds)
	}

	private fun disposable(): Disposable {
		return object : Disposable {
			private var disposed: Boolean = false

			override fun dispose() {
				disposed = true
			}

			override fun isDisposed(): Boolean {
				return disposed
			}
		}
	}

	override fun dispose() {
		disposed = true
	}

}