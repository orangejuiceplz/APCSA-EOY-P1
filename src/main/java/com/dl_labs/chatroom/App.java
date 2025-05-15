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
import com.dl_labs.utilities.ConsoleUtils;
import com.dl_labs.utilities.NetworkUtils;

public class App {
    private static final int DEFAULT_PORT = 12345;
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        ConsoleUtils.clearScreen();
        ConsoleUtils.printHeading("DL LABS CHATROOM");
        
        ArrayList<String> options = new ArrayList<>();
        options.add("create a chatroom (become host)");
        options.add("join an existing chatroom");
        options.add("display network information");
        options.add("exit");
        
        int choice = ConsoleUtils.showMenu("main menu", options);
        
        switch(choice) {
            case 1:
                String chatroomName = ConsoleUtils.promptForString("enter a name for your chatroom: ");
                
                int port = ConsoleUtils.promptForInt("enter port number (press Enter for default " + DEFAULT_PORT + "): ", 
                                                  1024, 65535);
                
                if (port == 1024) { 
                    port = DEFAULT_PORT;
                }
                
                ConsoleUtils.displayMessage("starting a chatroom server '" + chatroomName + "' on port " + port, 
                                         ConsoleUtils.MessageType.INFO);
                
                createChatroom(chatroomName, port);
                break;
                
            case 2:
                ConsoleUtils.clearScreen();
                ConsoleUtils.printHeading("JOIN A CHATROOM");
                
                ConsoleUtils.displayMessage("make sure the server is running before joining", 
                                         ConsoleUtils.MessageType.WARNING);
                
                String serverAddress = ConsoleUtils.promptForInput(
                    "enter the server IP address or hostname (press Enter for localhost): ",
                    input -> input.isEmpty() || NetworkUtils.isValidIpAddress(input) || NetworkUtils.getIPFromHostname(input) != null,
                    "Invalid IP address or hostname. Please try again."
                );
                
                if (serverAddress.isEmpty()) {
                    serverAddress = "localhost";
                } else if (!NetworkUtils.isValidIpAddress(serverAddress)) {
                    String resolvedIP = NetworkUtils.getIPFromHostname(serverAddress);
                    if (resolvedIP != null) {
                        ConsoleUtils.displayMessage("resolved hostname " + serverAddress + " to " + resolvedIP, 
                                                 ConsoleUtils.MessageType.INFO);
                        serverAddress = resolvedIP;
                    } else {
                        ConsoleUtils.displayMessage("couldn't resolve hostname, trying anyway...", 
                                                 ConsoleUtils.MessageType.WARNING);
                    }
                }
                
                port = ConsoleUtils.promptForInt("enter port number (press Enter for default " + DEFAULT_PORT + "): ", 
                                              1024, 65535);
                
                if (port == 1024) { 
                    port = DEFAULT_PORT;
                }
                
                ConsoleUtils.displayMessage("testing connection to " + serverAddress + ":" + port + "...", 
                                         ConsoleUtils.MessageType.SYSTEM);
                
                ConsoleUtils.showProgressAnimation("Testing connection", 1500);
                
                if (!NetworkUtils.testConnection(serverAddress, port, 3000)) {
                    ConsoleUtils.displayMessage("couldn't connect to server. server may be offline or unreachable.", 
                                            ConsoleUtils.MessageType.WARNING);
                    
                    if (!ConsoleUtils.confirm("try to connect anyway?")) {
                        ConsoleUtils.displayMessage("exiting...", ConsoleUtils.MessageType.INFO);
                        return;
                    }
                } else {
                    ConsoleUtils.displayMessage("connection test successful!", 
                                             ConsoleUtils.MessageType.SUCCESS);
                }
                
                joinChatroom(serverAddress, port);
                break;
                
            case 3:
                displayNetworkInfo();
                break;
                
            case 4:
                ConsoleUtils.displayMessage("exiting application. goodbye!", 
                                         ConsoleUtils.MessageType.INFO);
                break;
                
            default:
                ConsoleUtils.displayMessage("invalid choice. exiting.", 
                                         ConsoleUtils.MessageType.ERROR);
        }
        
        scanner.close();
    }
    
    private static void displayNetworkInfo() {
        ConsoleUtils.clearScreen();
        ConsoleUtils.printHeading("NETWORK INFORMATION");
        
        ConsoleUtils.displayMessage("HOSTNAME: " + NetworkUtils.getHostname(), 
                                 ConsoleUtils.MessageType.INFO);
        ConsoleUtils.displayMessage("PRIMARY IP: " + NetworkUtils.getLocalIpAddress(), 
                                 ConsoleUtils.MessageType.INFO);
        
        ArrayList<String> allIPs = NetworkUtils.getAllLocalIPs(false);
        String[] headers = {"index", "IP Address"};
        ArrayList<String[]> ipData = new ArrayList<>();
        
        for (int i = 0; i < allIPs.size(); i++) {
            ipData.add(new String[]{String.valueOf(i+1), allIPs.get(i)});
        }
        
        ConsoleUtils.printHeading("all available IP addresses");
        ConsoleUtils.printTable(headers, ipData);
        
        ConsoleUtils.printHeading("port information");
        boolean portAvailable = NetworkUtils.isPortAvailable(DEFAULT_PORT);
        
        if (portAvailable) {
            ConsoleUtils.displayMessage("default port " + DEFAULT_PORT + " is available", 
                                     ConsoleUtils.MessageType.SUCCESS);
        } else {
            ConsoleUtils.displayMessage("default port " + DEFAULT_PORT + " is NOT available", 
                                     ConsoleUtils.MessageType.WARNING);
            
            int nextAvailable = NetworkUtils.findAvailablePort(DEFAULT_PORT + 1);
            if (nextAvailable != -1) {
                ConsoleUtils.displayMessage("next available port: " + nextAvailable, 
                                         ConsoleUtils.MessageType.INFO);
            } else {
                ConsoleUtils.displayMessage("no available ports found", 
                                         ConsoleUtils.MessageType.ERROR);
            }
        }
        
        ConsoleUtils.displayMessage("press Enter to return to main menu...", 
                                 ConsoleUtils.MessageType.SYSTEM);
        new Scanner(System.in).nextLine();
        
        main(new String[0]);
    }
    
    private static void createChatroom(String chatroomName, int port) {
        if (!NetworkUtils.isPortAvailable(port)) {
            ConsoleUtils.displayMessage("port " + port + " is already in use", 
                                     ConsoleUtils.MessageType.ERROR);
            
            int newPort = NetworkUtils.findAvailablePort(port + 1);
            if (newPort == -1) {
                ConsoleUtils.displayMessage("no available ports found. exiting...", 
                                         ConsoleUtils.MessageType.ERROR);
                return;
            }
            
            ConsoleUtils.displayMessage("using port " + newPort + " instead", 
                                     ConsoleUtils.MessageType.WARNING);
            port = newPort;
        }
        
        ConsoleUtils.printHeading("SERVER INFORMATION");
        ConsoleUtils.displayMessage("HOSTNAME: " + NetworkUtils.getHostname(), 
                                 ConsoleUtils.MessageType.INFO);
        
        ConsoleUtils.displayMessage("IP ADDRESSES CLIENTS CAN USE TO CONNECT:", 
                                 ConsoleUtils.MessageType.INFO);
        ArrayList<String> allIPs = NetworkUtils.getAllLocalIPs(false);
        for (String ip : allIPs) {
            System.out.println(ConsoleUtils.BLUE + "  - " + ip + ConsoleUtils.RESET);
        }
        
        ConsoleUtils.displayMessage("PORT: " + port, ConsoleUtils.MessageType.INFO);
        
        final int finalPort = port;
        new Thread(() -> {
            ChatServer server = new ChatServer(finalPort, chatroomName);
            server.tryStart();
        }).start();
        
        ConsoleUtils.showProgressAnimation("Starting server", 2000);
        
        joinChatroom("localhost", finalPort);
    }
    
    private static void joinChatroom(String serverIP, int port) {
        try {
            ConsoleUtils.clearScreen();
            ConsoleUtils.printHeading("JOIN CHATROOM");
            
            String username = ConsoleUtils.promptForInput(
                "enter your username: ", 
                input -> !input.trim().isEmpty(),
                "Username cannot be empty. Please enter a valid username."
            );
            
            ConsoleUtils.displayMessage("connecting to " + serverIP + ":" + port + "...", 
                                     ConsoleUtils.MessageType.SYSTEM);
            ConsoleUtils.showProgressAnimation("Establishing connection", 1500);
            
            Socket socket = new Socket(serverIP, port);
            
            String serverName = NetworkUtils.getHostnameFromIP(serverIP);
            if (serverName != null && !serverName.equals(serverIP) && !serverIP.equals("localhost")) {
                ConsoleUtils.displayMessage("connected to " + serverName + " (" + serverIP + ") on port " + port + "!", 
                                         ConsoleUtils.MessageType.SUCCESS);
            } else {
                ConsoleUtils.displayMessage("connected to the server!", 
                                         ConsoleUtils.MessageType.SUCCESS);
            }
            
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            output.println(username);
            
            Person user = new Person(username, false);
            
            new Thread(() -> {
                try {
                    String receivedText;
                    while ((receivedText = input.readLine()) != null) {
                        if (receivedText.contains("[SYSTEM]")) {
                            System.out.println(ConsoleUtils.CYAN + receivedText + ConsoleUtils.RESET);
                        } else if (receivedText.contains("[ERROR]")) {
                            System.out.println(ConsoleUtils.RED + receivedText + ConsoleUtils.RESET);
                        } else if (receivedText.contains("[Private")) {
                            System.out.println(ConsoleUtils.PURPLE + receivedText + ConsoleUtils.RESET);
                        } else {
                            System.out.println(receivedText);
                        }
                    }
                } catch (IOException e) {
                    ConsoleUtils.displayMessage("disconnected from the server.", 
                                             ConsoleUtils.MessageType.ERROR);
                }
            }).start();
            
            ConsoleUtils.printHeading("CHAT COMMANDS");
            System.out.println(ConsoleUtils.YELLOW + "• " + ConsoleUtils.RESET + "type your message and press 'Enter' to send");
            System.out.println(ConsoleUtils.YELLOW + "• " + ConsoleUtils.RESET + "start with " + ConsoleUtils.BOLD + "/p username message" + 
                             ConsoleUtils.RESET + " to send a private message");
            System.out.println(ConsoleUtils.YELLOW + "• " + ConsoleUtils.RESET + "type " + ConsoleUtils.BOLD + "/help" + 
                             ConsoleUtils.RESET + " for commands");
            System.out.println(ConsoleUtils.YELLOW + "• " + ConsoleUtils.RESET + "type " + ConsoleUtils.BOLD + "exit" + 
                             ConsoleUtils.RESET + " to leave the chat");
            System.out.println();
            
            Scanner scanner = new Scanner(System.in);
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
                        System.out.println(ConsoleUtils.PURPLE + "[" + message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + 
                                          "] [Private message to " + recipient + "]: " + content + ConsoleUtils.RESET);
                    } else {
                        ConsoleUtils.displayMessage("bad private message format. use: /p username message", 
                                                 ConsoleUtils.MessageType.ERROR);
                        continue;
                    }
                } else if (userInput.startsWith("/")) {
                    message = new Message(userInput, user, MessageType.COMMAND);
                } else {
                    message = new Message(userInput, user);
                    System.out.println(ConsoleUtils.GREEN + message.format() + ConsoleUtils.RESET);
                }
                
                output.println(message.getContent());
            }
            
            socket.close();
            ConsoleUtils.displayMessage("disconnected from chat server.", 
                                     ConsoleUtils.MessageType.INFO);
            scanner.close();
            
            if (ConsoleUtils.confirm("return to main menu?")) {
                main(new String[0]);
            }
            
        } catch (UnknownHostException e) {
            ConsoleUtils.displayMessage("couldn't find the server: " + e.getMessage(), 
                                     ConsoleUtils.MessageType.ERROR);
        } catch (ConnectException e) {
            ConsoleUtils.displayMessage("connection refused. the server may be offline or not running on this port.", 
                                     ConsoleUtils.MessageType.ERROR);
        } catch (IOException e) {
            ConsoleUtils.displayMessage("error connecting to server: " + e.getMessage(), 
                                     ConsoleUtils.MessageType.ERROR);
        }
    }
}