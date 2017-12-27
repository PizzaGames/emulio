package com.github.emulio.utils

import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException

/**
 * Java implementation of the Unix tail command
 *
 * @param args[0]
 * File name
 * @param args[1]
 * Update time (seconds). Optional. Default value is 1 second
 *
 * @author Luigi Viggiano (original author)
 * http://it.newinstance.it/2005/11/19/listening-changes-on-a-text-file-
 * unix-tail-implementation-with-java/
 * @author Alessandro Melandri (modified by)
 */
class Tail {

//    internal var sleepTime: Long = 1000
//
//    @Throws(IOException::class)
//    @JvmStatic
//    fun main(args: Array<String>) {
//
//        if (args.size > 0) {
//
//            if (args.size > 1)
//                sleepTime = java.lang.Long.parseLong(args[1]) * 1000
//
//            val fileName = args[0]
//            val file = File(fileName)
//
//            tailFile(file)
//
//        } else {
//            println(
//                    "Missing parameter!\nUsage: java JavaTail fileName [updateTime (Seconds. default to 1 second)]")
//        }
//    }

    @Throws(FileNotFoundException::class, IOException::class)
    fun tailFile(file: File, sleepTime: Long) {
        val input = BufferedReader(FileReader(file))

        var currentLine: String? = input.readLine()
        while (true) {

            if (currentLine != null) {
                println(currentLine)
                currentLine = input.readLine()
                continue
            }

            try {
                Thread.sleep(sleepTime)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            }

        }
        input.close()
    }
}