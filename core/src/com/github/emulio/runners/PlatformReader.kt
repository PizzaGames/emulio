package com.github.emulio.runners


import com.github.emulio.model.Platform
import com.github.emulio.yaml.YamlUtils
import java.io.File


class PlatformReader : Function0<List<Platform>> {
	override fun invoke(): List<Platform> = YamlUtils().parsePlatforms(File("emulio-platforms.yaml"))
}

