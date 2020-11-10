package com.github.emulio.model.yaml

import com.badlogic.gdx.utils.StreamUtils
import com.github.emulio.log.measure
import com.github.emulio.model.Platform
import com.github.emulio.model.RomsMode
import com.github.emulio.model.RomsNaming
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
		val preparedYaml = this.toMutableMap().apply {
			this["rom.raw"] = "\"%ROM_RAW%\""
			this["rom.file"] = "\"%ROM_RAW%\""
			this["rom"] = "\"%ROM%\""
			this["basename"] = "\"%BASENAME%\""
		}

		val properties = preparedYaml.toProperties()
		val propertyPlaceholder = PropertyPlaceholderHelper("\${", "}")
		properties.entries.forEach { entry ->
			val value = entry.value
			check(value is String)
			entry.setValue(propertyPlaceholder.replacePlaceholders(value, properties))
		}
		return properties.toYaml(preparedYaml)
	}

	private fun Map<String, Any?>.toYaml(referenceYaml: Map<String, Any?>,
										 prefix: String? = null): MutableMap<String, Any?> {

		return referenceYaml.entries.map { entry ->
			val key = getPrefixedKey(entry.key, prefix)
			val value = translateValue(entry.value, key)

			entry.key to value
		}.toMap().toMutableMap()
	}

	private fun Map<String, Any?>.translateValue(referenceValue: Any?, key: String): Any? {
		return when (referenceValue) {
			is String -> {
				this[key]
			}
			is List<*> -> {
				translateList(key, referenceValue)
			}
			is Map<*, *> -> {
				translateMap(key, referenceValue)
			}
			else -> {
				error("Illegal state")
			}
		}
	}

	private fun Map<String, Any?>.translateMap(key: String, referenceValue: Map<*, *>): MutableMap<String, Any?> {
		val filtered = this.filter { it.key.startsWith(key) }
		return filtered.toYaml(referenceValue.asMap(), key)
	}

	private fun Map<String, Any?>.translateList(key: String, referenceValue: List<*>): List<Any?> {
		val filtered = this.filter { it.key.startsWith("$key[") }

		return referenceValue.mapIndexed { index, _ ->
			filtered["$key[$index]"]
		}
	}

	private fun getPrefixedKey(key: String, prefix: String?): String {
		if (prefix == null) {
			return key
		}
		return "$prefix.$key"
	}

	private fun Map<String, Any?>.toProperties(prefix: String? = null): MutableMap<String, Any?> {
		val properties = mutableMapOf<String, Any?>()

		this.forEach { (key, value) ->
			val prefixedKey = getPrefixedKey(key, prefix)

			when (value) {
				is Map<*, *> -> {
					flatenMap(value, key, prefix, properties)
				}
				is List<*> -> {
					flatenList(properties, value, prefixedKey)
				}
				else -> {
					flatenString(value, properties, prefixedKey)
				}
			}
		}

		return properties
	}

	private fun flatenString(value: Any?, properties: MutableMap<String, Any?>, prefixedKey: String) {
		check(value is String) { "Only Map/List/String values are supported in yaml" }
		properties[prefixedKey] = value
	}

	private fun flatenList(properties: MutableMap<String, Any?>, value: List<*>, prefixedKey: String) {
		properties.putAll(listToProperties(value.asList(), prefixedKey))
	}

	private fun flatenMap(value: Map<*, *>, key: String, prefix: String?, properties: MutableMap<String, Any?>) {
		val toProperties = value.asMap()
				.toProperties(key)
		val from = toProperties
				.map {
					getPrefixedKey(it.key, prefix) to it.value
				}
				.toMap()
		properties.putAll(from)
	}

	private fun listToProperties(list: List<String>, key: String): Map<out String, Any?> {
		return list.mapIndexed { index, value ->
			"$key[$index]" to value
		}.toMap()
	}

	private fun loadYaml(from: File): Map<String, Any?> {
		return FileInputStream(from).use {
			Yaml().load(it)
		}.asMap()
	}

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


	private fun expandList(any: Any?): List<String> {
		if (any == null) {
			return emptyList()
		}
		if (any is String) {
			return listOf(any)
		}
		if (any is List<*>) {
			return any.asList()
		}
		return emptyList()
	}

	@Suppress("UNCHECKED_CAST")
	private fun Any.asMap(): Map<String, Any?> = this as Map<String, Any?>

	@Suppress("UNCHECKED_CAST")
	private fun Any.asList(): List<String> = this as List<String>
}







