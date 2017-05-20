package com.github.emulio.runners


import com.github.emulio.model.Platform
import com.github.emulio.yaml.YamlReader
import io.reactivex.functions.Function
import java.io.File


class PlatformReader : Function<Unit, List<Platform>> {

	override fun apply(t: Unit?): List<Platform> {
		return YamlReader().parsePlatforms(File("emulio-platforms.yaml"))
	}

}

