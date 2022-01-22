package me.oscardoras.pistonsoverhaul;

public class Counter {
	
	public int id = 0;
	public boolean cancel = false;
	public boolean rotated = false;
	public int tick = 0;
	
	public void up() {
    	tick++;
    }
    
    public void down() {
    	tick--;
    }
    
}
