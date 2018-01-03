package com.github.emulio.process

import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import mu.KotlinLogging


object ProcessLauncher {

    val logger = KotlinLogging.logger { }

    fun executeProcess(commandArray: Array<String>) {
        logger.info { "executing process: [${commandArray.joinToString(" ")}]" }
        val runtime = Runtime.getRuntime()
        val process = runtime.exec(commandArray)

        //TODO avoid creation of new threads. Emulio will preferably habe only 4 threads (UI, FileFinder, ExecutionProcess, ProcessStreamPipe)

		Flowable.fromCallable({
			InputStreamConsumer(process.inputStream)
			InputStreamConsumer(process.errorStream)
		}).subscribeOn(Schedulers.newThread())

        val exitCode = process.waitFor() //blocks this thread waiting for execution to terminate
		logger.info { "process ended, returning to emulio main interface :)" }
        //TODO detect any return code problem here.

		if (exitCode > 0) {
			throw ProcessException("Error executing process [${commandArray.joinToString(" ")}], the return code was: [$exitCode], check log for more information.")
		}
    }
}

class ProcessException(message : String) : Exception(message)

