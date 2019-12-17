package com.github.emulio.runners

import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import java.io.File
import java.io.FileOutputStream


class PNGConverter {
	
	val pngTranscoder = PNGTranscoder()

	fun convertFromSVG(svgFile: File, pngFile: File, width: Float?, height: Float?) {
		if (width != null) {
			pngTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width)
		}
		if (height != null) {
			pngTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height)
		}

		val input = TranscoderInput(svgFile.toURI().toString())

		FileOutputStream(pngFile).use { outputStream ->
			val output = TranscoderOutput(outputStream)
			pngTranscoder.transcode(input, output)
			outputStream.flush()
		}
	}


}


//
//fun main(args: Array<String>) {
//	val start = System.currentTimeMillis()
//
//    Files.walk(File("G:\\workspace\\emulio\\core\\assets\\images\\resources").toPath()).filter({ path ->
//        path.toFile().extension.equals("svg")
//    }).forEach { path ->
//
//
//        val svgFile = path.toFile()
//        val pngFile = File("G:\\workspace\\emulio-skin\\sources", svgFile.nameWithoutExtension + "_40_40.png")
//        println("Converting: $svgFile to $pngFile")
//
//        PNGConverter().convertFromSVG(svgFile, pngFile, 40f, 40f)
//    }
//
//	//PNGConverter().convertFromSVG(File("G:/workspace/emulio/sample-files/theme/simple/3do/art/3do.svg"), File("G:/workspace/emulio/sample-files/theme/simple/3do/art/3do.png"), 128f, 128f)
//
//	println("${System.currentTimeMillis() - start}ms")
//}
