package me.oscardoras.pistonsoverhaul.io;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ConfigurationFile extends YamlConfiguration {
	
	protected final File file;
	
	public ConfigurationFile(File file) {
		this.file = file;
		try {
			file.setReadable(true);
			file.setWritable(true);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			load(file);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public ConfigurationFile(String path) {
		this(new File(path));
	}
	
	public void save() {
		try {
			this.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}