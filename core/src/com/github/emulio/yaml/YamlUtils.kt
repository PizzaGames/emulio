package com.github.emulio.yaml

import com.github.emulio.Emulio
import com.github.emulio.model.EmulioConfig
import com.github.emulio.model.Platform
import mu.KotlinLogging
import org.yaml.snakeyaml.Yaml
import java.io.File
import org.yaml.snakeyaml.DumperOptions



class YamlUtils {

	val logger = KotlinLogging.logger { }
	
	fun parseEmulioConfig(yamlFile: File): EmulioConfig {
		return getYaml().load(yamlFile.readText()) as EmulioConfig
	}
	
	fun saveEmulioConfig(yamlFile: File, config: EmulioConfig) {
		val yaml = getYaml().dump(config)
		yamlFile.writeText(yaml)
	}
	
	private fun getYaml(): Yaml {
		val options = DumperOptions()
		options.defaultFlowStyle = DumperOptions.FlowStyle.FLOW
		options.isPrettyFlow = true
		return Yaml(options)
	}
	
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