package com.github.emulio.process

import mu.KotlinLogging


class ProcessLauncher {

    val logger = KotlinLogging.logger {}

    fun executeProcess(commandArray: Array<String>) {
        val runtime = Runtime.getRuntime()
        val process = runtime.exec(commandArray)

        //TODO avoid creation of new threads. Emulio will preferably habe only 4 threads (UI, FileFinder, ExecutionProcess, ProcessStreamPipe)
        Thread({
            InputStreamConsumer(process.inputStream)
            InputStreamConsumer(process.errorStream)
        }).start()

        val exitCode = process.waitFor() //blocks this thread waiting for execution to terminate
        //TODO detect any return code problem here.
    }
}

