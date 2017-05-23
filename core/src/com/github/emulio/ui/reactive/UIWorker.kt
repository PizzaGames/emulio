package com.github.emulio.ui.reactive

import com.badlogic.gdx.Gdx
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class UIWorker : Scheduler.Worker() {
	
	@Volatile private var disposed: Boolean = false
	
	override fun isDisposed(): Boolean {
		return disposed
	}
	
	override fun schedule(run: Runnable, delay: Long, unit: TimeUnit?): Disposable {
		
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
			//TIMER e TIME UNIT
			if (delay == 0L && unit == null) {
				Gdx.app.postRunnable {
					if (!disposable.isDisposed) {
						run.run()
					}
				}
			} else {
				if (delay != 0L) {
				
				} else if (unit != null) {
				
				}
			}
		}
		
		return disposable
	}
	
	override fun dispose() {
		disposed = true
	}
	
}

class UIScheduler : Scheduler() {
	override fun createWorker(): Worker {
		return UIWorker()
	}
	//TODO
}
