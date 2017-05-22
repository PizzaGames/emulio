package com.github.emulio.xml

import com.github.emulio.model.Game
import mu.KotlinLogging
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.util.*

class ThemeSAXHandler() : DefaultHandler() {

	val logger = KotlinLogging.logger { }

	enum class Tag(val value: String) {
		ALIGNMENT("alignment"),
		COLOR("color"),
		DATETIME("datetime"),
		FILLEDPATH("filledPath"),
		FONTPATH("fontPath"),
		FONTSIZE("fontSize"),
		FORCEUPPERCASE("forceUppercase"),
		FORMATVERSION("formatVersion"),
		HELPSYSTEM("helpsystem"),
		HORIZONTALMARGIN("horizontalMargin"),
		VERTICALMARGIN("verticalMargin"),
		ICONCOLOR("iconColor"),
		IMAGE("image"),
		INCLUDE("include"),
		MAXSIZE("maxSize"),
		ORIGIN("origin"),
		PATH("path"),
		POS("pos"),
		RATING("rating"),
		SIZE("size"),
		TEXT("text"),
		TEXTCOLOR("textColor"),
		TEXTLIST("textlist"),
		THEME("theme"),
		UNFILLEDPATH("unfilledPath"),
		VIEW("view"),
		NO_STATE("")
	}

	var tag: Tag = Tag.NO_STATE

	private var name: String? = null
	private var extra: Boolean? = null

	override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes) {

		if (qName.equals(Tag.ALIGNMENT.value, true)) {
			tag = Tag.ALIGNMENT
		} else if (qName.equals(Tag.COLOR.value, true)) {
			tag = Tag.COLOR
		} else if (qName.equals(Tag.DATETIME.value, true)) {
			tag = Tag.DATETIME
		} else if (qName.equals(Tag.FILLEDPATH.value, true)) {
			tag = Tag.FILLEDPATH
		} else if (qName.equals(Tag.FONTPATH.value, true)) {
			tag = Tag.FONTPATH
		} else if (qName.equals(Tag.FONTSIZE.value, true)) {
			tag = Tag.FONTSIZE
		} else if (qName.equals(Tag.FORCEUPPERCASE.value, true)) {
			tag = Tag.FORCEUPPERCASE
		} else if (qName.equals(Tag.FORMATVERSION.value, true)) {
			tag = Tag.FORMATVERSION
		} else if (qName.equals(Tag.HELPSYSTEM.value, true)) {
			tag = Tag.HELPSYSTEM
		} else if (qName.equals(Tag.HORIZONTALMARGIN.value, true)) {
			tag = Tag.HORIZONTALMARGIN
		} else if (qName.equals(Tag.VERTICALMARGIN.value, true)) {
			tag = Tag.VERTICALMARGIN
		} else if (qName.equals(Tag.ICONCOLOR.value, true)) {
			tag = Tag.ICONCOLOR
		} else if (qName.equals(Tag.IMAGE.value, true)) {
			tag = Tag.IMAGE
		} else if (qName.equals(Tag.INCLUDE.value, true)) {
			tag = Tag.INCLUDE
		} else if (qName.equals(Tag.MAXSIZE.value, true)) {
			tag = Tag.MAXSIZE
		} else if (qName.equals(Tag.ORIGIN.value, true)) {
			tag = Tag.ORIGIN
		} else if (qName.equals(Tag.PATH.value, true)) {
			tag = Tag.PATH
		} else if (qName.equals(Tag.POS.value, true)) {
			tag = Tag.POS
		} else if (qName.equals(Tag.RATING.value, true)) {
			tag = Tag.RATING
		} else if (qName.equals(Tag.SIZE.value, true)) {
			tag = Tag.SIZE
		} else if (qName.equals(Tag.TEXT.value, true)) {
			tag = Tag.TEXT
		} else if (qName.equals(Tag.TEXTCOLOR.value, true)) {
			tag = Tag.TEXTCOLOR
		} else if (qName.equals(Tag.TEXTLIST.value, true)) {
			tag = Tag.TEXTLIST
		} else if (qName.equals(Tag.THEME.value, true)) {
			tag = Tag.THEME
		} else if (qName.equals(Tag.UNFILLEDPATH.value, true)) {
			tag = Tag.UNFILLEDPATH
		} else if (qName.equals(Tag.VIEW.value, true)) {
			tag = Tag.VIEW
		}

		for (i in 0..attributes.length) {
			if (attributes.getQName(i).equals("name", true)) {
				name = attributes.getValue(i)
			} else if (attributes.getQName(i).equals("extra", true)) {
				extra = attributes.getValue(i).toBoolean()
			}
		}
	}

	var alignment: String? = null
	var color: String? = null
	var datetime: String? = null
	var filledPath: String? = null
	var fontPath: String? = null
	var fontSize: String? = null
	var forceUppercase: String? = null
	var formatVersion: String? = null
	var helpsystem: String? = null
	var horizontalMargin: String? = null
	var verticalMargin: String? = null
	var iconColor: String? = null
	var image: String? = null
	var include: String? = null
	var maxSize: String? = null
	var origin: String? = null
	var path: String? = null
	var pos: String? = null
	var rating: String? = null
	var size: String? = null
	var text: String? = null
	var textColor: String? = null
	var textlist: String? = null
	var theme: String? = null
	var unfilledPath: String? = null
	var view: String? = null

	override fun endElement(uri: String?, localName: String?, qName: String?) {
		if (qName.equals(Tag.ALIGNMENT.value, true)) {

		} else if (qName.equals(Tag.COLOR.value, true)) {

		} else if (qName.equals(Tag.DATETIME.value, true)) {

		} else if (qName.equals(Tag.FILLEDPATH.value, true)) {

		} else if (qName.equals(Tag.FONTPATH.value, true)) {

		} else if (qName.equals(Tag.FONTSIZE.value, true)) {

		} else if (qName.equals(Tag.FORCEUPPERCASE.value, true)) {

		} else if (qName.equals(Tag.FORMATVERSION.value, true)) {

		} else if (qName.equals(Tag.HELPSYSTEM.value, true)) {

		} else if (qName.equals(Tag.HORIZONTALMARGIN.value, true)) {

		} else if (qName.equals(Tag.VERTICALMARGIN.value, true)) {

		} else if (qName.equals(Tag.ICONCOLOR.value, true)) {

		} else if (qName.equals(Tag.IMAGE.value, true)) {

		} else if (qName.equals(Tag.INCLUDE.value, true)) {

		} else if (qName.equals(Tag.MAXSIZE.value, true)) {

		} else if (qName.equals(Tag.ORIGIN.value, true)) {

		} else if (qName.equals(Tag.PATH.value, true)) {

		} else if (qName.equals(Tag.POS.value, true)) {

		} else if (qName.equals(Tag.RATING.value, true)) {

		} else if (qName.equals(Tag.SIZE.value, true)) {

		} else if (qName.equals(Tag.TEXT.value, true)) {

		} else if (qName.equals(Tag.TEXTCOLOR.value, true)) {

		} else if (qName.equals(Tag.TEXTLIST.value, true)) {

		} else if (qName.equals(Tag.THEME.value, true)) {
			//theme = Theme()
		} else if (qName.equals(Tag.UNFILLEDPATH.value, true)) {

		} else if (qName.equals(Tag.VIEW.value, true)) {

		}

		tag = Tag.NO_STATE
	}

	fun getFile(baseDir: File, path: String): File {
		val pathFixed = if (path.startsWith("./")) {
			path.replaceFirst("./", "")
		} else {
			path
		}

		return File(baseDir, pathFixed)
	}


	override fun characters(ch: CharArray, start: Int, length: Int) {
		when (tag) {
			Tag.ALIGNMENT -> { alignment = String(ch, start, length) }
			Tag.COLOR -> { color = String(ch, start, length) }
			Tag.DATETIME -> { datetime = String(ch, start, length) }
			Tag.FILLEDPATH -> { filledPath = String(ch, start, length) }
			Tag.FONTPATH -> { fontPath = String(ch, start, length) }
			Tag.FONTSIZE -> { fontSize = String(ch, start, length) }
			Tag.FORCEUPPERCASE -> { forceUppercase = String(ch, start, length) }
			Tag.FORMATVERSION -> { formatVersion = String(ch, start, length) }
			Tag.HELPSYSTEM -> { helpsystem = String(ch, start, length) }
			Tag.HORIZONTALMARGIN -> { horizontalMargin = String(ch, start, length) }
			Tag.VERTICALMARGIN -> { verticalMargin = String(ch, start, length) }
			Tag.ICONCOLOR -> { iconColor = String(ch, start, length) }
			Tag.IMAGE -> { image = String(ch, start, length) }
			Tag.INCLUDE -> { include = String(ch, start, length) }
			Tag.MAXSIZE -> { maxSize = String(ch, start, length) }
			Tag.ORIGIN -> { origin = String(ch, start, length) }
			Tag.PATH -> { path = String(ch, start, length) }
			Tag.POS -> { pos = String(ch, start, length) }
			Tag.RATING -> { rating = String(ch, start, length) }
			Tag.SIZE -> { size = String(ch, start, length) }
			Tag.TEXT -> { text = String(ch, start, length) }
			Tag.TEXTCOLOR -> { textColor = String(ch, start, length) }
			Tag.TEXTLIST -> { textlist = String(ch, start, length) }
			Tag.THEME -> { theme = String(ch, start, length) }
			Tag.UNFILLEDPATH -> { unfilledPath = String(ch, start, length) }
			Tag.VIEW -> { view = String(ch, start, length) }
		}
	}

	private fun convertDate(ch: CharArray, start: Int, length: Int): Date {
		val calendar = GregorianCalendar.getInstance()

		var currentOffset = start
		calendar.set(Calendar.YEAR, Integer.parseInt(String(ch, currentOffset, 4)))
		currentOffset += 4

		calendar.set(Calendar.MONTH, Integer.parseInt(String(ch, currentOffset, 2)) - 1) //months starts with 0
		currentOffset += 2

		calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(String(ch, currentOffset, 2)))
		currentOffset += 2
		currentOffset += 1 // T

		calendar.set(Calendar.HOUR, Integer.parseInt(String(ch, currentOffset, 2)))
		currentOffset += 2

		calendar.set(Calendar.MINUTE, Integer.parseInt(String(ch, currentOffset, 2)))
		currentOffset += 2

		calendar.set(Calendar.SECOND, Integer.parseInt(String(ch, currentOffset, 2)))
		currentOffset += 2

		return calendar.time

	}
	

}