package com.github.emulio.process

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
        val runtime = Runtime.getRuntime()
        val process = runtime.exec(command)

        //TODO avoid creation of new threads. Emulio will preferably have only 4 threads (UI, FileFinder, ExecutionProcess, ProcessStreamPipe)

		Flowable.fromCallable {
			InputStreamConsumer(process.inputStream)
			InputStreamConsumer(process.errorStream)
		}.subscribeOn(Schedulers.newThread())

        val exitCode = process.waitFor() //blocks this thread waiting for execution to terminate
		logger.info { "process ended, returning to emulio main interface :)" }

		//TODO detect any return code problem here.

		if (exitCode > 0) {
			throw ProcessException("Error executing process [$command], the return code was: [$exitCode], check log for more information.")
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
		} catch (e: ProcessException) {
			logger.trace(e) { "Unable to find vscode installed" }
			false
		}
	}

	private fun isWindows() = System.getProperty("os.name").toLowerCase().contains("windows")
}

class ProcessException(message : String) : Exception(message)

