package com.github.emulio.process

import com.github.emulio.exceptions.ProcessCreationException
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import mu.KotlinLogging
import java.awt.Desktop
import java.io.File


object ProcessLauncher {

    val logger = KotlinLogging.logger { }

    fun executeProcess(commandArray: Array<String>) {
		val command = commandArray.joinToString(" ")

		logger.info { "executing process: [$command]" }
		val exitCode = execute(command)
		logger.info { "process ended, returning to emulio main interface :)" }

		if (exitCode > 0) {
			throw ProcessCreationException("Error executing process [$command], the return code " +
					"was: [$exitCode], check log for more information.")
		}
    }

	private fun execute(command: String): Int {
		val runtime = Runtime.getRuntime()
		val process = runtime.exec(command)

		listenProcessStreams(process)

		return process.waitFor()
	}

	private fun listenProcessStreams(process: Process) {
		//TODO avoid creation of new threads. Emulio will preferably have only 4 threads (UI, FileFinder, ExecutionProcess, ProcessStreamPipe)
		Flowable.fromCallable {
			consumeProcessOutput(process)
		}.subscribeOn(Schedulers.newThread())
	}

	private fun consumeProcessOutput(process: Process) {
		InputStreamConsumerRunnable(process.inputStream) {
			logger.info { "process output: $it" }
		}
		InputStreamConsumerRunnable(process.errorStream) {
			logger.error { "process err output: $it" }
		}
	}

	fun editFile(file: File) {
		if (openOnCode(file)) return

		logger.info { "VSCode editor not found, falling back to the default editor from OS" }
		// BUG: enable this only in windows xp and 2003
//		if (isWindows()) {
//			val cmd = "rundll32 url.dll,FileProtocolHandler " + file.canonicalPath
//			Runtime.getRuntime().exec(cmd)
//		} else {
//			Desktop.getDesktop().edit(file)
//		}
		Desktop.getDesktop().edit(file)
	}

	private fun openOnCode(file: File): Boolean {
		return try {
			val args = if (isWindows()) {
				listOf("cmd", "/c", "code", file.absolutePath)
			} else {
				listOf("code", file.absolutePath)
			}
			executeProcess(args.toTypedArray())
			true
		} catch (e: ProcessCreationException) {
			logger.trace(e) { "Unable to find vscode installed" }
			false
		}
	}

	private fun isWindows() = System.getProperty("os.name").toLowerCase().contains("windows")
}

