package com.github.emulio.xml

import com.github.emulio.model.Game
import com.github.emulio.model.Platform
import com.github.emulio.model.theme.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.text.SimpleDateFormat
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.SAXParserFactory


class XMLReader {
    fun parseGameList(xmlFile: File, baseDir: File, pathSet: MutableSet<String>, platform: Platform): Flowable<Game> {
        
        return Flowable.create({ emitter ->
            val factory = SAXParserFactory.newInstance()
            val saxParser = factory.newSAXParser()
			
            saxParser.parse(xmlFile, GameInfoSAXHandler(emitter, baseDir, pathSet, platform))
			
        }, BackpressureStrategy.BUFFER)
    }
    
    fun parseTheme(xmlFile: File): Theme {

		check(xmlFile.exists()) { "File ${xmlFile.absolutePath} does not exists" }

		check(xmlFile.isFile)
    
		val factory = DocumentBuilderFactory.newInstance()
		val docBuilder = factory.newDocumentBuilder()
		val document = docBuilder.parse(xmlFile)
		
		val theme = findIncludeTheme(document, xmlFile)

		theme.formatVersion = document.getElementsByTagName("formatVersion")?.item(0)?.textContent
		theme.views = readViews(document.getElementsByTagName("view"), xmlFile, theme)

        return theme
    }
	
	private fun readViews(viewNodes: NodeList?, xmlFile: File, theme: Theme): List<View> {
		if (viewNodes == null || viewNodes.length == 0) {
			return emptyList()
		}
		
		val viewMap = mutableMapOf<String, View>()
		
		for (i in 0..viewNodes.length) {
			val viewNode = viewNodes.item(i) ?: continue
			check(viewNode.nodeName == "view")

			val viewName = viewNode.attributes.getNamedItem("name").nodeValue
			
			if (viewName.contains(",")) {
				viewName.split(",").forEach { name ->
					val viewName = name.trim()
					
					val view = theme.getViewByName(viewName) ?: View()
					view.name = viewName
					view.viewItems = readViewItems(viewNode.childNodes, xmlFile, view)
					
					if (viewMap[viewName] != null) {
						val viewToMerge = viewMap[viewName]!!
						view.viewItems!!.addAll(viewToMerge.viewItems!!)
					}
					viewMap[viewName] = view
				}
			} else {
				val view = theme.getViewByName(viewName) ?: View()
				view.name = viewName
				view.viewItems = readViewItems(viewNode.childNodes, xmlFile, view)
				
				if (viewMap[viewName] != null) {
					val viewToMerge = viewMap[viewName]!!
					view.viewItems!!.addAll(viewToMerge.viewItems!!)
				}
				viewMap[viewName] = view
			}
			
		}
		
		return viewMap.values.toList()
	}
	
	private fun readViewItems(itemNodes: NodeList?, xmlFile: File, view: View): MutableList<ViewItem> {
		if (itemNodes == null || itemNodes.length == 0) {
			return ArrayList()
		}
		
		val itemsByName = mutableMapOf<String, ViewItem>()
		
		if (view.viewItems != null) {
			view.viewItems!!.forEach { item ->
				itemsByName[item.name!!] = item
			}
		}

		for (i in 0..itemNodes.length) {
			val node = itemNodes.item(i) ?: continue

			val attributes = node.attributes
			
			if (node.nodeName == "#text" ||
					node.nodeName == "#comment") {
				continue
			}
			
			val viewItemName = attributes?.getNamedItem("name")?.nodeValue!!
			
			if (viewItemName.contains(",")) {
				viewItemName.split(",").forEach { splittedName ->
					val viewItemName = splittedName.trim()
					
					val foundView = itemsByName[viewItemName]
					
					itemsByName[viewItemName] = readViewItem(viewItemName, foundView, node, xmlFile).apply {
						name = viewItemName
					}
				}
			} else {
				
				val foundView = itemsByName[viewItemName]
				itemsByName[viewItemName] = readViewItem(viewItemName, foundView, node, xmlFile).apply {
					name = viewItemName
				}
			}
		}
		
		return itemsByName.values.toMutableList()
	}
	
	private fun readViewItem(name: String, foundView: ViewItem?, node: Node, xmlFile: File): ViewItem {
		return when (node.nodeName) {
			"image" -> {
				readImage(node, xmlFile, foundView, name)
			}
			"ninepatch" -> {
				readNinepatch(node, xmlFile, foundView, name)
			}
			"container" -> {
				readContainer(node, xmlFile, foundView, name)
			}
			"rating" -> {
				readRating(node, xmlFile, foundView as Text?, name)
			}
			"datetime" -> {
				readDatetime(node, xmlFile, foundView as Text?, name)
			}
			"helpsystem" -> {
				readHelpSystem(node, xmlFile, foundView, name)
			}
			"textlist" -> {
				readTextList(node, xmlFile, foundView as Text?, name)
			}
			"text" -> {
				readText(node, xmlFile, foundView, name)
			}
			"view" -> {
				readViewItem(node, xmlFile, foundView, name)
			}
			
			else -> {
				error("Tag not supported yet '${node.nodeName}' ")
			}
		}
	}
	
	private fun readContainer(node: Node, xmlFile: File, foundView: ViewItem?, name: String): Container {
		val container = if (foundView != null) { foundView as Container } else { Container() }
		return container.readViewItem(node)
	}
	
	private fun readNinepatch(node: Node, xmlFile: File, foundView: ViewItem?, name: String): NinePatch {
		val ninePatch = if (foundView != null) { foundView as NinePatch } else { NinePatch() }
		return ninePatch.readImage(node, xmlFile)
	}
	
	private fun readText(node: Node, xmlFile: File, foundView: ViewItem?, name: String): Text {
		val text = if (foundView != null) { foundView as Text } else { Text() }
		if (node.hasChildNodes()) {
			val childNodes = node.childNodes
			for (i in 0..childNodes.length) {
				val child = childNodes.item(i) ?: continue
				when (child.nodeName) {
					"text" -> {
						text.text = child.textContent
					}
					"color" -> { text.color = child.textContent }
					"alignment" -> {
						text.alignment = when(child.textContent) {
							"center" -> TextAlignment.CENTER
							"left" -> TextAlignment.LEFT
							"justify" -> TextAlignment.JUSTIFY
							"right" -> TextAlignment.RIGHT
							else -> TextAlignment.LEFT
						}
					}
					"fontPath" -> {
						text.fontPath = File(xmlFile.parentFile, child.textContent)
					}
					"fontSize" -> { text.fontSize = child.textContent.toFloat() }
				}
			}
			
		}
		return text.readViewItem(node)
	}

	private fun readTextList(node: Node, xmlFile: File, foundView: Text?, name: String): TextList {
		val textList = if (foundView != null) { TextList(foundView) } else { TextList() }
		
		if (node.hasChildNodes()) {
			val childNodes = node.childNodes
			for (i in 0..childNodes.length) {
				val child = childNodes.item(i) ?: continue
				when (child.nodeName) {
					"selectorColor" -> { child.textContent }
					"selectedColor" -> { child.textContent }
					"primaryColor" -> { child.textContent }
					"secondaryColor" -> { child.textContent }
				}
			}
		}
		return textList.readViewItem(node)
	}
	
	private fun readHelpSystem(node: Node, xmlFile: File, foundView: ViewItem?, name: String): HelpSystem {
		val helpSystem = if (foundView != null) { foundView as HelpSystem } else { HelpSystem() }
		return helpSystem.readViewItem(node)
	}
	
	private fun readDatetime(node: Node, xmlFile: File, foundView: Text?, name: String): DateTime {
		val dateTime = if (foundView != null) {
			if (foundView is DateTime) {
				DateTime(foundView)
			} else {
				DateTime(foundView)
			}
		} else { DateTime() }
		if (node.hasChildNodes()) {
			val childNodes = node.childNodes
			for (i in 0..childNodes.length) {
				val child = childNodes.item(i) ?: continue
				when (child.nodeName) {
					"date" -> { dateTime.date = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(child.textContent) }
					"color" -> { dateTime.color = child.textContent }
					"fontpath" -> { dateTime.fontPath = File(xmlFile.parentFile, child.textContent) }
					"fontsize" -> { dateTime.fontSize = child.textContent?.toFloat() }
				}
			}
			
		}
		return dateTime.readViewItem(node)
	}
	
	private fun  readRating(node: Node, xmlFile: File, foundView: Text?, name: String): Rating {
		val rating = if (foundView != null) {
			if (foundView is Rating) {
				Rating(foundView)
			} else {
				Rating(foundView)
			}
		} else { Rating() }
		if (node.hasChildNodes()) {
			val childNodes = node.childNodes
			for (i in 0..childNodes.length) {
				val child = childNodes.item(i) ?: continue
				when (child.nodeName) {
					"filledPath" -> { rating.filledPath = File(xmlFile.parentFile, child.textContent) }
					"unfilledPath" -> { rating.unfilledPath = File(xmlFile.parentFile, child.textContent) }
				}
			}
			
		}
		return rating.readViewItem(node)
	}
	
	private fun <T : ViewImage> T.readImage(node: Node, xmlFile: File): T {
		if (node.hasChildNodes()) {
			val childNodes = node.childNodes
			for (i in 0..childNodes.length) {
				val child = childNodes.item(i) ?: continue
				
				when (child.nodeName) {
					"path" -> { path = File(xmlFile.parentFile, child.textContent) }
				}
			}
			
		}
		return readViewItem(node)
	}
	
	private fun readImage(node: Node, xmlFile: File, foundView: ViewItem?, name: String): ViewImage {
		val viewImage = if (foundView != null) { foundView as ViewImage } else { ViewImage() }
		return viewImage.readImage(node, xmlFile)
	}
	
	private fun readViewItem(node: Node, xmlFile: File, foundView: ViewItem?, name: String): ViewItem {
		val viewItem = foundView ?: ViewItem()
		return viewItem.readViewItem(node)
	}
	
	private fun <T : ViewItem> T.readViewItem(node: Node): T {
		if (node.hasAttributes()) {
			val attributes = node.attributes
			name = attributes.getNamedItem("name")?.nodeValue
			extra = attributes.getNamedItem("extra")?.nodeValue?.toBoolean()
		}
		
		if (node.hasChildNodes()) {
			val childNodes = node.childNodes
			for (i in 0..childNodes.length) {
				val child = childNodes.item(i) ?: continue
				readViewItemChild(child)
			}
			
		}
		return this
	}
	
	private fun ViewItem.readViewItemChild(child: Node) {
		when (child.nodeName) {

			"pos" -> {
				val position = child.textContent.split(" ")

				positionX = position[0].toFloatOrNull()
				positionY = position[1].toFloatOrNull()
			}

			"position" -> {
				val position = child.textContent.split(" ")
				
				positionX = position[0].toFloatOrNull()
				positionY = position[1].toFloatOrNull()
			}
			
			"maxSize" -> {
				val maxSize = child.textContent.split(" ")
				
				maxSizeX = maxSize[0].toFloatOrNull()
				maxSizeY = maxSize[1].toFloatOrNull()
			}
			
			"size" -> {
				val size = child.textContent.split(" ")
				
				sizeX = size[0].toFloatOrNull()
				sizeY = size[1].toFloatOrNull()
			}
			
			"origin" -> {
				val origin = child.textContent.split(" ")
				
				originX = origin[0].toFloatOrNull()
				originY = origin[1].toFloatOrNull()
			}
			
			"horizontalMargin" -> {
				horizontalMargin = child.textContent.toFloatOrNull()
			}
			"verticalMargin" -> {
				verticalMargin = child.textContent.toFloatOrNull()
			}
			
			"textColor" -> {
				textColor = child.textContent
			}
			"iconColor" -> {
				iconColor = child.textContent
			}
		}
	}
	
	private fun findIncludeTheme(document: Document, mainXmlFile: File): Theme {
		val includeTag = document.getElementsByTagName("include")
		if (includeTag == null || includeTag.length == 0) {
			return Theme()
		}
		
		val includePath = includeTag.item(0).firstChild.textContent
		
		val xmlFile = File(mainXmlFile.parentFile, includePath)
		
		if (!xmlFile.isFile) {
			return Theme()
		}
		return parseTheme(xmlFile)
	}
	
}
