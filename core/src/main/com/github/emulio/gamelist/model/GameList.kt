package com.github.emulio.gamelist.model

data class GameList(
        val games: List<Game> = emptyList(),
        val folders: List<Folder> = emptyList()
)

