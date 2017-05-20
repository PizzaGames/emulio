package com.github.emulio.yaml

import com.github.emulio.model.Platform
import mu.KotlinLogging
import java.io.File

class YamlReader {

	val logger = KotlinLogging.logger { }
	
	fun parsePlatforms(yamlFile: File): List<Platform> {

		val start = System.currentTimeMillis()
		logger.info { "Parsing '${yamlFile.absolutePath}'" }

		val yaml = YamlReaderHelper.parse(yamlFile)
		val platforms = mutableListOf<Platform>()

		
		val systems: Map<String, Map<String, *>> = yaml["systems"] as Map<String, Map<String, *>>
		
		for ((platformName, platform) in systems) {
			val platformName = platformName
			val romsExtensions = expandList(platform["roms.extensions"])
			val romsPath = File(platform["roms.path"] as String)
			val runCommand = expandList(platform["run.command"])
			
			platforms += Platform(romsPath, runCommand, romsExtensions, platformName)
		}

		logger.info { "Platform configuration file read in ${System.currentTimeMillis() - start}ms" }
		
		return platforms
	}
	
	private fun expandList(any: Any?): List<String> {
		if (any == null) {
			return emptyList()
		}
		
		if (any is String) {
			return listOf(any)
		}
		
		if (any is List<*>) {
			return any as List<String>
		}
		
		return emptyList()
	}
	
}