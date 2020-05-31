package org.bukkitplugin.pistonsoverhaul;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.bukkitplugin.pistonsoverhaul.movingblock.MovingBlock;
import org.bukkitplugin.pistonsoverhaul.movingblock.SlidingMovingBlock;
import org.bukkitplugin.pistonsoverhaul.movingblock.TurningMovingBlock;

public class PistonListener implements Listener {
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPistonExtend(BlockPistonExtendEvent e) {
		Block piston = e.getBlock();
		int type = new MovableBlock(piston).getPistonType();
		if (type == 1 || type == 2 || type == 3) {
			if (type != 1) e.setCancelled(true);
			
			List<Block> blocks = getBlocks(piston, PistonsOverhaulPlugin.plugin.maxBlocks, type != 1);
			if (blocks.isEmpty()) return;
			
			
			BlockFace direction = e.getDirection();
			Rotation rotation;
			int time;
			if (type == 1) {
				rotation = null;
				time = 20;
			} else {
				rotation = Rotation.getRotation(direction, type == 3);
				double maxDistance = 0;
				for (Block b : blocks) {
					double distance = rotation.getDistance(piston, b.getLocation());
					if (distance > maxDistance) maxDistance = distance;
				}
				if (maxDistance < 0.5d) return;
				time = (int) Math.round(maxDistance*Math.PI*10);
			}

			World world = piston.getWorld();
			
			List<MovingBlock> movingBlocks = new ArrayList<MovingBlock>();
			for (Block b : blocks) {
				BlockData data = b.getBlockData();
				
				b.setType(Material.AIR);
				new MovableBlock(b).setMovable(false);
				
				Location fallingLocation = b.getLocation().add(0.5d, 0d, 0.5d);
				FallingBlock fallingBlock = world.spawnFallingBlock(fallingLocation, data);
				fallingBlock.setGravity(false);
				fallingBlock.setDropItem(false);
				fallingBlock.setHurtEntities(true);
				fallingBlock.setMetadata("MovableBlock", new FixedMetadataValue(PistonsOverhaulPlugin.plugin, true));
				
				if (type == 1) movingBlocks.add(new SlidingMovingBlock(fallingBlock, b, direction));
				else movingBlocks.add(new TurningMovingBlock(fallingBlock, b, rotation, piston, time));
			}
			
			
			final Counter counter = new Counter();
			counter.id = Bukkit.getScheduler().scheduleSyncRepeatingTask(PistonsOverhaulPlugin.plugin, () -> {
				if (!counter.cancel)
					for (MovingBlock movingBlock : movingBlocks)
						if (movingBlock.getTarget(counter.tick).add(0.5d, 0.5d, 0.5d).getBlock().getType().isSolid()) counter.cancel = true;
				
				if (!counter.cancel) counter.up();
				else counter.down();
				
				for (MovingBlock movingBlock : movingBlocks) move(movingBlock.getFallingBlock(), movingBlock.getTarget(counter.tick));
				
				if (!counter.cancel && counter.tick >= time) {
					for (MovingBlock movingBlock : movingBlocks) {
						Block to = movingBlock.getTo();
						
						to.breakNaturally();
						
						FallingBlock fallingBlock = movingBlock.getFallingBlock();
						BlockData data = fallingBlock.getBlockData();
						rotate(rotation, data);
						to.setBlockData(data);
						new MovableBlock(to).setMovable(true);
						fallingBlock.remove();
					}
					if (type != 1) new MovableBlock(piston).setPistonType(type);
					Bukkit.getScheduler().cancelTask(counter.id);
				} else if (counter.cancel && counter.tick <= 0) {
					for (MovingBlock movingBlock : movingBlocks) {
						Block from = movingBlock.getFrom();
						
						from.breakNaturally();
						
						FallingBlock fallingBlock = movingBlock.getFallingBlock();
						from.setBlockData(fallingBlock.getBlockData());
						new MovableBlock(from).setMovable(true);
						fallingBlock.remove();
					}
					if (type != 1) new MovableBlock(piston).setPistonType(type);
					Bukkit.getScheduler().cancelTask(counter.id);
				}
			}, 0, 1);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityChangeBlock(EntityChangeBlockEvent e) {
		Entity entity = e.getEntity();
		if (entity instanceof FallingBlock && entity.hasMetadata("MovableBlock"))
			e.setCancelled(true);
	}
	
	protected static List<Block> getBlocks(Block piston, int max, boolean keepPiston) {
		List<Block> blocks = new ArrayList<Block>();
		
		List<Material> excludes = new ArrayList<Material>();
		excludes.add(Material.AIR);
		excludes.add(Material.BARRIER);
		excludes.add(Material.BEDROCK);
		excludes.add(Material.OBSIDIAN);
		
		List<Block> last = new ArrayList<Block>();
		last.add(piston);
		boolean found = true;
		while (found) {
			found = false;
			List<Block> toAdd = new ArrayList<Block>();
			for (Block b : last) {
				List<Block> faces = new ArrayList<Block>();
				faces.add(b.getRelative(BlockFace.DOWN));
				faces.add(b.getRelative(BlockFace.EAST));
				faces.add(b.getRelative(BlockFace.NORTH));
				faces.add(b.getRelative(BlockFace.SOUTH));
				faces.add(b.getRelative(BlockFace.UP));
				faces.add(b.getRelative(BlockFace.WEST));
				for (Block block : faces) {
					if (!excludes.contains(block.getType()) && !toAdd.contains(block) && !blocks.contains(block) && new MovableBlock(block).isMovable()) {
						toAdd.add(block);
						found = true;
					}
				}
			}
			
			blocks.addAll(toAdd);
			last = toAdd;
			if (blocks.size() + (keepPiston ? 1 : 0) > max) return new ArrayList<Block>();
		}
		
		if (keepPiston) blocks.add(piston);
		return blocks;
	}
	
	protected static void move(FallingBlock fallingBlock, Location to) {
		Location l = fallingBlock.getLocation();
		Vector vec = new Vector(to.getX() + 0.5d - l.getX(), to.getY() - l.getY(), to.getZ() + 0.5d - l.getZ());
		fallingBlock.setVelocity(vec);
		
		for (Entity passenger : fallingBlock.getWorld().getNearbyEntities(fallingBlock.getLocation().add(0, 1, 0), 0.5d, 0.5d, 0.5d)) {
			if (!passenger.hasMetadata("MovableBlock") && (!(passenger instanceof Player) || !((Player) passenger).isFlying())) {
				passenger.setGravity(false);
				String uuid = UUID.randomUUID().toString();
				passenger.setMetadata("Passenger", new FixedMetadataValue(PistonsOverhaulPlugin.plugin, uuid));
				Bukkit.getScheduler().runTaskLater(PistonsOverhaulPlugin.plugin, () -> {
					if (!passenger.hasMetadata("Passenger") || passenger.getMetadata("Passenger").get(0).asString().equals(uuid)) {
						passenger.setGravity(true);
						passenger.setVelocity(new Vector(0d, 0.3d ,0d));
						passenger.removeMetadata("Passenger", PistonsOverhaulPlugin.plugin);
					}
				}, 2);
				passenger.setVelocity(vec);
			}
		}
	}
	
	protected static void rotate(Rotation rotation, BlockData data) {
		if (rotation == null) return;
		if (data instanceof Orientable) {
			Orientable orientable = (Orientable) data;
			Set<Axis> axes = orientable.getAxes();
			Axis axis = orientable.getAxis();
			if(rotation.isYaw()) {
				if (axes.contains(Axis.Z) && axes.contains(Axis.X)) {
					if (axis == Axis.Z) orientable.setAxis(Axis.X);
					if (axis == Axis.X) orientable.setAxis(Axis.Z);
				}
			} else if (rotation.isRoll()) {
				if (axes.contains(Axis.X) && axes.contains(Axis.Y)) {
					if (axis == Axis.X) orientable.setAxis(Axis.Y);
					if (axis == Axis.Y) orientable.setAxis(Axis.X);
				}
			} else if (rotation.isPitch()) {
				if (axes.contains(Axis.Y) && axes.contains(Axis.Z)) {
					if (axis == Axis.Y) orientable.setAxis(Axis.Z);
					if (axis == Axis.Z) orientable.setAxis(Axis.Y);
				}
			}
		}
		if (data instanceof Directional) {
			Directional directional = (Directional) data;
			BlockFace facing = directional.getFacing();
			try {
				if (rotation.isYaw()) {
					if (rotation.isRight()) {
						switch (facing) {
						case NORTH:
							directional.setFacing(BlockFace.EAST);
							break;
						case EAST:
							directional.setFacing(BlockFace.SOUTH);
							break;
						case SOUTH:
							directional.setFacing(BlockFace.WEST);
							break;
						case WEST:
							directional.setFacing(BlockFace.NORTH);
							break;
						default:
							break;
						}
					} else {
						switch (facing) {
						case NORTH:
							directional.setFacing(BlockFace.WEST);
							break;
						case WEST:
							directional.setFacing(BlockFace.SOUTH);
							break;
						case SOUTH:
							directional.setFacing(BlockFace.EAST);
							break;
						case EAST:
							directional.setFacing(BlockFace.NORTH);
							break;
						default:
							break;
						}
					}
				} else if (rotation.isRoll()) {
					if (rotation.isRight()) {
						switch (facing) {
						case EAST:
							directional.setFacing(BlockFace.DOWN);
							break;
						case DOWN:
							directional.setFacing(BlockFace.WEST);
							break;
						case WEST:
							directional.setFacing(BlockFace.UP);
							break;
						case UP:
							directional.setFacing(BlockFace.EAST);
							break;
						default:
							break;
						}
					} else {
						switch (facing) {
						case EAST:
							directional.setFacing(BlockFace.UP);
							break;
						case UP:
							directional.setFacing(BlockFace.WEST);
							break;
						case WEST:
							directional.setFacing(BlockFace.DOWN);
							break;
						case DOWN:
							directional.setFacing(BlockFace.EAST);
							break;
						default:
							break;
						}
					}
				} else if (rotation.isPitch()) {
					if (rotation.isRight()) {
						switch (facing) {
						case NORTH:
							directional.setFacing(BlockFace.DOWN);
							break;
						case DOWN:
							directional.setFacing(BlockFace.SOUTH);
							break;
						case SOUTH:
							directional.setFacing(BlockFace.UP);
							break;
						case UP:
							directional.setFacing(BlockFace.NORTH);
							break;
						default:
							break;
						}
					} else {
						switch (facing) {
						case NORTH:
							directional.setFacing(BlockFace.UP);
							break;
						case UP:
							directional.setFacing(BlockFace.SOUTH);
							break;
						case SOUTH:
							directional.setFacing(BlockFace.DOWN);
							break;
						case DOWN:
							directional.setFacing(BlockFace.NORTH);
							break;
						default:
							break;
						}
					}
				}
			} catch (IllegalArgumentException e) {}
		}
	}
	
}