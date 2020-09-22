package com.github.emulio.model

import java.io.File

enum class RomsMode {
	FLAT, NORMAL // GROUPED
}
enum class RomsNaming {
	FIRST_FOLDER, FOLDER, NORMAL
}

data class Platform(
	val romsPath: File,
	val runCommand: List<String>,
	val romsMode: RomsMode = RomsMode.NORMAL,
	val romsNaming: RomsNaming = RomsNaming.NORMAL,
	val romsExtensions: List<String>,
	val platformName: String,
    val name: String
)