package biz.wolter.minecraft.bukkit.net;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * {@link NetworkInterfaceChecker} implements a sophisticated method to find out a list of all
 * {@link InetAddress}es of connected network interfaces.
 */
public class NetworkInterfaceChecker {

    private final Logger logger;

    public NetworkInterfaceChecker(Logger logger) {
        this.logger = logger;
    }

    /**
     * Check all network adapters for usable addresses.
     *
     * @return list of all {@link InetAddress}es of network interfaces, that are usable for UPnP
     * @throws DiscoveryException for any error, that occurs
     */
    public List<InetAddress> getAddresses() {
        return discoverBindAddresses(discoverNetworkInterfaces());
    }

    private List<NetworkInterface> discoverNetworkInterfaces() {
        List<NetworkInterface> networkInterfaces = new ArrayList<NetworkInterface>();
        try {
            Enumeration<NetworkInterface> interfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface iface : Collections.list(interfaceEnumeration)) {
                if (isUsableNetworkInterface(iface)) {
                    logger.fine("Discovered usable network interface: " + iface.getDisplayName());
                    synchronized (networkInterfaces) {
                        networkInterfaces.add(iface);
                    }
                }
            }
        } catch (Exception ex) {
            logger.warning("Could not not analyze local network interfaces due to " + ex.getMessage());
        }

        return networkInterfaces;
    }

    private List<InetAddress> discoverBindAddresses(List<NetworkInterface> networkInterfaces) {
        List<InetAddress> bindAddresses = new ArrayList<InetAddress>();
        try {
            Iterator<NetworkInterface> it = networkInterfaces.iterator();
            while (it.hasNext()) {
                NetworkInterface networkInterface = it.next();

                int usableAddresses = 0;
                for (InetAddress inetAddress : getInetAddresses(networkInterface)) {
                    if (inetAddress == null) {
                        continue;
                    }

                    if (isUsableAddress(networkInterface, inetAddress)) {
                        logger.fine("Discovered usable network interface address: " + inetAddress.getHostAddress());
                        usableAddresses++;
                        synchronized (bindAddresses) {
                            bindAddresses.add(inetAddress);
                        }
                    }
                }

                if (usableAddresses == 0) {
                    it.remove();
                }
            }
        } catch (Exception ex) {
            logger.warning("Could not not analyze local network interfaces due to " + ex.getMessage());
        }

        return bindAddresses;
    }

    private List<InetAddress> getInetAddresses(NetworkInterface networkInterface) {
        return Collections.list(networkInterface.getInetAddresses());
    }

    /**
     * Validation of every discovered network interface.
     * <p>
     * The given implementation ignores interfaces which are
     * </p>
     * <ul>
     * <li>loopback (yes, we do not bind to lo0)</li>
     * <li>down</li>
     * <li>have no bound IP addresses</li>
     * <li>named "vmnet*" (OS X VMWare does not properly stop interfaces when it quits)</li>
     * <li>named "vnic*" (OS X Parallels interfaces should be ignored as well)</li>
     * <li>named "*virtual*" (VirtualBox interfaces, for example</li>
     * <li>named "ppp*"</li>
     * </ul>
     *
     * @param iface The interface to validate.
     * @return True if the given interface matches all validation criteria.
     * @throws Exception If any validation test failed with an un-recoverable error.
     */
    private boolean isUsableNetworkInterface(NetworkInterface iface) throws Exception {
    	
        if (!iface.isUp()) {
            return false;
        }

        if (getInetAddresses(iface).size() == 0) {
            return false;
        }

        if (iface.getName().toLowerCase(Locale.ENGLISH).startsWith("vmnet") || (iface.getDisplayName() != null
                && iface.getDisplayName().toLowerCase(Locale.ENGLISH).contains("vmnet"))) {
            return false;
        }

        if (iface.getName().toLowerCase(Locale.ENGLISH).startsWith("vnic")) {
            return false;
        }

        if (iface.getName().toLowerCase(Locale.ENGLISH).contains("virtual")) {
            return false;
        }

        if (iface.getName().toLowerCase(Locale.ENGLISH).startsWith("ppp")) {
            return false;
        }

        if (iface.getName().toLowerCase(Locale.ENGLISH).startsWith("utun")) {
            return false;
        }

        if (iface.getName().toLowerCase(Locale.ENGLISH).startsWith("veth")) {
            return false;
        }

        if (iface.getName().toLowerCase(Locale.ENGLISH).startsWith("docker")) {
            return false;
        }

        if (iface.getDisplayName().toLowerCase(Locale.ENGLISH).startsWith("hyper-v")) {
            return false;
        }        
        
        if (iface.isLoopback()) {
            return false;
        }

        return true;
    }

    private boolean isUsableAddress(NetworkInterface networkInterface, InetAddress address) throws IOException {
        return (address instanceof Inet4Address && !address.isLoopbackAddress());
    }

}
