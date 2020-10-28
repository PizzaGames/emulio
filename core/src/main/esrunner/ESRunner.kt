package esrunner

import com.github.emulio.yaml.YamlReaderHelper
import mu.KotlinLogging
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * This is an util class used in the past on a smaller project
 * that manages all the bad configs in the xml files from EmulationStation
 *
 * This was used in another script/project to make EmulationStation usage
 * much more easy to configure and now part of this implementation is used
 * in the Emulio project
 *
 * A lot of code here can be changed and upgraded and we can break compatibility
 * in complete with EmulationStation for now.
 */
class ESRunner : Runnable {

    val logger = KotlinLogging.logger { }

    init {
        logger.info { "Initializing ESRunner." }
        printProperties()
        printMotd()
    }

    private fun printMotd() {
        logger.info {
            """
             _______  _______  ______    __   __  __    _  __    _  _______  ______   
            |       ||       ||    _ |  |  | |  ||  |  | ||  |  | ||       ||    _ |  
            |    ___||  _____||   | ||  |  | |  ||   |_| ||   |_| ||    ___||   | ||  
            |   |___ | |_____ |   |_||_ |  |_|  ||       ||       ||   |___ |   |_||_ 
            |    ___||_____  ||    __  ||       ||  _    ||  _    ||    ___||    __  |
            |   |___  _____| ||   |  | ||       || | |   || | |   ||   |___ |   |  | |
            |_______||_______||___|  |_||_______||_|  |__||_|  |__||_______||___|  |_|
            
            """.trimIndent()
        }
    }

    private fun printProperties() {
        logger.info { "===========================================" }
        logger.info { "System Properties: " }
        logger.info { "===========================================" }
        System.getProperties().forEach { key, value ->
            logger.info { "$key: $value" }
        }
        logger.info { "===========================================" }
    }

    override fun run() {
        try {
            overrideEmulationStationFile(readConfig())
        } catch (t: Throwable) {
            handleThrowable(t)
        }
    }

    private fun handleThrowable(t: Throwable) {
        logger.error(t) { "Internal error ocurred" }
    }

    private fun readConfig(): Map<Any, Any> {
        val esRunnerYaml = File("esrunner.yaml")
        return YamlReaderHelper.parse(esRunnerYaml)
    }

    private fun overrideEmulationStationFile(esConfig: Map<Any, Any>) {
        logger.info { "Overriding EmulationStation config files" }

        validateConfig(esConfig)

        @Suppress("LocalVariableName")
        val es_systemsContent = generateESSystems(esConfig)

        val esHome = esConfig["EmulationStation.home"] as String
        val esRunnerHome = esConfig["esrunner.home"] as String

        @Suppress("LocalVariableName")
        val es_systemsFile = File(esHome, "es_systems.cfg")

        writeFile(es_systemsContent, es_systemsFile)

        val esCommandObject = esConfig["EmulationStation.command"]
        val esProcessBuilder: ProcessBuilder

        esProcessBuilder = if (esCommandObject is List<*>) {
            getListProcessBuilder(esCommandObject)
        } else {
            getProcessBuilder(esCommandObject)
        }

        logger.info { "Preparing to start EmulationStation" }

        val environment = esProcessBuilder.environment()
        environment["HOME"] = esRunnerHome

        logger.info { "Executing EmulationStation.. " }
        try {
            esProcessBuilder.start()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    private fun getProcessBuilder(esCommandObject: Any?) = ProcessBuilder(esCommandObject as String)

    private fun getListProcessBuilder(esCommandObject: Any?): ProcessBuilder {
        @Suppress("UNCHECKED_CAST")
        val command = esCommandObject as List<String>
        return ProcessBuilder(*command.toTypedArray())
    }

    private fun generateESSystems(esConfig: Map<Any, Any>): String {
        return StringBuilder()
            .append("<systemList>\n")
            .appendSystems(esConfig)
            .append("</systemList>").toString()
    }

    private fun StringBuilder.appendSystems(esConfig: Map<Any, Any>): StringBuilder {
        getSystems(esConfig).forEach { (systemName, value) ->
            this.appendSystem(systemName, value)
        }
        return this
    }

    @Suppress("UNCHECKED_CAST")
    private fun getSystems(esConfig: Map<Any, Any>) =
            esConfig["systems"] as Map<String, Any>

    private fun StringBuilder.appendSystem(systemName: String, value: Any) {
        logger.info { "Generating config for: $systemName" }

        val systemMap = getSystemMap(value)

        this.append("\t<system>\n")
                .appendName(systemName)
                .appendPlatform(systemName)
                .appendTheme(systemName)
                .appendPlatformName(systemMap)
                .appendExtension(systemMap)
                .appendPath(systemMap)
                .appendCommand(systemMap)
            .append("\t</system>\n")
    }

    private fun StringBuilder.appendCommand(systemMap: Map<String, Any>): StringBuilder {
        val runCommandObject = systemMap["run.command"]
        this.append(String.format("\t\t<command>"))
        if (runCommandObject is List<*>) {
            @Suppress("UNCHECKED_CAST")
            val runCommands = runCommandObject as List<String>

            for (command in runCommands) {
                if (command.indexOf(' ') != -1) {
                    this.append("\"").append(command).append("\" ")
                } else {
                    this.append(command).append(" ")
                }
            }
            this.setLength(this.length - 1)
        } else {
            this.append(runCommandObject as String)
        }
        this.append("</command>\n")

        return this
    }

    private fun StringBuilder.appendPath(systemMap: Map<String, Any>): StringBuilder {
        return this.append(String.format("\t\t<path>%s</path>\n", systemMap["roms.path"] as String))
    }

    private fun StringBuilder.appendExtension(systemMap: Map<String, Any>): StringBuilder {
        val romsExtensionsObject = systemMap["roms.extensions"]
        this.append(String.format("\t\t<extension>"))
        if (romsExtensionsObject is List<*>) {
            this.appendExtensionsList(romsExtensionsObject)
        } else {
            this.appendExtension(romsExtensionsObject)
        }
        this.append("</extension>\n")

        return this
    }

    private fun StringBuilder.appendExtension(romsExtensionsObject: Any?) {
        val extension = romsExtensionsObject as String
        this.append(extension.toLowerCase()).append(" ").append(extension.toUpperCase())
    }

    private fun StringBuilder.appendExtensionsList(romsExtensionsObject: Any?) {
        @Suppress("UNCHECKED_CAST")
        val romsExtensions = romsExtensionsObject as List<String>
        romsExtensions.forEach { extension ->
            this.append(extension.toLowerCase()).append(" ").append(extension.toUpperCase()).append(" ")
        }
        this.setLength(this.length - 1)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getSystemMap(value: Any): Map<String, Any> {
        return value as Map<String, Any>
    }

    private fun StringBuilder.appendPlatformName(systemMap: Map<String, Any>): StringBuilder {
        return this.append(String.format("\t\t<fullname>%s</fullname>\n", systemMap["platform.name"] as String))
    }

    private fun StringBuilder.appendTheme(systemName: String): StringBuilder {
        return this.append(String.format("\t\t<theme>%s</theme>\n", systemName))
    }

    private fun StringBuilder.appendPlatform(systemName: String): StringBuilder {
        return this.append(String.format("\t\t<platform>%s</platform>\n", systemName))
    }

    private fun StringBuilder.appendName(systemName: String): StringBuilder {
        return this.append(String.format("\t\t<name>%s</name>\n", systemName))
    }

    private fun validateConfig(esConfig: Map<Any, Any>) {
        checkValue("You must provide a systems on your yaml file.", esConfig["systems"])
        checkValue("You must provide a EmulationStation.home on your yaml file.", esConfig["EmulationStation.home"])
        checkValue("You must provide a EmulationStation.command on your yaml file.", esConfig["EmulationStation.command"])
        checkValue("You need to specify at least a system.", esConfig["systems"])
    }

    private fun checkValue(message: String, value: Any?) {
        if (value == null) {
            throw RuntimeException(message)
        }
    }

    private fun writeFile(content: String, file: File) {
        System.out.printf("Writing file: [%s] \n", file.absolutePath)
        try {
            FileOutputStream(file).use { fos ->
                fos.write(content.toByteArray())
                fos.flush()
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        logger.info { "Writing done" }
    }

}

fun main() {
    ESRunner().run { }
}