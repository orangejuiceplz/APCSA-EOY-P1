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
            System.out.println("Connected to the server!");
            
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
            
            // Help text
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
                    } else {
                        System.out.println("bad private message format. use: /p username message");
                        continue;
                    }
                } else if (userInput.startsWith("/")) {
                    message = new Message(userInput, user, MessageType.COMMAND);
                } else {
                    message = new Message(userInput, user);
                }
                
                output.println(message.getContent());
            }
            
            socket.close();
            System.out.println("disconnected from chat server.");
            scanner.close();
            
        } catch (IOException e) {
            System.out.println("error connecting to server: " + e.getMessage());
        }
    }
} 