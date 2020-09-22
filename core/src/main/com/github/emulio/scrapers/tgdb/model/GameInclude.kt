package com.github.emulio.scrapers.tgdb.model

class GameInclude(private val boxart: Boolean = false,
                  private val publishers: Boolean = false) {
    fun fields(): String {
        return StringBuilder().apply {
            append(if (boxart) "icon," else "")
            append(if (publishers) "console," else "")
            if (length > 0) {
                setLength(length - 1)
            }

        }.toString()
    }
}