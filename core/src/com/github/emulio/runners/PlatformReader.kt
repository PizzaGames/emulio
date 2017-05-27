package com.github.emulio.runners


import com.github.emulio.model.Platform
import com.github.emulio.yaml.YamlReader
import java.io.File


class PlatformReader : Function0<List<Platform>> {
	override fun invoke(): List<Platform> {
		return YamlReader().parsePlatforms(File("emulio-platforms.yaml"))
	}
}

