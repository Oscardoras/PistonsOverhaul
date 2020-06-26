package me.oscardoras.pistonsoverhaul;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Vector;

import me.oscardoras.spigotutils.BukkitPlugin;
import me.oscardoras.spigotutils.io.TranslatableMessage;

public class PistonsOverhaulPlugin extends BukkitPlugin implements Listener {
	
	public static PistonsOverhaulPlugin plugin;
	
	public PistonsOverhaulPlugin() {
		plugin = this;
	}
	
	
	public int maxBlocks;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		
		maxBlocks = getConfig().getInt("max_blocks");
		
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(this, this);
		pm.registerEvents(new PistonListener(), this);
	}
	
	@Override
	public void onDisable() {
		for (World world : Bukkit.getWorlds())
			if (MovableBlock.configs.containsKey(world))
				MovableBlock.configs.get(world).save();
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onWorldSave(WorldSaveEvent e) {
		World world = e.getWorld();
		if (MovableBlock.configs.containsKey(world))
			MovableBlock.configs.get(world).save();
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlaceBlock(BlockPlaceEvent e) {
		if (!e.isCancelled()) {
			Player player = e.getPlayer();
			Block block = e.getBlock();
			ItemStack item = player.getInventory().getItemInOffHand();
			if (item != null && item.getType() == Material.CRAFTING_TABLE && e.getBlockPlaced().getType().isSolid()) new MovableBlock(block).setMovable(true);
			else new MovableBlock(block).setMovable(false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBreakBlock(BlockBreakEvent e) {
		if (!e.isCancelled()) {
			Block block = e.getBlock();
			new MovableBlock(block).setMovable(false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		if (!player.isSneaking() && e.getAction() == Action.RIGHT_CLICK_BLOCK && e.hasItem() && e.getItem().getType() == Material.STICK) {
			Block block = e.getClickedBlock();
			if (block != null && (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON)) {
				List<String> types = new ArrayList<String>();
				types.add("default");
				types.add("sliding");
				types.add("turning_left");
				types.add("turning_right");
				
				MovableBlock movableBlock = new MovableBlock(block);
				int oldType = movableBlock.getPistonType();
				int newType = 0;
				if (oldType != 3) newType = oldType+1;
				else newType = 0;
				movableBlock.setPistonType(newType);
				player.sendTitle("", new TranslatableMessage(this, "piston_type." + types.get(newType)).getMessage(player), 10, 70, 20);
				e.setCancelled(true);
			}
		}
	}
	
	public void onQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		if (player.hasMetadata("Passenger")) {
			player.setVelocity(new Vector(0, 0.1, 0));
			player.setGravity(true);
			player.removeMetadata("Passenger", this);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onKick(PlayerKickEvent e) {
		Player player = e.getPlayer();
		if (player.hasMetadata("Passenger") && e.getReason().equals("Flying is not enabled on this server")) e.setCancelled(true);
	}
	
}