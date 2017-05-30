package com.github.emulio.xml

import com.github.emulio.model.Game
import com.github.emulio.model.Platform
import com.github.emulio.model.theme.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
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
		
		val theme = Theme()
		theme.formatVersion = document.getElementsByTagName("formatVersion")?.item(0)?.textContent
		theme.views = readViews(document.getElementsByTagName("view"), xmlFile)

		theme.includeTheme = findIncludeTheme(document, xmlFile)



        return theme
    }
	
	private fun readViews(viewNodes: NodeList?, xmlFile: File): List<View> {
		if (viewNodes == null || viewNodes.length == 0) {
			return emptyList()
		}
		
		val views = mutableListOf<View>()
		
		for (i in 0..viewNodes.length) {
			val viewNode = viewNodes.item(i) ?: continue
			check(viewNode.nodeName == "view")

			val view = View()
			
			view.name = viewNode.attributes.getNamedItem("name").nodeValue
			view.viewItems = readViewItems(viewNode.childNodes, xmlFile)
			
			views.add(view)
		}
		
		return views
	}
	
	private fun readViewItems(itemNodes: NodeList?, xmlFile: File): List<ViewItem> {
		if (itemNodes == null || itemNodes.length == 0) {
			return emptyList()
		}
		
		val items = mutableListOf<ViewItem>()
		
		for (i in 0..itemNodes.length) {
			val node = itemNodes.item(i) ?: continue

			if (node.nodeName == "#text" ||
					node.nodeName == "#comment") {
				continue
			}
			
			val viewItem = when(node.nodeName) {
				"image" -> { readImage(node, xmlFile) }
				"ninepatch" -> { readNinepatch(node, xmlFile) }
				"container" -> { readContainer(node, xmlFile) }
				"rating" -> { readRating(node, xmlFile) }
				"datetime" -> { readDatetime(node, xmlFile) }
				"helpsystem" -> { readHelpSystem(node, xmlFile) }
				"textlist" -> { readTextList(node, xmlFile) }
				"text" -> { readText(node, xmlFile) }
				"view" -> { readViewItem(node, xmlFile) }
				
				else -> {
					error("Tag not supported yet '${node.nodeName}' ")
				}
			}
			
			items.add(viewItem)
			
		}
		
		return items
	}
	
	private fun readContainer(node: Node, xmlFile: File): Container {
		return Container().readViewItem(node)
	}
	
	private fun readNinepatch(node: Node, xmlFile: File): ViewItem {
		return NinePatch().readImage(node, xmlFile)
	}
	
	private fun readText(node: Node, xmlFile: File): Text {3
		val text = Text()
		if (node.hasChildNodes()) {
			val childNodes = node.childNodes
			for (i in 0..childNodes.length) {
				val child = childNodes.item(i) ?: continue
				when (child.nodeName) {
					"date" -> { text.text = node.nodeValue }
					"color" -> { text.color = node.nodeValue }
					"alignment" -> {
						text.alignment = when(node.nodeValue) {
							"center" -> TextAlignment.CENTER
							"left" -> TextAlignment.LEFT
							"justify" -> TextAlignment.JUSTIFY
							"right" -> TextAlignment.RIGHT
							else -> TextAlignment.LEFT
						}
					}
					"fontpath" -> { text.fontPath = File(xmlFile.parentFile, child.nodeValue) }
					"fontsize" -> { text.fontSize = node.nodeValue.toInt() }
				}
			}
			
		}
		return text.readViewItem(node)
	}
	
	private fun readTextList(node: Node, xmlFile: File): TextList {
		return TextList().readViewItem(node)
	}
	
	private fun readHelpSystem(node: Node, xmlFile: File): HelpSystem {
		return HelpSystem().readViewItem(node)
	}
	
	private fun readDatetime(node: Node, xmlFile: File): DateTime {
		val dateTime = DateTime()
		if (node.hasChildNodes()) {
			val childNodes = node.childNodes
			for (i in 0..childNodes.length) {
				val child = childNodes.item(i) ?: continue
				when (child.nodeName) {
					"date" -> { dateTime.date = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(node.nodeValue) }
					"color" -> { dateTime.color = node.nodeValue }
					"fontpath" -> { dateTime.fontPath = File(xmlFile.parentFile, child.nodeValue) }
					"fontsize" -> { dateTime.fontSize = node.nodeValue.toInt() }
				}
			}
			
		}
		return dateTime.readViewItem(node)
	}
	
	private fun  readRating(node: Node, xmlFile: File): Rating {
		val rating = Rating()
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
	
	private fun readImage(node: Node, xmlFile: File): ViewImage {
		return ViewImage().readImage(node, xmlFile)
	}
	
	private fun readViewItem(node: Node, xmlFile: File): ViewItem {
		return ViewItem().readViewItem(node)
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
			"position" -> {
				val position = child.textContent.split(" ")
				
				positionX = position[0].toFloatOrNull()
				positionY = position[1].toFloatOrNull()
			}
			
			"maxsize" -> {
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
	
	private fun findIncludeTheme(document: Document, mainXmlFile: File): Theme? {
		val includeTag = document.getElementsByTagName("include")
		if (includeTag == null || includeTag.length == 0) {
			return null
		}
		
		val includePath = includeTag.item(0).firstChild.textContent
		
		val xmlFile = File(mainXmlFile.parentFile, includePath)
		
		if (!xmlFile.isFile) {
			return null
		}
		return parseTheme(xmlFile)
	}
	
}
