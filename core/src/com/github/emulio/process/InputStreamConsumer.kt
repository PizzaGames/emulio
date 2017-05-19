package com.github.emulio.process

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


class InputStreamConsumer(val inputStream: InputStream) : Runnable {
    override fun run() {
        try {
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))

            var line = bufferedReader.readLine()

            while (line != null) {
                println(">" + line)
                line = bufferedReader.readLine()
            }
        } catch (e: IOException) {
            error(e)
        }
    }
}
