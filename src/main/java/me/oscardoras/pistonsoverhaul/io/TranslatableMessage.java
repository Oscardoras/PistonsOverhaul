package me.oscardoras.pistonsoverhaul.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Player;

public final class TranslatableMessage {

	private static Map<String, Properties> translations = new HashMap<String, Properties>();

	private TranslatableMessage() {}

	public static void loadTranslation(File file) {
		try {
			JarFile jarFile = new JarFile(file);
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (entry.getName().startsWith("translations"))
					try {
						Properties properties = new Properties();
						properties.load(new InputStreamReader(TranslatableMessage.class.getClassLoader().getResourceAsStream(entry.getName()), Charset.forName("UTF-8")));
						String[] files = entry.getName().split("/");
						translations.put(files[files.length - 1].split("\\.")[0], properties);
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			jarFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getMessage(CommandSender sender, String key) {
		return getMessage(getLanguage(sender), key, translations);
	}
	
	public static String getLanguage(CommandSender sender) {
		if (sender instanceof ProxiedCommandSender) sender = ((ProxiedCommandSender) sender).getCaller();
		if (sender instanceof Player) return ((Player) sender).getLocale().split("_")[0].toLowerCase();
		else return "en";
	}
	
	private static String getMessage(String language, String key, Map<String, Properties> translates) {
		key = key.toLowerCase();
		if (translates.containsKey(language)) {
			Properties properties = translates.get(language);
			if (properties.containsKey(key)) return properties.getProperty(key);
		}
		if (translates.containsKey("en")) {
			Properties properties = translates.get("en");
			if (properties.containsKey(key)) return properties.getProperty(key);
		}
		for (String name : translates.keySet()) if (!name.equals(language) && !name.equals("en")) {
			Properties properties = translates.get(name);
			if (properties.containsKey(key)) return properties.getProperty(key);
		}
		throw new TranslatableMessageException(key);
	}
	
	public static class TranslatableMessageException extends IllegalArgumentException {

		private static final long serialVersionUID = -8022664399481137796L;

		public TranslatableMessageException(String key) {
			super(key);
		}
		
	}
	
}