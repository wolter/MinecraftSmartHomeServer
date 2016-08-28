/**
 * 
 */
package biz.wolter.minecraft.bukkit.thing;

/**
 * @author sw
 *
 */
public enum ThingComponentType {
	// quirk for sensors adding humidity - please note, this is read only
	HUMIDITY("HUMIDITY"),
	// quirk for sensors adding light  - please note, this is read only
	LIGHT("LIGHT"),
	// doors can be (isOpen) open (true) or close (false) - - please note, some blocks are read only
	OPEN("OPEN"),
	// redstones can be (isPowerd) on (true) or off (false) - please note, some blocks are read only
	POWERED("POWERED"),
	// daylight sensors have power between 0 and 15 - please note, this is read only
	POWER("POWER"),
	// pressure sensor can be (isPressed) pressed (true) or released (false) - please note, this is read only  
	PRESSED("PRESSED"),
	// quirk for sensors adding temperature - please note, this is read only  
	TEMPERATURE("TEMPERATURE"),
	// quirk for player
	PLAYER("PLAYER");
	
	private final String text;
    /**
     * @param text
     */
    private ThingComponentType(final String text) {
        this.text = text;
    }
	
    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }  	
	
 
}