package biz.wolter.minecraft.bukkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.google.gson.Gson;

import biz.wolter.minecraft.bukkit.command.SendCommand;
import biz.wolter.minecraft.bukkit.event.BlockChangeListener;
import biz.wolter.minecraft.bukkit.rest.EventsBroadcasterResource;
import biz.wolter.minecraft.bukkit.rest.RestResource;
import biz.wolter.minecraft.bukkit.thing.ThingList;

public class SmartHome extends JavaPlugin {
	
	public static final int DEFAULT_PORT = 10692;
	
	private final Logger logger = getLogger();
	private final PluginManager pluginManager = Bukkit.getPluginManager();
	
    // REST Server
	private HttpServer server;
	// JmDNS
	private JmDNS jmdns;
	
    // Fired when plugin is first enabled
    @Override
    public void onEnable() {
    	logger.info("Plugin enabled.");
    	
    	// Make default config from within resources available
    	saveDefaultConfig();
    	
    	// Retrieve config values
    	int port = getConfig().getInt("port", DEFAULT_PORT);
    	
    	// URI baseUri = UriBuilder.fromUri("http://localhost/").port(port).build();
    	URI baseUri = UriBuilder.fromUri("http://0.0.0.0").port(port).build();
    	ResourceConfig config = new ResourceConfig(RestResource.class, EventsBroadcasterResource.class, SseFeature.class);    	
    	server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config, true);

    	// Each plugin has its own "data folder"
    	setThingList(ThingList.load(new File(getDataFolder(), "devices.json")));
    	
    	// Register event listener
    	pluginManager.registerEvents(new BlockChangeListener(), this);
    	
    	// Register command (set an instance of command class as executor)
    	this.getCommand("sendCommand").setExecutor(new SendCommand());   	
    	
		try {
            // Create a JmDNS instance
            jmdns = JmDNS.create(InetAddress.getLocalHost());
            
            String serviceType = "_minecraft-server._tcp.local.";
            String serviceName = "smarthome";
            String serviceDescription = "Minecraft Smart Home";
            Hashtable<String, String> serviceProperties = new Hashtable<String, String>();
            serviceProperties.put("uri", "/rest");
            
            // Register a service
            // ServiceInfo serviceInfo = ServiceInfo.create(serviceType, serviceName, port, serviceDescription);
            ServiceInfo serviceInfo = ServiceInfo.create(serviceType, serviceName, port, 0,0, serviceProperties);

            jmdns.registerService(serviceInfo);
		} catch (IOException e) {
			logger.warning(e.getMessage());
		}		
		
    }
    // Fired when plugin is disabled
    @Override
    public void onDisable() {
    	logger.info("Plugin disabled.");
    	
    	// Store changes
    	if (!thingList.isSavedImmediately()) {
    		getThingList().save();
    	}
    	
    	// Unregister all services
        jmdns.unregisterAllServices();
    	server.shutdownNow();
    }
    
	private ThingList thingList;    
    
	/**
	 * @return the thingList
	 */
	public ThingList getThingList() {
		return thingList;
	}
	/**
	 * @param thingList the thingList to set
	 */
	private void setThingList(ThingList thingList) {
		this.thingList = thingList;
	}
    
}
