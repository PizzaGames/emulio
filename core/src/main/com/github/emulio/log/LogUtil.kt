package com.github.emulio.log

import mu.KotlinLogging
import kotlin.system.measureTimeMillis

val logger = KotlinLogging.logger { }

inline fun <T> measure(message: String, block: () -> T): T {
    val result: T
    val time = measureTimeMillis {
        result = block()
    }
    logger.info { "$message took $time milliseconds" }
    return result
}