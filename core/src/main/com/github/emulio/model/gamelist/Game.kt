package com.github.emulio.model.gamelist

import java.util.*

data class Game(
    /** the displayed name for the game. */
    val name: String? = null,
    /** a description of the game. Longer descriptions will automatically scroll, so don't worry about size. */
    val desc: String? = null,
    /** the path to an image to display for the game (like box art or a screenshot). */
    val image: String? = null,
    /** the path to a smaller image, displayed in image lists like the grid view. Should be small to ensure quick loading. */
    val thumbnail: String? = null,
    /** the path to a video to display for the game, for themes that support the video viewstyle. */
    val video: String? = null,
    /** the rating for the game, expressed as a floating point number between 0 and 1.
     * Arbitrary values are fine (ES can display half-stars, quarter-stars, etc). */
    val rating: Float? = null,
    /** the date the game was released. Displayed as date only, time is ignored. */
    val releasedate: Date? = null,
    /** the developer for the game. */
    val developer: String? = null,
    /** the publisher for the game. */
    val publisher: String? = null,
    /** the (primary) genre for the game. */
    val genre: String? = null,
    /** the number of players the game supports. */
    val players: Int? = null,
    /** the number of times this game has been played. */
    val playcount: Int? = null,
    /** the last date and time this game was played. */
    val lastplayed: Date? = null,
    /** used in sorting the gamelist in a system, instead of name. */
    val sortname: String? = name
)



