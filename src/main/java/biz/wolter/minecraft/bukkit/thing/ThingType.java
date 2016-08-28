/**
 * 
 */
package biz.wolter.minecraft.bukkit.thing;

/**
 * @author sw
 *
 */
public enum ThingType {
	DOOR("DOOR"),
	WEATHER_SENSOR("WEATHER_SENSOR"),
	SWITCH("SWITCH"),
	BUTTON("BUTTON"),
	PRESSURE_SENSOR("PRESSURE_SENSOR"),
	LAMP("LAMP"),
	TRIPWIRE("TRIPWIRE")
	;
	
	private final String text;
    /**
     * @param text
     */
    private ThingType(final String text) {
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