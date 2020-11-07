package com.github.emulio.gamelist.scrapers.tgdb.model

class PlatformImageFields(
        private val fanart: Boolean = false,
        private val banner: Boolean = false,
        private val boxart: Boolean = false) {
    fun fields(): String {
        return StringBuilder().apply {
            append(if (fanart) "fanart," else "")
            append(if (banner) "banner," else "")
            append(if (boxart) "boxart," else "")
            if (length > 0) {
                setLength(length - 1)
            }

        }.toString()
    }
}