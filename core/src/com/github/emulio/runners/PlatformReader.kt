package com.github.emulio.runners


import com.github.emulio.Emulio
import com.github.emulio.model.Platform
import com.github.emulio.yaml.YamlUtils
import java.io.*


class PlatformReader(val emulio: Emulio) : Function0<List<Platform>> {
	override fun invoke(): List<Platform> {
        val platformsFile = File(emulio.workdir, "emulio-platforms.yaml")

        if (!platformsFile.exists()) {
            val template = Emulio::class.java.getResourceAsStream("/emulio-platforms-template.yaml")

            val templateName = "${platformsFile.nameWithoutExtension}-template.yaml"
            val templateFile = File(platformsFile.parentFile, templateName)
            try {

                BufferedWriter(FileWriter(templateFile)).use { writer ->
                    InputStreamReader(template).forEachLine { line ->
                        writer.write(line)
                        writer.newLine()
                    }
                    writer.flush()
                }
            } catch (e: IOException) {
                error("Error writing ${templateFile.absolutePath}. Check your permissions in this folder.")
            }


            error("${platformsFile.canonicalPath} not found, a template ('${templateFile.name}') \n " +
                    "file was created so you can change and rename it.")
        }
        return YamlUtils().parsePlatforms(platformsFile)
    }
}

