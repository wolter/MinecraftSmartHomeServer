package biz.wolter.minecraft.bukkit.rest;

public enum MessageType {

	// ADD and REMOVE are associated with Thing
	ADD_THING("ADD_THING"),
	REMOVE_THING("REMOVE_THING"),
	// UPDATE and PLAYER are associated with ThingCommand
	UPDATE_THING("UPDATE_THING"),
	PLAYER_JOINED("PLAYER_JOINED"),
	PLAYER_QUITTED("PLAYER_QUITTED")
	;

	private final String text;
    /**
     * @param text
     */
    private MessageType(final String text) {
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
