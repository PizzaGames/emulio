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
    
		val factory = DocumentBuilderFactory.newInstance()
		val docBuilder = factory.newDocumentBuilder()
		val document = docBuilder.parse(xmlFile)
		
		val theme = Theme()
		theme.formatVersion = document.getElementsByTagName("formatVersion")?.item(0)?.nodeValue
		theme.includeTheme = findIncludeTheme(document, xmlFile)
		theme.views = readViews(document.getElementsByTagName("view"), xmlFile)
		
        return theme
    }
	
	private fun readViews(viewNodes: NodeList?, xmlFile: File): List<View> {
		if (viewNodes == null || viewNodes.length == 0) {
			return emptyList()
		}
		
		val views = mutableListOf<View>()
		
		for (i in 0..viewNodes.length) {
			val viewNode = viewNodes.item(i)
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
			val node = itemNodes.item(i)
			
			val viewItem = when(node.localName) {
				"image" -> { readImage(node, xmlFile) }
				"rating" -> { readRating(node, xmlFile) }
				"datetime" -> { readDatetime(node, xmlFile) }
				"helpsystem" -> { readHelpSystem(node, xmlFile) }
				"textlist" -> { readTextList(node, xmlFile) }
				"text" -> { readText(node, xmlFile) }
				else -> { readViewItem(node, xmlFile) }
			}
			
			items.add(viewItem)
			
		}
		
		return items
	}
	
	private fun readText(node: Node, xmlFile: File): Text {3
		val text = Text()
		if (node.hasChildNodes()) {
			val childNodes = node.childNodes
			for (i in 0..childNodes.length) {
				val child = childNodes.item(i)
				when (child.localName) {
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
				val child = childNodes.item(i)
				when (child.localName) {
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
				val child = childNodes.item(i)
				when (child.localName) {
					"filledPath" -> { rating.filledPath = File(xmlFile.parentFile, child.nodeValue) }
					"unfilledPath" -> { rating.unfilledPath = File(xmlFile.parentFile, child.nodeValue) }
				}
			}
			
		}
		return rating.readViewItem(node)
	}
	
	private fun readImage(node: Node, xmlFile: File): Image {
		val image = Image()
		if (node.hasChildNodes()) {
			val childNodes = node.childNodes
			for (i in 0..childNodes.length) {
				val child = childNodes.item(i)
				when (child.localName) {
					"path" -> { image.path = File(xmlFile.parentFile, child.nodeValue) }
				}
			}
			
		}
		return image.readViewItem(node)
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
				val child = childNodes.item(i)
				readViewItemChild(child)
			}
			
		}
		return this
	}
	
	private fun ViewItem.readViewItemChild(child: Node) {
		when (child.localName) {
			"position" -> {
				val position = child.nodeValue.split(" ")
				
				positionX = position[0].toFloatOrNull()
				positionY = position[1].toFloatOrNull()
			}
			
			"maxsize" -> {
				val maxSize = child.nodeValue.split(" ")
				
				maxSizeX = maxSize[0].toFloatOrNull()
				maxSizeY = maxSize[1].toFloatOrNull()
			}
			
			"size" -> {
				val size = child.nodeValue.split(" ")
				
				sizeX = size[0].toFloatOrNull()
				sizeY = size[1].toFloatOrNull()
			}
			
			"origin" -> {
				val origin = child.nodeValue.split(" ")
				
				originX = origin[0].toFloatOrNull()
				originY = origin[1].toFloatOrNull()
			}
			
			"horizontalMargin" -> {
				horizontalMargin = child.nodeValue.toFloatOrNull()
			}
			"verticalMargin" -> {
				verticalMargin = child.nodeValue.toFloatOrNull()
			}
			
			"textColor" -> {
				textColor = child.nodeValue
			}
			"iconColor" -> {
				iconColor = child.nodeValue
			}
		}
	}
	
	private fun findIncludeTheme(document: Document, mainXmlFile: File): Theme? {
		val includeTag = document.getElementsByTagName("include")
		if (includeTag == null || includeTag.length == 0) {
			return null
		}
		
		val includePath = includeTag.item(0).firstChild.nodeValue
		
		val xmlFile = File(mainXmlFile.parentFile, includePath)
		
		if (!xmlFile.isFile) {
			return null
		}
		return parseTheme(xmlFile)
	}
	
}
