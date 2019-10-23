package com.github.emulio.scrapers.thegamesdb

import java.nio.charset.Charset
import java.util.*

object TheGamesDBScraperV2 {
    
    fun key(): String {
        // FIXME I know.. this is not the ideal option to store the key. change later.
        return String(Base64.getDecoder().decode("==QZmNWZyIDM1kjMkZzM4IGMzQWYlNzM0UTNiNDZyUjNhVzN2YTMxkDOlhDMhNTM5MGZ1IzYkZWZxMjZmNDN0gDZ".reversed()), Charset.forName("UTF-8"))
    }


}