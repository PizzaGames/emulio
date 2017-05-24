package com.github.emulio.ui.reactive

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Timer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * This is a RxJava scheduler that is responsible to route all runnables to run inside the Gdx thread.
 */
object GdxScheduler : Scheduler() {
	override fun createWorker(): Worker {
		return GdxWorker()
	}
}

private class GdxWorker : Scheduler.Worker() {
	
	@Volatile private var disposed: Boolean = false
	
	override fun isDisposed(): Boolean {
		return disposed
	}
	
	override fun schedule(runnable: Runnable, delay: Long, timeUnit: TimeUnit): Disposable {
		
		val disposable = object : Disposable {
			private var disposed: Boolean = false
			
			override fun dispose() {
				disposed = true
			}
			
			override fun isDisposed(): Boolean {
				return disposed
			}
		}
		
		if (!isDisposed) {
			val delaySeconds = timeUnit.toMillis(delay).toFloat() / 1000f
			if (delaySeconds > 0) {
				Timer.schedule(object : Timer.Task() {
					override fun run() {
						runnable.run()
					}
				}, delaySeconds)
			} else {
				Gdx.app.postRunnable {
					if (!disposable.isDisposed) {
						runnable.run()
					}
				}
			}

		}
		return disposable
	}
	
	override fun dispose() {
		disposed = true
	}
	
}


