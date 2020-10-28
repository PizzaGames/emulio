package com.github.emulio.runners

import com.github.emulio.Emulio
import com.github.emulio.exceptions.ConfigNotFoundException
import com.github.emulio.model.Platform
import com.github.emulio.yaml.YamlUtils
import mu.KotlinLogging
import java.io.*

class PlatformReader(val emulio: Emulio) : Function0<List<Platform>> {

    val logger = KotlinLogging.logger { }

	override fun invoke(): List<Platform> {
        val platformsFile = emulio.options.platformsFile
        logger.info { "Reading platforms file: ${platformsFile.absolutePath}" }

        if (!platformsFile.exists()) {
            logger.info { "Platforms file not found, initializing PlatformWizard" }
            createTemplateFileWizard(platformsFile)
            return emptyList()
        }

        return YamlUtils.parsePlatforms(platformsFile)
    }

    private fun createTemplateFileWizard(platformsFile: File) {
        val template = Emulio::class.java.getResourceAsStream("/initialsetup/emulio-platforms-template.yaml")

        val templateName = "${platformsFile.nameWithoutExtension}-template.yaml"
        val templateFile = File(platformsFile.parentFile, templateName)
        logger.debug { "Creating template file: ${templateFile.absolutePath}" }

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

        throw ConfigNotFoundException("${platformsFile.canonicalPath} not found, a template ('${templateFile.name}') \n " +
                "file was created so you can change and rename it.")
    }
}
