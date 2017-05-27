package esrunner;

import com.github.emulio.yaml.YamlReaderHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class ESRunner {

	public ESRunner(List<String> args) {
		System.out.println("Initializing ESRunner.");

		printProperties("System", System.getProperties());

		System.out.println("   _____ ____  ____                                \n"
				+ "  | ____/ ___||  _ \\ _   _ _ __  _ __   ___ _ __   \n"
				+ "  |  _| \\___ \\| |_) | | | | '_ \\| '_ \\ / _ \\ '__|  \n"
				+ "  | |___ ___) |  _ <| |_| | | | | | | |  __/ |     \n"
				+ "  |_____|____/|_| \\_\\\\__,_|_| |_|_| |_|\\___|_|     \n");

	}

	private void printProperties(final String name, final Properties properties) {
		System.out.println("===========================================");
		System.out.println(name + " Properties: ");
		System.out.println("===========================================");
		for (Entry<Object, Object> entry : properties.entrySet()) {
			System.out.printf("%s: %s\n", entry.getKey(), entry.getValue());
		}
		System.out.println("===========================================");
	}

	public static void main(String[] args) {
		new ESRunner(Arrays.asList(args)).run();
	}

	private void run() {

		try {
			final Map<Object, Object> esConfig = readConfig();
			overrideEmulationStationFile(esConfig);

		} catch (Throwable t) {
			if (t.getMessage() != null) {
				System.out.println("\n\n[ERROR] " + t.getMessage());
			}

			System.out.println("\n\n\n==========================================================");
			t.printStackTrace(System.out);
		}
	}

	private Map<Object, Object> readConfig() {
		final File esRunnerYaml = new File("esrunner.yaml");
		
		return YamlReaderHelper.parse(esRunnerYaml);
	}

	

	private void overrideEmulationStationFile(final Map<Object, Object> esConfig) {
		System.out.println("Overriding emulationstation config files");
		
		validateConfig(esConfig);
		
		final String es_systemsContent = generateESSystems(esConfig);
		
		final String esHome = (String) esConfig.get("emulationstation.home");
		final String esRunnerHome = (String) esConfig.get("esrunner.home");
		
		final File es_systemsFile = new File(esHome, "es_systems.cfg");
		
		writeFile(es_systemsContent, es_systemsFile);
		
		final Object esCommandObject = esConfig.get("emulationstation.command");
		final ProcessBuilder esProcessBuilder;
		if (esCommandObject instanceof List) {
			@SuppressWarnings("unchecked")
			final List<String> command = (List<String>) esCommandObject;
			esProcessBuilder = new ProcessBuilder(command.toArray(new String[command.size()]));
		} else {
			esProcessBuilder = new ProcessBuilder((String) esCommandObject);
		}
		
		System.out.println("Preparing to start emulationstation");
		
		final Map<String, String> environment = esProcessBuilder.environment();
		environment.put("HOME", esRunnerHome);
		
		System.out.println("Executing emulationstation.. ");
		try {
			final Process process = esProcessBuilder.start();
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	private static void writeFile(final String content, final File file) {
		System.out.printf("Writing file: [%s] \n", file.getAbsolutePath());
		try (final FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(content.getBytes());
			fos.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Writing done");
	}

	private String generateESSystems(final Map<Object, Object> esConfig) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<systemList>\n");
		
		@SuppressWarnings("unchecked")
		final Map<String, Object> systems = (Map<String, Object>) esConfig.get("systems");
		for (Entry<String, Object> entry : systems.entrySet()) {
		
			final String systemName = entry.getKey();
			System.out.println("Generating config for: " + systemName);
			 
			@SuppressWarnings("unchecked")
			final Map<String, Object> systemMap = (Map<String, Object>) entry.getValue();
			
			sb.append("\t<system>\n");
			sb.append(String.format("\t\t<name>%s</name>\n", systemName));
			sb.append(String.format("\t\t<platform>%s</platform>\n", systemName));
			sb.append(String.format("\t\t<theme>%s</theme>\n", systemName));
			
			final String platformName = (String) systemMap.get("platform.name");
			sb.append(String.format("\t\t<fullname>%s</fullname>\n", platformName));
			
			final Object romsExtensionsObject = systemMap.get("roms.extensions");
			sb.append(String.format("\t\t<extension>"));
			if (romsExtensionsObject instanceof List) {
				@SuppressWarnings("unchecked")
				final List<String> romsExtensions = (List<String>) romsExtensionsObject;
					
				for (String extension : romsExtensions) {
					sb.append(extension.toLowerCase()).append(" ").append(extension.toUpperCase()).append(" ");
				}
				sb.setLength(sb.length() -1);
			} else {
				final String extension = (String) romsExtensionsObject;
				sb.append(extension.toLowerCase()).append(" ").append(extension.toUpperCase());
			}
			sb.append("</extension>\n");
			
			final String romsPath = (String) systemMap.get("roms.path");
			sb.append(String.format("\t\t<path>%s</path>\n", romsPath));
			
			final Object runCommandObject = systemMap.get("run.command");
			sb.append(String.format("\t\t<command>"));
			if (runCommandObject instanceof List) {
				@SuppressWarnings("unchecked")
				final List<String> runCommands = (List<String>) runCommandObject;
					
				for (String command: runCommands) {
					if (command.indexOf(' ') != -1) {
						sb.append("\"").append(command).append("\" ");
					} else {
						sb.append(command).append(" ");
					}
				}
				sb.setLength(sb.length() -1);
			} else {
				sb.append((String) runCommandObject);
			}
			sb.append("</command>\n");
			sb.append("\t</system>\n");
		}		
		sb.append("</systemList>");
		
		
		final String es_systemsContent = sb.toString();
		
		return es_systemsContent;
	}

	private void validateConfig(final Map<Object, Object> esConfig) {
		checkValue("You must provide a systems on your yaml file.", esConfig.get("systems"));
		checkValue("You must provide a emulationstation.home on your yaml file.", esConfig.get("emulationstation.home"));
		checkValue("You must provide a emulationstation.command on your yaml file.", esConfig.get("emulationstation.command"));
		checkValue("You need to specify at least a system.", esConfig.get("systems"));
	}

	

	private void checkValue(String message, Object value) {
		if (value == null) {
			throw new RuntimeException(message);
		}
	}

	

}
