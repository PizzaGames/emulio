package com.github.emulio.gamelist.scrapers.tgdb.model

class PlatformFields(private val icon: Boolean = false,
                     private val console: Boolean = false,
                     private val controller: Boolean = false,
                     private val developer: Boolean = false,
                     private val manufacturer: Boolean = false,
                     private val media: Boolean = false,
                     private val cpu: Boolean = false,
                     private val memory: Boolean = false,
                     private val graphics: Boolean = false,
                     private val sound: Boolean = false,
                     private val maxcontrollers: Boolean = false,
                     private val display: Boolean = false,
                     private val overview: Boolean = false,
                     private val youtube: Boolean = false) {
    fun fields(): String {
        return StringBuilder().apply {
            append(if (icon) "icon," else "")
            append(if (console) "console," else "")
            append(if (controller) "controller," else "")
            append(if (developer) "developer," else "")
            append(if (manufacturer) "manufacturer," else "")
            append(if (media) "media," else "")
            append(if (cpu) "cpu," else "")
            append(if (memory) "memory," else "")
            append(if (graphics) "graphics," else "")
            append(if (sound) "sound," else "")
            append(if (maxcontrollers) "maxcontrollers," else "")
            append(if (display) "display," else "")
            append(if (overview) "overview," else "")
            append(if (youtube) "youtube," else "")
            if (length > 0) {
                setLength(length - 1)
            }

        }.toString()
    }
}