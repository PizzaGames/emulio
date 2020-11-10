package com.github.emulio.model.gamelist

data class Folder(
    /** the displayed name for the folder. */
    val name: String? = null,
    /** the description for the folder. */
    val desc: String? = null,
    /** the path to an image to display for the folder. */
    val image: String? = null,
    /** the path to a smaller image to display for the folder. */
    val thumbnail: String? = null,

    val games: List<Game>? = null
)