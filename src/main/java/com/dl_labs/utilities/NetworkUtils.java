package com.dl_labs.utilities;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class NetworkUtils {
    
    // this was all AI
    // this is just a regex to check if the string is a valid IPv4 address
    // it checks if the string is in the format of 0-255.0-255.0-255.0-255
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
            public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "don't know";
        }
    }
    
    public static String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    // lists ALL of the local IP addresses
    public static ArrayList<String> getAllLocalIPs(boolean includeLoopback) {
        ArrayList<String> allAddresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces(); // wtf is this
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isUp() && (!networkInterface.isLoopback() || includeLoopback)) {
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (inetAddress instanceof Inet4Address) {
                            allAddresses.add(inetAddress.getHostAddress());
                        }
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Error getting network interfaces: " + e.getMessage());
        }
        return allAddresses;
    }
    

    public static boolean isPortAvailable(int port) {
        if (port < 0 || port > 65535) {
            return false;
        }
        
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public static int findAvailablePort(int startPort) {
        for (int port = startPort; port <= 65535; port++) {
            if (isPortAvailable(port)) {
                return port;
            }
        }
        return -1;
    }
    
    public static boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return IP_PATTERN.matcher(ip).matches() || ip.equals("localhost");
    }
    
        public static boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }
    
    // ping a host
    public static boolean testConnection(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public static String getHostnameFromIP(String ipAddress) {
        try {
            InetAddress addr = InetAddress.getByName(ipAddress);
            return addr.getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }
    
    public static String getIPFromHostname(String hostname) {
        try {
            InetAddress addr = InetAddress.getByName(hostname);
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

}
