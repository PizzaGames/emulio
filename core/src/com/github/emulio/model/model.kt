package com.github.emulio.model


data class Game(
	val name: String,
	val path: String,
	val thumbnailPath: String,
	val description: String,
	val rating: Int
)

data class Platform(
	val name: String,
	val emulatorParams: List<String>
)