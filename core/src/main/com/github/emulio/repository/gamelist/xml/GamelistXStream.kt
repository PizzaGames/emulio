package com.github.emulio.repository.gamelist.xml

import com.github.emulio.model.gamelist.Folder
import com.github.emulio.model.gamelist.Game
import com.github.emulio.model.gamelist.GameList
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