package com.github.emulio.service.process

import mu.KotlinLogging
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * This is a runnable that responsibility is to consume a InputStream.
 * It is normally used in parallel running in another thread for instance.
 *
 * It was initially designed to consume all the output from a external process
 * started from Emulio.
 *
 * @see ProcessLauncherService
 */
internal class InputStreamConsumerRunnable(
        private val inputStream: InputStream,
        private val consumer: (line: String) -> Unit) : Runnable {

	val logger = KotlinLogging.logger { }

    override fun run() {
        logger.debug { "Initializing InputStreamConsumer" }

        BufferedReader(InputStreamReader(inputStream)).useLines {
            consumer(it.toString())
        }

        logger.debug { "InputStreamConsumer ended" }
    }

}
