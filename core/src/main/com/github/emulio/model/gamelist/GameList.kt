package com.github.emulio.model.gamelist

data class GameList(
        val games: List<Game> = emptyList(),
        val folders: List<Folder> = emptyList()
)

