package biz.wolter.minecraft.bukkit;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.ws.rs.core.UriBuilder;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;

import biz.wolter.minecraft.bukkit.command.SendCommand;
import biz.wolter.minecraft.bukkit.event.BlockChangeListener;
import biz.wolter.minecraft.bukkit.net.NetworkInterfaceChecker;
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
        ResourceConfig config = new ResourceConfig(RestResource.class, EventsBroadcasterResource.class,
                SseFeature.class);
        server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config, true);

        // Each plugin has its own "data folder"
        setThingList(ThingList.load(new File(getDataFolder(), "devices.json")));

        // Register event listener
        pluginManager.registerEvents(new BlockChangeListener(), this);

        // Register command (set an instance of command class as executor)
        this.getCommand("sendCommand").setExecutor(new SendCommand());

        try {

            // InetAddress addr = InetAddress.getLocalHost();
            // Hack needed to get the real adapter and it's address
            // See http://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
            // --> not working on Mac!
            // InetAddress addr;
            // try (final DatagramSocket socket = new DatagramSocket()) {
            // // IP and port are just placeholder to create a connection for the preferred adapter...
            // socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            // // ...which helps us to discover the "real" address
            // addr = socket.getLocalAddress();
            // }

            NetworkInterfaceChecker networkChecker = new NetworkInterfaceChecker(logger);
            List<InetAddress> addresses = networkChecker.getAddresses();

            if (addresses.isEmpty()) {
                logger.warning("No usable IP address found!");
                return;
            }

            // Getting IP address of localhost - getHostAddress returns IP Address in textual format
            logger.info("IP is " + addresses.get(0));

            // Create a JmDNS instance
            jmdns = JmDNS.create(addresses.get(0));

            String serviceType = "_minecraft-server._tcp.local.";
            String serviceName = "smarthome";
            String serviceDescription = "Minecraft Smart Home";
            Hashtable<String, String> serviceProperties = new Hashtable<String, String>();
            serviceProperties.put("uri", "/rest");

            // Register a service
            // ServiceInfo serviceInfo = ServiceInfo.create(serviceType, serviceName, port, serviceDescription);
            ServiceInfo serviceInfo = ServiceInfo.create(serviceType, serviceName, port, 0, 0, serviceProperties);

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
        if (jmdns != null) jmdns.unregisterAllServices();
        
        // Shutdown REST and SSE server
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
