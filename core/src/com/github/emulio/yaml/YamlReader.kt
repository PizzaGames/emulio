package com.github.emulio.yaml

import com.github.emulio.model.PlatformConfig
import com.github.emulio.xml.XMLReader
import java.io.File

class YamlReader {
	
	fun parsePlatforms(yamlFile: File): List<PlatformConfig> {
		
		val yaml = YamlReaderHelper.parse(yamlFile)
		val platforms = mutableListOf<PlatformConfig>()

		
		val systems: Map<String, Map<String, *>> = yaml["systems"] as Map<String, Map<String, *>>
		
		for ((platformName, platform) in systems) {
			val platformName = platformName
			val romsExtensions = expandList(platform["roms.extensions"])
			val romsPath = File(platform["roms.path"] as String)
			val runCommand = expandList(platform["run.command"])
			
			platforms += PlatformConfig(romsPath, runCommand, romsExtensions, platformName)
		}
		
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

fun main(args: Array<String>) {
	for (i in 0..100000) stressTest()
}

private fun stressTest() {
	val start = System.currentTimeMillis()
	val platforms = YamlReader().parsePlatforms(File("sample-files/emulio-platforms.yaml"))
	val elapsed = System.currentTimeMillis() - start

//	platforms.forEach {
//		println(it)
//	}
	
	println("document parsed in: ${elapsed}ms")
}

