package org.bukkitplugin.pistonsoverhaul.movingblock;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;

public abstract class MovingBlock {
	
	protected final FallingBlock fallingBlock;
	protected final Block from;
	protected final Block to;
	
    public MovingBlock(FallingBlock fallingBlock, Block from, Block to) {
    	this.fallingBlock = fallingBlock;
    	this.from = from;
    	this.to = to;
    }
    
    public FallingBlock getFallingBlock() {
    	return fallingBlock;
    }
    
    public Block getFrom() {
    	return from;
    }
    
    public Block getTo() {
    	return to;
    }
	
	public abstract Location getTarget(int tick);
	
}