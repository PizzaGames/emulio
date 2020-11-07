package com.github.emulio.yaml

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

object ConfigYaml {

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