package biz.wolter.minecraft.bukkit.rest;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.entity.Player.Spigot;
import org.bukkit.material.Attachable;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;
import org.bukkit.material.PressurePlate;
import org.bukkit.material.Redstone;
import org.bukkit.material.RedstoneWire;
import org.bukkit.scheduler.BukkitScheduler;

import com.google.gson.Gson;

import biz.wolter.minecraft.bukkit.SmartHome;
import biz.wolter.minecraft.bukkit.thing.Thing;
import biz.wolter.minecraft.bukkit.thing.ThingCommand;
import biz.wolter.minecraft.bukkit.thing.ThingComponentType;
import biz.wolter.minecraft.bukkit.thing.ThingList;
import biz.wolter.minecraft.bukkit.thing.ThingLocation;

@Path("rest/")
public class RestResource {

	private final Logger logger = SmartHome.getPlugin(SmartHome.class).getLogger();
	private final SmartHome smartHome = SmartHome.getPlugin(SmartHome.class);
	
	@GET
	@Path("hello/")
	@Produces(MediaType.TEXT_PLAIN)
	public String helloWorld() {
		return "Hello, world!";
	}

	@GET
	@Path("things/")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDevices() {
		Gson gson = new Gson();
		String response = gson.toJson(smartHome.getThingList());
		return response;
	}

	@GET
	@Path("things/{x}/{y}/{z}/")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDevice(@PathParam("x") int x, @PathParam("y") int y, @PathParam("z") int z) {
		Gson gson = new Gson();
		ThingLocation location = new ThingLocation(x,  y, z);
		Thing thing = smartHome.getThingList().findThingByLocation(location);

    	if (thing == null) {
    		throw new WebApplicationException("Unknown or unsupported block at " + location.toString() + ".");
    	}
		return gson.toJson(thing);
	}
	
	@POST
	@Path("commands/execute/")
	@Produces(MediaType.APPLICATION_JSON)	
	public void executeCommand(final String body, @Suspended final AsyncResponse asyncResponse) {
		
		asyncResponse.setTimeout(10, TimeUnit.SECONDS);
		
    	Gson gson = new Gson();
    	ThingCommand command = gson.fromJson(body, ThingCommand.class);
    	
    	World world = Bukkit.getWorld("world"); // from server.properties level-name
    	
    	Location location = new Location(world, command.location.x, command.location.y, command.location.z);
		Block block = location.getBlock();
		BlockState blockState = block.getState();
		
       	ThingList thingList = smartHome.getThingList();
    	final Thing thing = thingList.findThingByLocation(command.location);    		
    	// Ignore unknown things
    	if (thing == null) {
    		throw new WebApplicationException("Unknown or unsupported block " + blockState.getType().name() +".");
    	}
		
    	// Beware of Threading and Issue https://bukkit.atlassian.net/browse/BUKKIT-1858
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		boolean switched = (boolean) command.component.state;
		
    	switch (command.component.type) {    	
    		case POWERED:
    			
		        scheduler.scheduleSyncDelayedTask(SmartHome.getPlugin(SmartHome.class), new Runnable() {
		            @Override
		            public void run() {
		            	
		            	if (blockState.getData() instanceof Lever) {    				
    	    				Lever lever = (Lever) blockState.getData();
    	    				lever.setPowered(switched);    	    				
    	    				blockState.setData(lever);
		            	} else {
		            		Button button = (Button) blockState.getData();
		            		button.setPowered(switched);
		            		blockState.setData(button);
		            	}
	            		boolean success = blockState.update(true, true);
	            		if (success) {
	            			ThingCommand updateCommand;
	        		        updateCommand = new ThingCommand();
	                		updateCommand.location = thing.location;
	                		updateCommand.component = thing.getComponentByType(ThingComponentType.POWERED);
	                		updateCommand.component.state = switched;						
	                		EventsBroadcasterResource.broadcastMessage(MessageType.UPDATE_THING, gson.toJson(updateCommand));
	            			asyncResponse.resume(Response.ok().build());
	            		} else {
	            			asyncResponse.resume(Response.serverError().build());
	            		}
		            }
		        });

    			break;
    			
    		case OPEN:
    			
		        scheduler.scheduleSyncDelayedTask(SmartHome.getPlugin(SmartHome.class), new Runnable() {
		            @Override
		            public void run() {    			
		    			((Openable) blockState.getData()).setOpen((Boolean) command.component.state); // Open or close the door    				 
		    		    blockState.setData(blockState.getData()); // Add the data to the BlockState
	            		boolean success = blockState.update(true, true); //Update the BlockState, the door will now open
	            		if (success) {
	            			ThingCommand updateCommand;
	        		        updateCommand = new ThingCommand();
	                		updateCommand.location = thing.location;
	                		updateCommand.component = thing.getComponentByType(ThingComponentType.OPEN);
	                		updateCommand.component.state = switched;						
	                		EventsBroadcasterResource.broadcastMessage(MessageType.UPDATE_THING, gson.toJson(updateCommand));
	            			asyncResponse.resume(Response.ok().build());
	            		} else {
	            			asyncResponse.resume(Response.serverError().build());
	            		}		    			
		            }
		        });
		        
    			break;
    			
    		default:
    			logger.warning("Unknown or unsupported component type " + command.component.type +".");
    			asyncResponse.resume(Response.serverError().build());
    	}
	}
}