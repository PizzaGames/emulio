package com.github.emulio.yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import esrunner.ESRunner;
import org.springframework.util.PropertyPlaceholderHelper;
import org.yaml.snakeyaml.Yaml;

public final class YamlReaderHelper {
	
	private static void createConfigFromClasspath(final File esconfig) {
//		System.out.printf("Config file not found in initial dir. Creating from template. [%s]\n",
//				esconfig.getAbsolutePath());
		try (final InputStream esTemplateStream = ESRunner.class.getResourceAsStream("/esrunner_template.yaml");
			 final FileOutputStream fos = new FileOutputStream(esconfig)) {

			final byte[] buff = new byte[4096]; // 4kb
			int bytesRead = 0;
			while ((bytesRead = esTemplateStream.read(buff)) != -1) {
				fos.write(buff, 0, bytesRead);
			}

			fos.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
//		System.out.println("Config file created.");
	}

	public static Map<Object, Object> parse(final File configFile) {
		if (!configFile.exists()) {
			createConfigFromClasspath(configFile);
		}

//		System.out.printf("Reading configuration file [%s]\n", configFile.getAbsolutePath());

		final Yaml yaml = new Yaml();
		final Map<Object, Object> esConfig;
		try (final FileInputStream fis = new FileInputStream(configFile)) {
			esConfig = (Map<Object, Object>) yaml.load(fis);
//			System.out.println("Config file loaded");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		YamlReaderHelper.prepareConfig(esConfig, configFile);

		return esConfig;
	}
	
	public static void prepareConfig(final Map<Object, Object> esConfig, final File configFile) {
		if (!esConfig.containsKey("esrunner.home")) {
			
			//String esRunnerHome = configFile.getParentFile().getAbsolutePath();
			
			final String classPathProperty = System.getProperty("java.class.path");
			final String[] paths = classPathProperty.split(File.pathSeparator);
			for (String path : paths) {
				if (path.contains("esrunner")) {
					esConfig.put("esrunner.home", path);
				}
			}
		}

		esConfig.put("rom.raw", "\"%ROM_RAW%\"");
		esConfig.put("rom.file", "\"%ROM_RAW%\"");
		esConfig.put("rom", "\"%ROM%\"");
		esConfig.put("basename", "\"%BASENAME%\"");

		YamlReaderHelper.expandVars(esConfig);
	}
	
	public static void expandVars(Map<Object, Object> esConfig) {
		final HashMap<String, Object> flatenedConfig = new HashMap<>();
		flattenProperties(esConfig, flatenedConfig, null);
		expand(flatenedConfig);
		deflatenProperties(esConfig, flatenedConfig, null);
	}

	private static void expand(final HashMap<String, Object> flatenedConfig) {
		PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}");
		final Set<Entry<String, Object>> entrySet = flatenedConfig.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			Object value = entry.getValue();
			if (value instanceof String) {
				entry.setValue(helper.replacePlaceholders((String) value, flatenedConfig));
			} else if (value instanceof List) {
				@SuppressWarnings("unchecked")
				final List<String> list = (List<String>) value;
				for (int i = 0; i < list.size(); i++) {
					list.set(i, helper.replacePlaceholders(list.get(i), flatenedConfig));
				}
			}
		}
	}

	private static void flattenProperties(Map<Object, Object> esConfig, HashMap<String, Object> flatenedConfig,
										  final String prefix) {
		final Set<Entry<Object, Object>> entries = esConfig.entrySet();
		for (Entry<Object, Object> entry : entries) {
			String key = (String) entry.getKey();
			final Object value = entry.getValue();
			if (value instanceof Map) {
				flattenProperties(((Map<Object, Object>) value), flatenedConfig, key);
			} else {
				if (prefix != null) {
					flatenedConfig.put(prefix + "." + key, value);
				} else {
					flatenedConfig.put(key, value);
				}
			}
		}
	}

	private static void deflatenProperties(Map<Object, Object> esConfig, HashMap<String, Object> flatenedConfig,
			final String prefix) {
		
		final Set<Entry<Object, Object>> entries = esConfig.entrySet();
		for (Entry<Object, Object> entry : entries) {
			String key = (String) entry.getKey();
			final Object value = entry.getValue();

			if (value instanceof Map) {
				deflatenProperties((Map<Object, Object>) value, flatenedConfig, key);
			} else {
				if (prefix != null) {
					entry.setValue(flatenedConfig.get(prefix + "." + key));
				} else {
					entry.setValue(flatenedConfig.get(key));
				}
				
			}
		}
	}
}
