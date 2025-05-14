package com.dl_labs.chatroom;

import java.util.Scanner;
import java.io.*;
import java.net.*;

import com.dl_labs.chatroom.server.ChatServer;

public class App {
    private static final int DEFAULT_PORT = 12345;
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("test");
        System.out.println("1. create a chatroom (become host)");
        System.out.println("2. join an existing chatroom");
        System.out.print("enter your choice (1 or 2): ");
        
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        
        if (choice == 1) {
            // Start a server
            System.out.println("starting a chatroom server on port " + DEFAULT_PORT);
            createChatroom();
        } else if (choice == 2) {
            // Join a server
            System.out.print("enter the server IP address: ");
            String serverIP = scanner.nextLine();
            joinChatroom(serverIP);
        } else {
            System.out.println("invalid choice. exiting...");
        }
        
        scanner.close();
    }
    
    private static void createChatroom() {
        // Start server in a separate thread so it doesn't block
        new Thread(() -> {
            ChatServer server = new ChatServer(DEFAULT_PORT);
            server.tryStart();
        }).start();
        
        // Give the server a moment to start
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Connect to the server as a client
        joinChatroom("localhost");
    }
    
    private static void joinChatroom(String serverIP) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            
            Socket socket = new Socket(serverIP, DEFAULT_PORT);
            System.out.println("Connected to the server!");
            
            // Set up input and output streams
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Send username to the server
            out.println(username);
            
            // Start a thread to receive messages from the server
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from the server.");
                }
            }).start();
            
            // Read user input and send messages
            String message;
            while (scanner.hasNextLine()) {
                message = scanner.nextLine();
                out.println(message);
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
            }
            
            // Close resources
            socket.close();
            scanner.close();
            
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }
}