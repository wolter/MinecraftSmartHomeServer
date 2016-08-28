package biz.wolter.minecraft.bukkit.rest;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.media.sse.SseFeature;

@Singleton
@Path("rest/events")
public class EventsBroadcasterResource {

	private static SseBroadcaster broadcaster = new SseBroadcaster();	
	
    public static void broadcastMessage(String payload) {
    	broadcastMessage("message", payload, MediaType.TEXT_PLAIN_TYPE);
    }

    public static void broadcastMessage(MessageType messageType, String payload) {
    	broadcastMessage(messageType.toString(), payload, MediaType.TEXT_PLAIN_TYPE);
    }
    
    public static void broadcastMessage(String eventName, String payload, MediaType type) {
        OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
        OutboundEvent event = eventBuilder.name(eventName)
            .mediaType(type)
            .data(String.class, payload)
            .build();
        broadcaster.broadcast(event);
    }
 
    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput listenToBroadcast() {
        final EventOutput eventOutput = new EventOutput();
        this.broadcaster.add(eventOutput);
        return eventOutput;
    }	
	
}
