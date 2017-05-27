package com.github.emulio.runners

import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import java.io.File
import java.io.FileOutputStream


class PNGConverter {

	fun convertFromSVG(svgFile: File, pngFile: File, width: Float, height: Float) {
		val t = PNGTranscoder()

		t.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width)
		t.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height)

		val input = TranscoderInput(svgFile.toURI().toString())

		FileOutputStream(pngFile).use { outputStream ->
			val output = TranscoderOutput(outputStream)
			t.transcode(input, output)
			outputStream.flush()
		}
	}


}


//fun main(args: Array<String>) {
//	val start = System.currentTimeMillis()
//	PNGConverter().convertFromSVG(File("G:/workspace/emulio/sample-files/theme/simple/3do/art/3do.svg"), File("G:/workspace/emulio/sample-files/theme/simple/3do/art/3do.png"), 200f, 200f)
//
//	println("${System.currentTimeMillis() - start}ms")
//}