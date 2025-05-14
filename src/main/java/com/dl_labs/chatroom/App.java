package com.dl_labs.chatroom;

import java.util.Scanner;
import java.io.*;
import java.net.*;

import com.dl_labs.chatroom.server.ChatServer;
import com.dl_labs.chatroom.user_stuff.Message;
import com.dl_labs.chatroom.user_stuff.Person;
import com.dl_labs.chatroom.user_stuff.Message.MessageType;

public class App {
    private static final int DEFAULT_PORT = 12345;
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("welcome to DL labs chatroom");
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
            Thread.sleep(1000); // Wait a bit for server to start
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
            System.out.println("Connected to the server!");
            
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Send username first to identify on the server
            output.println(username);
            
            // Create a Person object for the current user
            Person user = new Person(username, false);
            
            // Start a thread to receive and display messages
            new Thread(() -> {
                try {
                    String receivedText;
                    while ((receivedText = input.readLine()) != null) {
                        // Just display the message as is since it's already formatted
                        // by the server using Message.format()
                        System.out.println(receivedText);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from the server.");
                }
            }).start();
            
            // Help text
            System.out.println("Commands:");
            System.out.println("- Type your message and press Enter to send");
            System.out.println("- Start with /p [username] to send a private message");
            System.out.println("- Type /help for commands");
            System.out.println("- Type 'exit' to leave the chat");
            
            // Main loop to read and send user input
            String userInput;
            while (scanner.hasNextLine()) {
                userInput = scanner.nextLine();
                
                if (userInput.equalsIgnoreCase("exit")) {
                    break;
                }
                
                // Create an appropriate Message object based on input
                Message message;
                
                if (userInput.startsWith("/p ")) {
                    // Private message format: /p username message
                    int spaceIndex = userInput.indexOf(' ', 3);
                    if (spaceIndex > 0) {
                        String recipient = userInput.substring(3, spaceIndex);
                        String content = userInput.substring(spaceIndex + 1);
                        message = new Message("@" + recipient + " " + content, user, MessageType.PRIVATE);
                    } else {
                        System.out.println("Invalid private message format. Use: /p username message");
                        continue;
                    }
                } else if (userInput.startsWith("/")) {
                    // Command message
                    message = new Message(userInput, user, MessageType.COMMAND);
                } else {
                    // Regular chat message
                    message = new Message(userInput, user);
                }
                
                // Send the message content to the server
                // (the server will handle creating Message objects with the sender)
                output.println(message.getContent());
            }
            
            socket.close();
            System.out.println("Disconnected from chat server.");
            scanner.close();
            
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }
}