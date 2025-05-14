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
        scanner.nextLine(); 
        
        if (choice == 1) {
            
            System.out.println("starting a chatroom server on port " + DEFAULT_PORT);
            createChatroom();
        } else if (choice == 2) {
            System.out.println("joining an existing chatroom");
            System.out.println("make sure the server is running before joining");
            System.out.print("enter the server IP address: ");
            String serverIP = scanner.nextLine();
            joinChatroom(serverIP);
        } else {
            System.out.println("invalid choice. exiting...");
        }
        
        scanner.close();
    }
    
    private static void createChatroom() {
        
        new Thread(() -> {
            ChatServer server = new ChatServer(DEFAULT_PORT);
            server.tryStart();
        }).start();
        
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        
        joinChatroom("localhost");
    }
    
    private static void joinChatroom(String serverIP) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("enter your username: ");
            String username = scanner.nextLine();
            
            Socket socket = new Socket(serverIP, DEFAULT_PORT);
            System.out.println("connected to the server!");
            
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            output.println(username);
            
            new Thread(() -> {
                try {
                    String message;
                    while ((message = input.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from the server.");
                }
            }).start();
            
            String message;
            while (scanner.hasNextLine()) {
                message = scanner.nextLine();
                output.println(message);
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
            }
            
            socket.close();
            scanner.close();
            
        } catch (IOException e) {
            System.out.println("error connecting to server: " + e.getMessage());
        }
    }
}