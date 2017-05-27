package com.github.emulio.model

import java.io.File
import java.util.*

data class Game(
		val id: String?,
		val source: String?,
		val path: File,
		val name: String?,
		val description: String?,
		val image: File?,
		val releaseDate: Date?,
		val developer: String?,
		val publisher: String?,
		val genre: String?,
		val players: String?,
		val platform: Platform
) {
	constructor(path: File, platform: Platform) : this(null, null, path, path.name, null, null, null, null, null, null, null, platform)
}