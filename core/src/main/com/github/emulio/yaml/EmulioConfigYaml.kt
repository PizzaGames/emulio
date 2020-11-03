package com.github.emulio.yaml

import com.github.emulio.model.config.EmulioConfig
import com.github.emulio.utils.measure
import mu.KotlinLogging
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File

object EmulioConfigYaml {

	val logger = KotlinLogging.logger { }

	private val yaml by lazy {
		logger.debug { "Initializing yaml for EmulioConfig" }
		val options = DumperOptions().apply {
			defaultFlowStyle = DumperOptions.FlowStyle.FLOW
			isPrettyFlow = true
		}

        Yaml(options)
	}

	fun read(from: File): EmulioConfig {
		return measure("read EmulioConfig from ${from.absolutePath}") {
			val fileContent = from.readText()
			yaml.load(fileContent) as EmulioConfig
		}
	}

	fun save(emulioConfig: EmulioConfig, to: File) {
		measure("saving EmulioConfig to: ${to.absolutePath}") {
			val yamlLoaded = yaml.dump(emulioConfig)
			to.writeText(yamlLoaded)
		}
	}

}

