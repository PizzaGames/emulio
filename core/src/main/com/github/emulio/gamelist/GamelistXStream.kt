package com.github.emulio.gamelist

import com.github.emulio.gamelist.model.Folder
import com.github.emulio.gamelist.model.Game
import com.github.emulio.gamelist.model.GameList
import com.thoughtworks.xstream.XStream

object GamelistXStream {

    val stax by lazy {
        XStream().apply { this.configureXstream() }
    }

    private fun XStream.configureXstream() {
        alias("game", Game::class.java)
        alias("gameList", GameList::class.java)
        alias("folder", Folder::class.java)

        addImplicitArray(GameList::class.java, "games", Game::class.java)
        addImplicitArray(Folder::class.java, "games", Game::class.java)
        addImplicitArray(GameList::class.java, "folders", Folder::class.java)

        setMode(XStream.NO_REFERENCES)
    }

}