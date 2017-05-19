package com.github.emulio.model

import java.io.File
import java.util.*


data class GameInfo(
	val id: String,
	val source: String?,
	val path: File,
	val name: String?,
	val description: String?,
	val image: File?,
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

data class PlatformConfig(
    val romsPath: File,
    val runCommand: List<String >,
    val romsExtensions: List<String>,
    val platformName: String
)