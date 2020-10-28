package com.github.emulio.yaml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PropertyPlaceholderHelper;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class YamlReaderHelper {

	private static final Logger logger = LoggerFactory.getLogger(YamlReaderHelper.class);

	private static void createConfigFromClasspath(final File esconfig) {
		logger.info("Creating emulio-platforms.yaml blank file.");

		try (final InputStream esTemplateStream = YamlReaderHelper.class.getResourceAsStream("/emulio-platforms.yaml");
			 final FileOutputStream fos = new FileOutputStream(esconfig)) {

			final byte[] buff = new byte[4096]; // 4kb
			int bytesRead;
			while ((bytesRead = esTemplateStream.read(buff)) != -1) fos.write(buff, 0, bytesRead);

			fos.flush();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		logger.info("Sample ");
	}

	public static Map<Object, Object> parse(final File configFile) {
		if (!configFile.exists()) createConfigFromClasspath(configFile);

//		System.out.printf("Reading configuration file [%s]\n", configFile.getAbsolutePath());

		final Yaml yaml = new Yaml();
		final Map<Object, Object> esConfig;
		try (final FileInputStream fis = new FileInputStream(configFile)) {
			esConfig = (Map<Object, Object>) yaml.load(fis);
//			System.out.println("Config file loaded");
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		YamlReaderHelper.prepareConfig(esConfig);

		return esConfig;
	}

	public static void prepareConfig(final Map<Object, Object> esConfig) {
		if (!esConfig.containsKey("esrunner.home")) {

			//String esRunnerHome = configFile.getParentFile().getAbsolutePath();

			final String classPathProperty = System.getProperty("java.class.path");
			final String[] paths = classPathProperty.split(File.pathSeparator);
			for (final String path : paths) if (path.contains("esrunner")) esConfig.put("esrunner.home", path);
		}

		esConfig.put("rom.raw", "\"%ROM_RAW%\"");
		esConfig.put("rom.file", "\"%ROM_RAW%\"");
		esConfig.put("rom", "\"%ROM%\"");
		esConfig.put("basename", "\"%BASENAME%\"");

		YamlReaderHelper.expandVars(esConfig);
	}

	public static void expandVars(final Map<Object, Object> esConfig) {
		final HashMap<String, Object> flatenedConfig = new HashMap<>();
		flattenProperties(esConfig, flatenedConfig, null);
		expand(flatenedConfig);
		deflatenProperties(esConfig, flatenedConfig, null);
	}

	private static void expand(final HashMap<String, Object> flatenedConfig) {
		final PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}");
		final Set<Entry<String, Object>> entrySet = flatenedConfig.entrySet();
		for (final Entry<String, Object> entry : entrySet) {
			final Object value = entry.getValue();
			if (value instanceof String) entry.setValue(helper.replacePlaceholders((String) value, flatenedConfig));
			else if (value instanceof List) {
				final List<String> list = (List<String>) value;
				for (int i = 0; i < list.size(); i++)
					list.set(i, helper.replacePlaceholders(list.get(i), flatenedConfig));
			}
		}
	}

	private static void flattenProperties(final Map<Object, Object> esConfig, final HashMap<String, Object> flatenedConfig,
										  final String prefix) {
		final Set<Entry<Object, Object>> entries = esConfig.entrySet();
		for (final Entry<Object, Object> entry : entries) {
			final String key = (String) entry.getKey();
			final Object value = entry.getValue();
			if (value instanceof Map) flattenProperties(((Map<Object, Object>) value), flatenedConfig, key);
			else if (prefix != null) flatenedConfig.put(prefix + "." + key, value);
			else
				flatenedConfig.put(key, value);
		}
	}

	private static void deflatenProperties(final Map<Object, Object> esConfig, final HashMap<String, Object> flatenedConfig,
										   final String prefix) {

		final Set<Entry<Object, Object>> entries = esConfig.entrySet();
		for (final Entry<Object, Object> entry : entries) {
			final String key = (String) entry.getKey();
			final Object value = entry.getValue();

			if (value instanceof Map) deflatenProperties((Map<Object, Object>) value, flatenedConfig, key);
			else if (prefix != null) entry.setValue(flatenedConfig.get(prefix + "." + key));
			else
				entry.setValue(flatenedConfig.get(key));
		}
	}
}
