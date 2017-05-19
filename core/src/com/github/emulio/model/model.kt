package com.github.emulio.model

import java.util.*


data class GameInfo(
    val id: String,
    val source: String?,
    val path: String,
    val name: String?,
    val description: String?,
    val image: String?,
    val releaseDate: Date?,
    val developer: String?,
    val publisher: String?,
    val genre: String?,
    val players: String?
)

data class Config(
    val screenWidth: Int,
    val screenHeight: Int,
    val fullscreen: Boolean,
    val vsync: Boolean,
    val theme: String
)

data class Platform(
    val romsPath: String,
    val runCommand: List<String >,
    val romsExtensions: List<String>,
    val platformName: String
)