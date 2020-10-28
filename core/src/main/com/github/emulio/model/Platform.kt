package com.github.emulio.model

import java.io.File

data class Platform(
	val romsPath: File,
	val runCommand: List<String>,
	val romsMode: RomsMode = RomsMode.NORMAL,
	val romsNaming: RomsNaming = RomsNaming.NORMAL,
	val romsExtensions: List<String>,
	val platformName: String,
    val name: String
)