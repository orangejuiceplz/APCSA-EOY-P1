package com.dl_labs.chatroom;

import java.util.Scanner;
import java.io.*;
import java.net.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import com.dl_labs.chatroom.server.ChatServer;
import com.dl_labs.chatroom.user_stuff.Message;
import com.dl_labs.chatroom.user_stuff.Person;
import com.dl_labs.chatroom.user_stuff.Message.MessageType;
import com.dl_labs.utilities.NetworkUtils;

public class App {
    private static final int DEFAULT_PORT = 12345;
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("welcome to DL labs chatroom");
        System.out.println("1. create a chatroom (become host)");
        System.out.println("2. join an existing chatroom");
        System.out.println("3. display network information");
        System.out.print("enter your choice (1, 2, or 3): ");
        
        int choice = scanner.nextInt();
        scanner.nextLine(); 
        
        if (choice == 1) {
            System.out.print("enter a name for your chatroom: ");
            String chatroomName = scanner.nextLine();
            
            System.out.print("enter port number (press Enter for default " + DEFAULT_PORT + "): ");
            String portInput = scanner.nextLine();
            int port = DEFAULT_PORT;
            
            if (!portInput.isEmpty()) {
                try {
                    port = Integer.parseInt(portInput);
                    if (!NetworkUtils.isValidPort(port)) {
                        System.out.println("invalid port number. using default port " + DEFAULT_PORT);
                        port = DEFAULT_PORT;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("invalid input. using default port " + DEFAULT_PORT);
                }
            }
            
            System.out.println("starting a chatroom server '" + chatroomName + "' on port " + port);
            createChatroom(chatroomName, port);
        } else if (choice == 2) {
            System.out.println("joining an existing chatroom");
            System.out.println("make sure the server is running before joining");
            
            System.out.print("enter the server IP address or hostname (press Enter for localhost): ");
            String serverAddress = scanner.nextLine();
            if (serverAddress.isEmpty()) {
                serverAddress = "localhost";
            } else if (!NetworkUtils.isValidIpAddress(serverAddress)) {
                String resolvedIP = NetworkUtils.getIPFromHostname(serverAddress);
                if (resolvedIP != null) {
                    System.out.println("resolved hostname " + serverAddress + " to " + resolvedIP);
                    serverAddress = resolvedIP;
                } else {
                    System.out.println("couldn't resolve hostname, trying anyway...");
                }
            }
            
            System.out.print("enter port number (press Enter for default " + DEFAULT_PORT + "): ");
            String portInput = scanner.nextLine();
            int port = DEFAULT_PORT;
            
            if (!portInput.isEmpty()) {
                try {
                    port = Integer.parseInt(portInput);
                    if (!NetworkUtils.isValidPort(port)) {
                        System.out.println("invalid port number. using default port " + DEFAULT_PORT);
                        port = DEFAULT_PORT;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("invalid input. using default port " + DEFAULT_PORT);
                }
            }
            
            System.out.println("testing connection to " + serverAddress + ":" + port + "...");
            if (!NetworkUtils.testConnection(serverAddress, port, 3000)) {
                System.out.println("warning: couldn't connect to server. server may be offline or unreachable.");
                System.out.print("try to connect anyway? (y/n): ");
                String answer = scanner.nextLine();
                if (!answer.equalsIgnoreCase("y")) {
                    System.out.println("exiting...");
                    scanner.close();
                    return;
                }
            } else {
                System.out.println("connection test successful!");
            }
            
            joinChatroom(serverAddress, port);
        } else if (choice == 3) {
            displayNetworkInfo();
            scanner.close();
        } else {
            System.out.println("invalid choice. exiting...");
        }
        
        scanner.close();
    }
    
    private static void displayNetworkInfo() {
        System.out.println("\n--- network information ---");
        System.out.println("hostname: " + NetworkUtils.getHostname());
        System.out.println("primary IP: " + NetworkUtils.getLocalIpAddress());
        
        System.out.println("\nall available IP addresses:");
        ArrayList<String> allIPs = NetworkUtils.getAllLocalIPs(false);
        for (String ip : allIPs) {
            System.out.println("  - " + ip);
        }
        
        System.out.println("\ndefault port: " + DEFAULT_PORT);
        boolean portAvailable = NetworkUtils.isPortAvailable(DEFAULT_PORT);
        System.out.println("default port available: " + (portAvailable ? "yes" : "no"));
        
        if (!portAvailable) {
            int nextAvailable = NetworkUtils.findAvailablePort(DEFAULT_PORT + 1);
            if (nextAvailable != -1) {
                System.out.println("next available port: " + nextAvailable);
            } else {
                System.out.println("no available ports found");
            }
        }
        
        System.out.println("\npress Enter to return to main menu...");
        new Scanner(System.in).nextLine();
    }
    
    private static void createChatroom(String chatroomName, int port) {
        if (!NetworkUtils.isPortAvailable(port)) {
            System.out.println("port " + port + " is already in use");
            int newPort = NetworkUtils.findAvailablePort(port + 1);
            if (newPort == -1) {
                System.out.println("no available ports found. exiting...");
                return;
            }
            System.out.println("using port " + newPort + " instead");
            port = newPort;
        }
        
        System.out.println("\nserver information:");
        System.out.println("hostname: " + NetworkUtils.getHostname());
        System.out.println("IP addresses clients can use to connect:");
        for (String ip : NetworkUtils.getAllLocalIPs(false)) {
            System.out.println("  - " + ip);
        }
        System.out.println("port: " + port);
        
        final int finalPort = port;
        new Thread(() -> {
            ChatServer server = new ChatServer(finalPort, chatroomName);
            server.tryStart();
        }).start();
        
        try {
            Thread.sleep(1000); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        joinChatroom("localhost", finalPort);
    }
    
    private static void joinChatroom(String serverIP, int port) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("enter your username: ");
            String username = scanner.nextLine();
            
            System.out.println("connecting to " + serverIP + ":" + port + "...");
            Socket socket = new Socket(serverIP, port);
            
            String serverName = NetworkUtils.getHostnameFromIP(serverIP);
            if (serverName != null && !serverName.equals(serverIP) && !serverIP.equals("localhost")) {
                System.out.println("connected to " + serverName + " (" + serverIP + ") on port " + port + "!");
            } else {
                System.out.println("connected to the server!");
            }
            
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            output.println(username);
            
            Person user = new Person(username, false);
            new Thread(() -> {
                try {
                    String receivedText;
                    while ((receivedText = input.readLine()) != null) {
                        System.out.println(receivedText);
                    }
                } catch (IOException e) {
                    System.out.println("disconnected from the server.");
                }
            }).start();
            
            System.out.println("the commands:");
            System.out.println("- type your message and press 'Enter' to send");
            System.out.println("- start with /p [username] to send a private message");
            System.out.println("- type /help for commands");
            System.out.println("- type 'exit' to leave the chat");
            
            String userInput;
            while (scanner.hasNextLine()) {
                userInput = scanner.nextLine();
                
                if (userInput.equalsIgnoreCase("exit")) {
                    break;
                }
                
                Message message;
                
                if (userInput.startsWith("/p ")) {
                    int spaceIndex = userInput.indexOf(' ', 3);
                    if (spaceIndex > 0) {
                        String recipient = userInput.substring(3, spaceIndex);
                        String content = userInput.substring(spaceIndex + 1);
                        message = new Message("@" + recipient + " " + content, user, MessageType.PRIVATE);
                        System.out.println("[" + message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + 
                                          "] [Private message to " + recipient + "]: " + content);
                    } else {
                        System.out.println("bad private message format. use: /p username message");
                        continue;
                    }
                } else if (userInput.startsWith("/")) {
                    message = new Message(userInput, user, MessageType.COMMAND);
                } else {
                    message = new Message(userInput, user);
                    System.out.println(message.format());
                }
                
                output.println(message.getContent());
            }
            
            socket.close();
            System.out.println("disconnected from chat server.");
            scanner.close();
            
        } catch (UnknownHostException e) {
            System.out.println("couldn't find the server: " + e.getMessage());
        } catch (ConnectException e) {
            System.out.println("connection refused. the server may be offline or not running on this port.");
        } catch (IOException e) {
            System.out.println("error connecting to server: " + e.getMessage());
        }
    }
}