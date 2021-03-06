package com.github.emulio.ui.reactive

import io.reactivex.Scheduler

/**
 * This is a RxJava scheduler that is responsible to route all runnables to run inside the Gdx thread.
 */
object GdxScheduler : Scheduler() {
	override fun createWorker(): Worker {
		return GdxWorker()
	}
}


