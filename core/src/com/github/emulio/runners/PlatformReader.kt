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

            val templateFile = File(platformsFile.parentFile, "${platformsFile.nameWithoutExtension}-template.yaml")
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


            error("${platformsFile.name} not found in workdir, a template file is now copied so you can change it.")
        }
        return YamlUtils().parsePlatforms(platformsFile)
    }
}

