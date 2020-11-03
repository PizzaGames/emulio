package com.github.emulio.yaml

import com.badlogic.gdx.utils.StreamUtils
import com.github.emulio.model.Platform
import com.github.emulio.model.RomsMode
import com.github.emulio.model.RomsNaming
import com.github.emulio.utils.measure
import mu.KotlinLogging
import org.springframework.util.PropertyPlaceholderHelper
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


object PlatformConfigYaml {

	val logger = KotlinLogging.logger { }
	
	fun read(from: File): List<Platform> {
		return measure("Reading PlatformConfig from ${from.absolutePath}") {
			if (!from.exists()) {
				createPlatformConfig(from)
			}

			val yaml = readConfigYamlFile(from)

			getSystems(yaml).map { (platformName, platform) ->
				readPlatformFromYaml(platformName, platform)
			}

			emptyList()
		}
	}

	private fun readPlatformFromYaml(platformName: String, platform: Map<String, *>): Platform {
		logger.debug { "reading platform: $platformName; $platform" }
		val romsExtensions = expandList(platform["roms.extensions"])
		val romsPath = File(platform["roms.path"] as String)
		val romsMode = RomsMode.valueOf((platform["roms.mode"] as String? ?: "normal").toUpperCase())
		val romsNaming = RomsNaming.valueOf((platform["roms.naming"] as String? ?: "normal").toUpperCase())
		val runCommand = expandList(platform["run.command"])
		val name = (platform["platform.name"] as String?) ?: platformName

		return Platform(romsPath, runCommand, romsMode, romsNaming, romsExtensions, platformName, name)
	}

	@Suppress("UNCHECKED_CAST")
	private fun getSystems(yaml: Map<String, Any?>) =
			yaml["systems"] as Map<String, Map<String, *>>

	private fun readConfigYamlFile(from: File) = loadYaml(from).preparePlatformYaml()

	private fun Map<String, Any?>.preparePlatformYaml(): Map<String, Any?> {
		val preparedYaml = this.toMutableMap()
		preparedYaml["rom.raw"] = "\"%ROM_RAW%\""
		preparedYaml["rom.file"] = "\"%ROM_RAW%\""
		preparedYaml["rom"] = "\"%ROM%\""
		preparedYaml["basename"] = "\"%BASENAME%\""

		val properties = preparedYaml.toProperties()

		val propertyPlaceholder = PropertyPlaceholderHelper("\${", "}")
		properties.entries.forEach { entry ->
			val value = entry.value
			check(value is String)
			entry.setValue(propertyPlaceholder.replacePlaceholders(value, properties))
		}

		val yamlMap = properties.toYamlMap()
		return yamlMap
	}

	private fun Map<String, Any?>.toYamlMap(prefix: String? = null): MutableMap<String, Any?> {
		return this.entries
			.map { entry ->
				val (key, value) = entry

				key to ""
			}
			.toMap()
			.toMutableMap()

		//		this.entries.forEach { entry ->
//			val key = entry.key
//			val value = entry.value
//			when {
//				value is Map<*, *> -> {
//					value.toMutableMap().deflaten(flatened, key)
//				}
//				prefix != null -> {
//					entry.setValue(flatened["$prefix.$key"])
//				}
//				else -> {
//					entry.setValue(flatened[key])
//				}
//			}
//		}
	}

	private fun Map<String, Any?>.toProperties(prefix: String? = null): MutableMap<String, Any?> {
		val properties = mutableMapOf<String, Any?>()

		this.forEach { (key, value) ->
			val prefixedKey = getPrefixKey(prefix, key)

			when (value) {
				is Map<*, *> -> {
					properties.putAll(value.asMap().toProperties(key))
				}
				is List<*> -> {
					properties.putAll(listToProperties(value.asList(), prefixedKey))
				}
				else -> {
					check(value is String) { "Only Map/List/String values are supported in yaml" }
					properties[prefixedKey] = value
				}
			}
		}

		return properties
	}

	private fun getPrefixKey(prefix: String?, key: String): String {
		return if (prefix != null) {
			"$prefix.$key"
		} else {
			key
		}
	}

	private fun listToProperties(list: List<String>, key: String): Map<out String, Any?> {
		return list.mapIndexed { index, value ->
			"$key[$index]" to value
		}.toMap()
	}

	@Suppress("UNCHECKED_CAST")
	private fun Any.asMap(): Map<String, Any?> = this as Map<String, Any?>

	@Suppress("UNCHECKED_CAST")
	private fun Any.asList(): List<String> = this as List<String>

//	private fun MutableMap<String, Any?>.expandVars() {
//
//		val flatened = mutableMapOf<String, Any?>()
//		flatened.flaten(this)
//		flatened.expand()
//
//		this.deflaten(flatened)
//	}

//	private fun MutableMap<String, Any?>.flaten(origin: MutableMap<String, Any?>, prefix: String? = null): MutableMap<String, Any?> {
//		origin.entries.forEach { entry ->
//			val (key, value) = entry
//			when {
//				value is Map<*, *> -> {
//					entry.setValue(value.toMutableMap().flaten(this, key))
//				}
//				prefix != null -> {
//					this["$prefix.$key"] = value
//				}
//				else -> {
//					this[key] = value
//				}
//			}
//		}
//		return this
//	}
//
//	private fun MutableMap<String, Any?>.expand(): MutableMap<String, Any?> {
//		val helper = PropertyPlaceholderHelper("\${", "}")
//
//		this.entries.forEach { entry ->
//			val value = entry.value
//
//			if (value is String) {
//				entry.setValue(helper.replacePlaceholders(value, this))
//			} else if (value is List<*>) {
//				val list = value.toMutableList()
//				for (i in list.indices) {
//					list[i] = helper.replacePlaceholders(list[i], this)
//				}
//			}
//		}
//
//		return this
//	}
//
//	private fun MutableMap<String, Any?>.deflaten(flatened: MutableMap<String, Any?>, prefix: String? = null) {
//		this.entries.forEach { entry ->
//			val key = entry.key
//			val value = entry.value
//			when {
//				value is Map<*, *> -> {
//					value.toMutableMap().deflaten(flatened, key)
//				}
//				prefix != null -> {
//					entry.setValue(flatened["$prefix.$key"])
//				}
//				else -> {
//					entry.setValue(flatened[key])
//				}
//			}
//		}
//	}

	private fun loadYaml(from: File): Map<String, Any?> {
		return FileInputStream(from).use {
			Yaml().load(it)
		}.asMap()
	}

	@Suppress("UNCHECKED_CAST")
	private fun Any.toMutableList() = this as MutableList<String>



	private fun createPlatformConfig(platformsConfigFile: File) {
		logger.info("Creating emulio-platforms.yaml blank file.")

		try {
			logger.debug { "copying internal resource into external file" }
			StreamUtils.copyStream(
					javaClass.getResourceAsStream("/emulio-platforms.yaml"),
					FileOutputStream(platformsConfigFile)
			)
		} catch (exception: IOException) {
			throw RuntimeException(exception)
		}
	}

	@Suppress("UNCHECKED_CAST")
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







