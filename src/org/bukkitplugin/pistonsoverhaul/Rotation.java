package org.bukkitplugin.pistonsoverhaul;


import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public enum Rotation {
	
	LYAW, RYAW, LROLL, RROLL, LPITCH, RPITCH;
	
	public boolean isRight() {
		return this == RYAW || this == RROLL || this == RPITCH;
	}
	
	public boolean isYaw() {
		return this == RYAW || this == LYAW;
	}
	
	public boolean isRoll() {
		return this == RROLL || this == LROLL;
	}
	
	public boolean isPitch() {
		return this == RPITCH || this == LPITCH;
	}
	
	public Rotation getOpposite() {
		if (this == RYAW) return LYAW;
		if (this == RROLL) return LROLL;
		if (this == RPITCH) return LPITCH;
		return null;
	}
	
	public double getDistance(Block axis, Location location) {
		Location loc = location.clone();
		if (this.isYaw()) loc.setY(axis.getY());
		else if (this.isRoll()) loc.setZ(axis.getZ());
		else if (this.isPitch()) loc.setX(axis.getX());
		return axis.getLocation().distance(loc);
	}
	
	public Vector getHeight(Block axis, Location locaction) {
		if (this.isYaw()) return new Vector(0d, locaction.getY() - axis.getY(), 0d);
		else if (this.isRoll()) return new Vector(0d, 0d, locaction.getZ() - axis.getZ());
		else if (this.isPitch()) return new Vector(locaction.getX() - axis.getX(), 0d, 0d);
		else return new Vector();
	}
	
	private double getArc(double x, double y) {
		if (x > 1) x = 1;
		else if (x < -1) x = -1;
		return Math.acos(x) * (y < 0 ? -1 : 1);
	}
	
	public double getArc(Vector vec) {
		if (isYaw()) return getArc(vec.getX(), vec.getZ());
		else if (isRoll()) return getArc(vec.getY(), vec.getX());
		else if (isPitch()) return getArc(vec.getZ(), vec.getY());
		throw new NullPointerException();
	}
	
	public double getModX(double rad) {
		if (isYaw()) return Math.cos(rad);
		else if (isRoll()) return Math.sin(rad);
		else return 0;
	}
	
	public double getModY(double rad) {
		if (isRoll()) return Math.cos(rad);
		else if (isPitch()) return Math.sin(rad);
		else return 0;
	}
	
	public double getModZ(double rad) {
		if (isYaw()) return Math.sin(rad);
		else if (isPitch()) return Math.cos(rad);
		else return 0;
	}
	
	public static Rotation getRotation(BlockFace blockFace, boolean right) {
		if (blockFace == BlockFace.NORTH) {
			if (right) return RROLL;
			else return LROLL;
		} else if (blockFace == BlockFace.SOUTH) {
			if (right) return LROLL;
			else return RROLL;
		} else if (blockFace == BlockFace.EAST) {
			if (right) return RPITCH;
			else return LPITCH;
		} else if (blockFace == BlockFace.WEST) {
			if (right) return LPITCH;
			else return RPITCH;
		} else if (blockFace == BlockFace.UP) {
			if (right) return RYAW;
			else return LYAW;
		} else if (blockFace == BlockFace.DOWN) {
			if (right) return LYAW;
			else return RYAW;
		} else return null;
	}
	
}