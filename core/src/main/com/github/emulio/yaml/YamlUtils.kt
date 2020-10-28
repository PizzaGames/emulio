package com.github.emulio.yaml

import com.github.emulio.model.Platform
import com.github.emulio.model.RomsMode
import com.github.emulio.model.RomsNaming
import com.github.emulio.model.config.EmulioConfig
import mu.KotlinLogging
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


object YamlUtils {

	val logger = KotlinLogging.logger { }
	
	fun parseEmulioConfig(yamlFile: File): EmulioConfig {
        FileInputStream(yamlFile).use {
            return getYaml().load(it) as EmulioConfig
        }
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
            val romsExtensions = expandList(platform["roms.extensions"])
			val romsPath = File(platform["roms.path"] as String)
			val romsMode = RomsMode.valueOf((platform["roms.mode"] as String? ?: "normal").toUpperCase())
			val romsNaming = RomsNaming.valueOf((platform["roms.naming"] as String? ?: "normal").toUpperCase())
			val runCommand = expandList(platform["run.command"])
			val name = (platform["platform.name"] as String?) ?: platformName

			platforms += Platform(romsPath, runCommand, romsMode, romsNaming, romsExtensions, platformName, name)
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

	fun parse(stream: InputStream): Map<*, *> {
		stream.use {
			val yaml = Yaml()
			val loaded = yaml.load(it)
			return if (loaded == null) {
				emptyMap<Any, Any>()
			} else {
				loaded as Map<*, *>
			}

		}
	}

    fun parse(yamlFile: File): Map<*, *> {
        check(yamlFile.exists())
        check(yamlFile.isFile)

		return parse(FileInputStream(yamlFile))
    }

}