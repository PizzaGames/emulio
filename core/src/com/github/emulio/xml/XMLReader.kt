package com.github.emulio.xml

import com.github.emulio.model.Game
import com.github.emulio.model.Platform
import com.github.emulio.model.theme.Theme
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import java.io.File
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
		
        
        
        return Theme()
    }
    
}
