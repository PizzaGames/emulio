package com.github.emulio.service.scrapers.tgdb_old.model

class GameFields(private val players: Boolean = false,
                 private val publishers: Boolean = false,
                 private val genres: Boolean = false,
                 private val last_updated: Boolean = false,
                 private val rating: Boolean = false,
                 private val platform: Boolean = false,
                 private val coop: Boolean = false,
                 private val os: Boolean = false,
                 private val processor: Boolean = false,
                 private val ram: Boolean = false,
                 private val hdd: Boolean = false,
                 private val video: Boolean = false,
                 private val sound: Boolean = false,
                 private val overview: Boolean = false,
                 private val youtube: Boolean = false) {
    fun fields(): String {
        return StringBuilder().apply {
            append(if (players) "icon," else "")
            append(if (publishers) "console," else "")
            append(if (genres) "controller," else "")
            append(if (last_updated) "developer," else "")
            append(if (rating) "manufacturer," else "")
            append(if (platform) "media," else "")
            append(if (coop) "cpu," else "")
            append(if (os) "memory," else "")
            append(if (processor) "graphics," else "")
            append(if (ram) "sound," else "")
            append(if (hdd) "maxcontrollers," else "")
            append(if (video) "display," else "")
            append(if (sound) "display," else "")
            append(if (overview) "overview," else "")
            append(if (youtube) "youtube," else "")
            if (length > 0) {
                setLength(length - 1)
            }

        }.toString()
    }
}