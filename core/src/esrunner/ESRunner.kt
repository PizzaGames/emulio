package esrunner

import com.github.emulio.yaml.YamlReaderHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * This is an utilitary class used in the past on a smaller project
 * that manages all the bad configs in the xmls from EmulationStation
 *
 * This was used in another script/project to make EmulationStation usage
 * much more easy to configure and now part of this implementation is used
 * in the Emulio project
 *
 * A lot of code here can be changed and upgraded and we can break compatibility
 * in complete with EmulationStation for now.
 */
class ESRunner {

    init {
        println("Initializing ESRunner.")

        printProperties("System", System.getProperties())

        println("   _____ ____  ____                                \n"
                + "  | ____/ ___||  _ \\ _   _ _ __  _ __   ___ _ __   \n"
                + "  |  _| \\___ \\| |_) | | | | '_ \\| '_ \\ / _ \\ '__|  \n"
                + "  | |___ ___) |  _ <| |_| | | | | | | |  __/ |     \n"
                + "  |_____|____/|_| \\_\\\\__,_|_| |_|_| |_|\\___|_|     \n")

    }

    private fun printProperties(name: String, properties: Properties) {
        println("===========================================")
        println(name + " Properties: ")
        println("===========================================")
        for ((key, value) in properties) {
            System.out.printf("%s: %s\n", key, value)
        }
        println("===========================================")
    }

    private fun run() {

        try {
            val esConfig = readConfig()
            overrideEmulationStationFile(esConfig)

        } catch (t: Throwable) {
            if (t.message != null) {
                println("\n\n[ERROR] " + t.message)
            }

            println("\n\n\n==========================================================")
            t.printStackTrace(System.out)
        }

    }

    private fun readConfig(): Map<Any, Any> {
        val esRunnerYaml = File("esrunner.yaml")

        return YamlReaderHelper.parse(esRunnerYaml)
    }


    private fun overrideEmulationStationFile(esConfig: Map<Any, Any>) {
        println("Overriding EmulationStation config files")

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
            @Suppress("UNCHECKED_CAST")
            val command = esCommandObject as List<String>
            ProcessBuilder(*command.toTypedArray())
        } else {
            ProcessBuilder(esCommandObject as String)
        }

        println("Preparing to start EmulationStation")

        val environment = esProcessBuilder.environment()
        environment.put("HOME", esRunnerHome)

        println("Executing EmulationStation.. ")
        try {
            /*val process = */esProcessBuilder.start()
            //TODO Control the process in someway??
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    private fun generateESSystems(esConfig: Map<Any, Any>): String {
        val sb = StringBuilder()
        sb.append("<systemList>\n")

        @Suppress("UNCHECKED_CAST")
        val systems = esConfig["systems"] as Map<String, Any>
        for ((systemName, value) in systems) {

            println("Generating config for: " + systemName)

            @Suppress("UNCHECKED_CAST")
            val systemMap = value as Map<String, Any>

            sb.append("\t<system>\n")
            sb.append(String.format("\t\t<name>%s</name>\n", systemName))
            sb.append(String.format("\t\t<platform>%s</platform>\n", systemName))
            sb.append(String.format("\t\t<theme>%s</theme>\n", systemName))

            val platformName = systemMap["platform.name"] as String
            sb.append(String.format("\t\t<fullname>%s</fullname>\n", platformName))

            val romsExtensionsObject = systemMap["roms.extensions"]
            sb.append(String.format("\t\t<extension>"))
            if (romsExtensionsObject is List<*>) {
                @Suppress("UNCHECKED_CAST")
                val romsExtensions = romsExtensionsObject as List<String>

                for (extension in romsExtensions) {
                    sb.append(extension.toLowerCase()).append(" ").append(extension.toUpperCase()).append(" ")
                }
                sb.setLength(sb.length - 1)
            } else {
                val extension = romsExtensionsObject as String
                sb.append(extension.toLowerCase()).append(" ").append(extension.toUpperCase())
            }
            sb.append("</extension>\n")

            val romsPath = systemMap["roms.path"] as String
            sb.append(String.format("\t\t<path>%s</path>\n", romsPath))

            val runCommandObject = systemMap["run.command"]
            sb.append(String.format("\t\t<command>"))
            if (runCommandObject is List<*>) {
                @Suppress("UNCHECKED_CAST")
                val runCommands = runCommandObject as List<String>

                for (command in runCommands) {
                    if (command.indexOf(' ') != -1) {
                        sb.append("\"").append(command).append("\" ")
                    } else {
                        sb.append(command).append(" ")
                    }
                }
                sb.setLength(sb.length - 1)
            } else {
                sb.append(runCommandObject as String)
            }
            sb.append("</command>\n")
            sb.append("\t</system>\n")
        }
        sb.append("</systemList>")


        return sb.toString()
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

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            ESRunner().run()
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

            println("Writing done")
        }
    }


}