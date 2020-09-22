package com.github.emulio.scrapers.tgdb.model

class GameImageFields(private val fanart: Boolean = false,
                      private val banner: Boolean = false,
                      private val boxart: Boolean = false,
                      private val screenshot: Boolean = false,
                      private val clearlogo: Boolean = false) {
    fun fields(): String {
        return StringBuilder().apply {
            append(if (fanart) "icon," else "")
            append(if (banner) "console," else "")
            append(if (boxart) "controller," else "")
            append(if (screenshot) "developer," else "")
            append(if (clearlogo) "manufacturer," else "")
            if (length > 0) {
                setLength(length - 1)
            }

        }.toString()
    }
}