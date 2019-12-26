package org.bukkitplugin.pistonsoverhaul.movingblock;


import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;
import org.bukkitplugin.pistonsoverhaul.Rotation;

public class TurningMovingBlock extends MovingBlock {
	
	protected final Rotation rotation;
	protected final Block axis;
	protected final double distance;
	protected final Vector height;
	protected final double arc;
	protected final double a;
	
    public TurningMovingBlock(FallingBlock fallingBlock, Block from, Rotation rotation, Block axis, double time) {
    	super(fallingBlock, from, getTo(from, rotation, axis));
    	this.rotation = rotation;
    	this.axis = axis;
    	Location fromLocation = from.getLocation();
    	this.distance = rotation.getDistance(axis, fromLocation);
    	this.height = rotation.getHeight(axis, fromLocation);
    	Vector vec = from.getLocation().subtract(axis.getLocation()).toVector().multiply(1d/distance);
    	this.arc = distance >= 0.5d ? rotation.getArc(vec) : 0d;
    	this.a = (rotation.isRight() ? 1 : -1) * (Math.PI/2)/time;
    }
    
    @Override
	public Location getTarget(int tick) {
		return axis.getLocation()
				.add(new Vector(rotation.getModX(arc + a*tick), rotation.getModY(arc + a*tick), rotation.getModZ(arc + a*tick)).multiply(distance))
				.add(height);
	}
    
    protected static Block getTo(Block from, Rotation rotation, Block axis) {
    	Location fromLocation = from.getLocation();
    	double distance = rotation.getDistance(axis, fromLocation);
    	if (distance < 0.5d) return from;
    	Vector vec = from.getLocation().subtract(axis.getLocation()).toVector().multiply(1d/distance);
    	double arc = rotation.getArc(vec);
    	arc += (rotation.isRight() ? 1 : -1) * Math.PI/2;
    	return axis.getLocation()
    			.add(new Vector(rotation.getModX(arc), rotation.getModY(arc), rotation.getModZ(arc)).multiply(distance))
    			.add(rotation.getHeight(axis, fromLocation))
    			.getBlock();
    }
    
}