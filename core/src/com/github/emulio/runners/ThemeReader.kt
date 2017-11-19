package com.github.emulio.runners

import com.badlogic.gdx.Gdx
import com.github.emulio.model.Platform
import com.github.emulio.model.theme.Theme
import com.github.emulio.model.theme.ViewImage
import com.github.emulio.xml.XMLReader
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import mu.KotlinLogging
import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.apache.batik.bridge.BridgeContext
import org.apache.batik.bridge.DocumentLoader
import org.apache.batik.bridge.GVTBuilder
import org.apache.batik.bridge.UserAgentAdapter
import org.apache.batik.util.XMLResourceDescriptor
import java.awt.geom.Rectangle2D
import java.io.File
import java.io.FileInputStream

const val FORCE_PNG_CONVERSION = true

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

	private fun readPlatformTheme(platform: Platform, themeDir: File): Flowable<Theme> {
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
	
	private val pngConverter = PNGConverter()
	
	private fun convertImage(viewImage: ViewImage) {
		val imgFile = viewImage.path ?: return
		
		if (!imgFile.exists()) {
			return
		}
		
		if (imgFile.extension.toLowerCase() == "svg") {
			
			val sizeX = viewImage.sizeX?.toDouble()
			val sizeY = viewImage.sizeY?.toDouble()
			
			val maxSizeX = viewImage.maxSizeX?.toDouble()
			val maxSizeY = viewImage.maxSizeY?.toDouble()
			
			val graphics = Gdx.graphics
			val screenWidth = graphics.width.toDouble()
			val screenHeight = graphics.height.toDouble()

			val preferredBounds = readPreferredBounds(imgFile)

			val preferredWidth = preferredBounds.width
			val preferredHeight = preferredBounds.height


			val preferredWidthCeil = Math.ceil(preferredWidth)
			var width = if (sizeX != null) {
				screenWidth * sizeX
			} else {
				preferredWidthCeil
			}
			
			if (maxSizeX != null) {
				width = Math.ceil(Math.min(width, (screenWidth * maxSizeX)))
			}

			val preferredHeightCeil = Math.ceil(preferredHeight)
			var height = if (sizeY != null) {
				screenHeight * sizeY
			} else {
				preferredHeightCeil
			}
			
			if (maxSizeY != null) {
				height = Math.ceil(Math.min(height, (screenHeight * maxSizeY)))
			}

			val preferredRatio = preferredWidthCeil / preferredHeightCeil
			val desiredRatio = width / height
			
			if (desiredRatio != preferredRatio) {
				println("Outside ratio!")

                val spx = width
				val spy = height
				
				val rw = preferredWidth / spx
				val rh = preferredHeight / spy
				
				val npw = preferredWidth / rw
				val nph = preferredHeight / rw
				
				if (spx >= npw && spy >= nph) {
					width = Math.ceil(npw)
					height = Math.ceil(nph)
				} else {
					width = Math.ceil(preferredWidth / rh)
					height = Math.ceil(preferredHeight / rh)
				}

			}

			val imgName = if (width == preferredWidth && height == preferredHeight) {
				"${imgFile.nameWithoutExtension}.png"
			} else {
				"${imgFile.nameWithoutExtension}_${width.toInt()}x${height.toInt()}.png"
			}
			
			val pngFile = File(imgFile.parentFile, imgName)

            if (!pngFile.exists() || FORCE_PNG_CONVERSION) {
				logger.debug { "convertImage: Converting ${imgFile.name} image into ${pngFile.name}" }
				pngConverter.convertFromSVG(imgFile, pngFile, width.toFloat(), height.toFloat())
				viewImage.path = pngFile
			} else {
				viewImage.path = pngFile
			}
		}

	}

	private fun readPreferredBounds(imgFile: File): Rectangle2D {
		return FileInputStream(imgFile).use { fis ->
			val factory = SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName())

			val document = factory.createDocument(imgFile.toURI().toURL().toString(), fis)
			val agent = UserAgentAdapter()
			val loader = DocumentLoader(agent)
			val context = BridgeContext(agent, loader)

			context.isDynamic = true
			val builder = GVTBuilder()
			val root = builder.build(context, document)

			root.primitiveBounds
		}
	}

}

