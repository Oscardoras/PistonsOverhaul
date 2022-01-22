package me.oscardoras.pistonsoverhaul;


import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.block.Block;

import me.oscardoras.pistonsoverhaul.io.ConfigurationFile;

public class MovableBlock {
	
	protected static final Map<World, ConfigurationFile> configs = new HashMap<World, ConfigurationFile>();
	
	protected final Block block;
	protected final ConfigurationFile config;
	
	public MovableBlock(Block block) {
		this.block = block;
		
		World world = block.getWorld();
		if (configs.containsKey(world)) this.config = configs.get(world);
		else {
			this.config = new ConfigurationFile(world.getWorldFolder().getPath() + "/data/movable_blocks.yml");
			configs.put(world, config);
		}
	}
	
	public boolean isMovable() {
		String path = block.getX() + "." + block.getY() + "." + block.getZ();
		if (config.isBoolean(path)) return config.getBoolean(path);
		return false;
	}
	
	public void setMovable(boolean movable) {
		if (movable) config.set(block.getX() + "." + block.getY() + "." + block.getZ(), true);
		else if (config.contains(block.getX() + "." + block.getY() + "." + block.getZ())) {
			config.set(block.getX() + "." + block.getY() + "." + block.getZ(), null);
			if (config.getConfigurationSection(block.getX() + "." + block.getY()).getKeys(false).isEmpty()) {
				config.set(block.getX() + "." + block.getY(), null);
			}
			if (config.getConfigurationSection("" + block.getX()).getKeys(false).isEmpty()) {
				config.set("" + block.getX(), null);
			}
		}
	}
	
	public int getPistonType() {
		String path = block.getX() + "." + block.getY() + "." + block.getZ();
		if (config.isInt(path)) return config.getInt(path);
		return 0;
	}
	
	public void setPistonType(int type) {
		if (type != 0) config.set(block.getX() + "." + block.getY() + "." + block.getZ(), type);
		else {
			config.set(block.getX() + "." + block.getY() + "." + block.getZ(), null);
			if (config.getConfigurationSection(block.getX() + "." + block.getY()).getKeys(false).isEmpty()) {
				config.set(block.getX() + "." + block.getY(), null);
			}
			if (config.getConfigurationSection("" + block.getX()).getKeys(false).isEmpty()) {
				config.set("" + block.getX(), null);
			}
		}
	}
	
}