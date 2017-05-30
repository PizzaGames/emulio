package com.github.emulio.runners

import com.badlogic.gdx.Gdx
import com.github.emulio.model.Platform
import com.github.emulio.model.theme.ViewImage
import com.github.emulio.model.theme.Theme
import com.github.emulio.xml.XMLReader
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import mu.KotlinLogging
import java.io.File

class ThemeReader {
	
	private val logger = KotlinLogging.logger {}

	fun readTheme(platforms: List<Platform>, themeDir: File): Flowable<Theme> {
		logger.info("Reading theme from all platforms")
		
		var theme = Flowable.empty<Theme>()
		platforms.forEach { platform ->
			logger.debug { "reading theme for platform: ${platform.platformName} " }
			theme = theme.concatWith(readPlatformTheme(platform, themeDir))
		}
		return theme
	}

	fun readPlatformTheme(platform: Platform, themeDir: File): Flowable<Theme> {
		return Flowable.create({ emitter ->
			val start = System.currentTimeMillis()
			
			val xmlFile = File(File(themeDir, platform.platformName), "theme.xml")
			logger.debug { "readPlatformTheme: reading theme from file '${xmlFile.absolutePath}'" }

			val theme = XMLReader().parseTheme(xmlFile).apply {
				this.platform = platform
			}
			
			convertImages(theme)

			emitter.onNext(theme)
			
			logger.info { "Theme of '${platform.platformName}' read in ${System.currentTimeMillis() - start}ms " }
			emitter.onComplete()
		}, BackpressureStrategy.BUFFER)
	}
	
	private fun convertImages(theme: Theme) {
		if (theme.includeTheme != null) {
			convertImages(theme.includeTheme!!)
		}
		
		theme.views?.forEach { view ->
			view.viewItems?.forEach { viewItem ->
				if (viewItem is ViewImage) {
					convertImage(viewItem)
				}
			}
		}
	}
	
	val pngConverter = PNGConverter()
	
	private fun convertImage(viewImage: ViewImage) {
		val imgFile = viewImage.path ?: return
		
		if (!imgFile.exists()) {
			return
		}
		
		if (imgFile.extension.toLowerCase() == "svg") {
			
			val sizeX = viewImage.sizeX
			val sizeY = viewImage.sizeY
			
			val maxSizeX = viewImage.maxSizeX
			val maxSizeY = viewImage.maxSizeY
			
			val graphics = Gdx.graphics
			val screenWidth = graphics.width
			val screenHeight = graphics.height
			
			val width = if (maxSizeX != null) {
				screenWidth * maxSizeX
			} else if (sizeX != null) {
				screenWidth * sizeX
			} else {
				null
			}
			
			val height = if (maxSizeY != null) {
				screenHeight * maxSizeY
			} else if (sizeY != null) {
				screenHeight * sizeY
			} else {
				null
			}
			
			val imgName = if (width == null && height == null) {
				"${imgFile.nameWithoutExtension}.png"
			} else {
				"${imgFile.nameWithoutExtension}_${width}x$height.png"
			}
			
			val pngFile = File(imgFile.parentFile, imgName)
			if (!pngFile.exists()) {
				logger.debug { "convertImage: Converting ${imgFile.name} image into ${pngFile.name}" }
				pngConverter.convertFromSVG(imgFile, pngFile, width, height)
			}
		}
	
	}
	
}

