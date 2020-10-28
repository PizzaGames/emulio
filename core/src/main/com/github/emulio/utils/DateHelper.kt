package com.github.emulio.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

// FIXME this class needs to die
object DateHelper {

    const val DF_SIMPLE_STRING = "yyyy-MM-dd"
    @JvmField val DF_SIMPLE_FORMAT = object : ThreadLocal<DateFormat>() {
        override fun initialValue(): DateFormat {
            return SimpleDateFormat(DF_SIMPLE_STRING, Locale.US)
        }
    }

    fun format(date: Date): String {
        return DF_SIMPLE_FORMAT.get().format(date)
    }
}
