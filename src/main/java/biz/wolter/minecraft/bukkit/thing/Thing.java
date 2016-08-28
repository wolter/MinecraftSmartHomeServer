package biz.wolter.minecraft.bukkit.thing;

import java.util.ArrayList;

public class Thing {    	
	
	// Location basically is the unique identifier of a device, because there could only be one at a time at each location
	public ThingLocation location;
	// Type describing the thing behavior
	public ThingType type;
	// Material holds the Minecraft material and is more detailed than the type 
	public String material;
	// Components are the possible channels to read (and in some cases to write)
	public ArrayList<ThingComponent> components;
	
	/*
	 * Compare two devices based on the location, because there could only be one at a time at each location
	 */
	public boolean equals(Thing location) {
		return this.location.equals(location); 
	}
	
	/*
	 * Retrieve ThingComponent by ThingComponentType
	 */
	public ThingComponent getComponentByType(ThingComponentType type) {
		ThingComponent component = null;
		for (ThingComponent c : components) {
			if (c.type.equals(type)) {
				component = c;
				break;
			}
		}
		return component;
	}
}