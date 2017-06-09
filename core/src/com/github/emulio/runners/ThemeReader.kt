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

			val preferredBounds = readPreferredBounds(imgFile)

			val preferredWidth = preferredBounds.width
			val preferredHeight = preferredBounds.height


			val preferredWidthCeil = Math.ceil(preferredWidth)
			var width = if (sizeX != null) {
				screenWidth * sizeX
			} else {
				preferredWidthCeil.toFloat()
			}

			val widthDouble = width.toDouble()
			if (maxSizeX != null) {
				width = Math.ceil(Math.min(widthDouble, (screenWidth * maxSizeX).toDouble())).toFloat()
			}

			val preferredHeightCeil = Math.ceil(preferredHeight)
			var height = if (sizeY != null) {
				screenHeight * sizeY
			} else {
				preferredHeightCeil.toFloat()
			}

			val heightDouble = height.toDouble()
			if (maxSizeY != null) {
				height = Math.ceil(Math.min(heightDouble, (screenHeight * maxSizeY).toDouble())).toFloat()
			}

			val originalRatio = preferredWidthCeil / preferredHeightCeil
			if ((widthDouble / heightDouble) != originalRatio) {
				println("Outside ratio!")

				if (Math.max(widthDouble, preferredWidthCeil) / Math.min(widthDouble, preferredWidthCeil) == originalRatio) {
					val rx = preferredWidth / width
					height = preferredHeight.toFloat() * rx.toFloat()
				} else if (Math.max(heightDouble, preferredHeightCeil) / Math.min(heightDouble, preferredHeightCeil) == originalRatio) {
					val ry = preferredHeight / height
					width = preferredWidth.toFloat() * ry.toFloat()
				} else {
					width = preferredWidth.toFloat()
					height = preferredHeight.toFloat()
				}

			}

			val imgName = if (width == preferredWidth.toFloat() && height == preferredHeight.toFloat()) {
				"${imgFile.nameWithoutExtension}.png"
			} else {
				"${imgFile.nameWithoutExtension}_${width}x$height.png"
			}
			
			val pngFile = File(imgFile.parentFile, imgName)
			if (!pngFile.exists()) {
				logger.debug { "convertImage: Converting ${imgFile.name} image into ${pngFile.name}" }
				pngConverter.convertFromSVG(imgFile, pngFile, width, height)
				viewImage.path = pngFile
			} else {
				viewImage.path = pngFile
			}

			pngFile.deleteOnExit()
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

