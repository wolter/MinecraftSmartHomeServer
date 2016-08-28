package biz.wolter.minecraft.bukkit.thing;

import org.bukkit.Location;

public class ThingLocation {
	
	public int x;
	public int y;
	public int z;
	
	public ThingLocation() {		
	}

	public ThingLocation(Location location) {
		this.x = location.getBlockX();
		this.y = location.getBlockY();
		this.z = location.getBlockZ();
	}

	public ThingLocation(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/*
	 * Compares two locations based on their coordinates x, y, and z
	 */
	public boolean equals(ThingLocation location) {
		return (x == location.x && y == location.y  && z == location.z); 
	}
	
	@Override
	public String toString() {
		return "("+x+","+y+","+z+")";
	}
}