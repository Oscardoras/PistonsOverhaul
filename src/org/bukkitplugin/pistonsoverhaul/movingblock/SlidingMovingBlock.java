package org.bukkitplugin.pistonsoverhaul.movingblock;


import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;

public class SlidingMovingBlock extends MovingBlock {
	
    protected final BlockFace direction;
	
	public SlidingMovingBlock(FallingBlock fallingBlock, Block from, BlockFace direction) {
    	super(fallingBlock, from, from.getRelative(direction));
    	this.direction = direction;
    }

	@Override
	public Location getTarget(int tick) {
		return from.getLocation().add(direction.getModX() * (tick / 20d), direction.getModY() * (tick / 20d), direction.getModZ() * (tick / 20d));
	}
    
}