package com.github.emulio.runners

import com.github.emulio.model.Platform
import com.github.emulio.model.theme.Theme
import com.github.emulio.xml.XMLReader
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import java.io.File

class ThemeReader {

	fun readTheme(platforms: List<Platform>, themeDir: File): Flowable<Theme> {
		var theme = Flowable.empty<Theme>()
		platforms.forEach { platform ->
			theme = theme.concatWith(readPlatform(platform, themeDir))
		}
		return theme
	}

	fun readPlatform(platform: Platform, themeDir: File): Flowable<Theme> {
		return Flowable.create({ emitter ->
			val xmlFile = File(File(themeDir, platform.platformName), "theme.xml")

			val theme = XMLReader().parseTheme(xmlFile).apply {
				this.platform = platform
			}

			emitter.onNext(theme)
			emitter.onComplete()
		}, BackpressureStrategy.BUFFER)
	}

}

