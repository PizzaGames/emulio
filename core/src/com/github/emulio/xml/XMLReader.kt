package com.github.emulio.xml

import com.github.emulio.model.Game
import com.github.emulio.model.Platform
import io.reactivex.Observable
import java.io.File
import javax.xml.parsers.SAXParserFactory


class XMLReader {
    fun parseGameList(xmlFile: File, baseDir: File, platform: Platform): Observable<Game> {
        
        return Observable.create({ emitter ->
            val factory = SAXParserFactory.newInstance()
            val saxParser = factory.newSAXParser()
            
    
            saxParser.parse(xmlFile, GameInfoSAXHandler(emitter, baseDir, platform))
        })
    }
}
