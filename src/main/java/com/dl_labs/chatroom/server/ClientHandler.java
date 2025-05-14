package com.dl_labs.chatroom.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.dl_labs.chatroom.user_stuff.Message;
import com.dl_labs.chatroom.user_stuff.Person;
import com.dl_labs.chatroom.user_stuff.Message.MessageType;



public class ClientHandler extends Thread {
    private final ChatServer chatServer;
    private final Socket clientSocket;
    private PrintWriter output;
    private BufferedReader input;
    private Person person;
    private boolean running = true;

    public ClientHandler(Socket socket, ChatServer chatServer) {
        this.clientSocket = socket;
        this.chatServer = chatServer;
    }

    public void run() {
        try {
            output = new PrintWriter(clientSocket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String userName = input.readLine();
            boolean isHost = false;

            if (chatServer.getPeople().isEmpty()) {
                isHost = true;
            }

            person = new Person(userName, isHost);
            chatServer.addUser(person);

            // Send welcome message using Message class
            Message welcomeMessage = new Message("Welcome " + userName + "! You are now connected to the chat server.");
            sendMessage(welcomeMessage.format());
            
            // Broadcast join notification using Message class
            Message joinMessage = new Message(userName + " has joined the chat.");
            chatServer.broadcastMessage(joinMessage.format(), this);

            String inputLine;
            while (running && (inputLine = input.readLine()) != null) {
                System.out.println("Message from " + userName + ": " + inputLine);
                
                // Create appropriate Message object based on content
                Message message;
                
                if (inputLine.startsWith("@")) {
                    // Handle private messages
                    int spaceIndex = inputLine.indexOf(' ');
                    if (spaceIndex > 0) {
                        String recipient = inputLine.substring(1, spaceIndex);
                        message = new Message(inputLine, person, MessageType.PRIVATE);
                        
                        // Try to send to specific recipient
                        boolean delivered = chatServer.sendPrivateMessage(message, recipient);
                        if (!delivered) {
                            // If user not found, notify sender
                            Message errorMsg = new Message("User '" + recipient + "' not found or offline.", 
                                                         null, MessageType.SYSTEM);
                            sendMessage(errorMsg.format());
                            continue;
                        }
                    } else {
                        // Invalid format
                        continue;
                    }
                } else if (inputLine.startsWith("/")) {
                    // Command message
                    message = new Message(inputLine, person, MessageType.COMMAND);
                    
                    // Handle special commands
                    if (inputLine.equals("/help")) {
                        Message helpMessage = new Message(
                            "Available commands:\n" +
                            "/help - Show this help\n" +
                            "/users - List online users\n" +
                            "@username message - Send private message\n" +
                            "exit - Disconnect from chat",
                            null, MessageType.SYSTEM
                        );
                        sendMessage(helpMessage.format());
                        continue;
                    } else if (inputLine.equals("/users")) {
                        StringBuilder userList = new StringBuilder("Online users:\n");
                        for (Person p : chatServer.getPeople()) {
                            userList.append("- ").append(p.getName())
                                  .append(p.isHost() ? " (host)" : "").append("\n");
                        }
                        Message usersMessage = new Message(userList.toString(), null, MessageType.SYSTEM);
                        sendMessage(usersMessage.format());
                        continue;
                    }
                } else {
                    // Regular chat message
                    message = new Message(inputLine, person);
                }
                
                // Broadcast the message to all clients
                chatServer.broadcastMessage(message.format(), this);
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    public void sendMessage(String message) {
        if (output != null) {
            output.println(message);
        }
    }

    public Person getPerson() {
        return person;
    }

    public void disconnect() {
        running = false;
        try {
            if (input != null) {
                input.close();
            } 
            if (output != null) {
                output.close();
            }

            if (clientSocket != null) {
                clientSocket.close();
            }

            chatServer.removeClient(this);

            if (person != null) {
                Message leaveMessage = new Message(person.getName() + " has left the chat.");
                chatServer.broadcastMessage(leaveMessage.format(), this);
            }
        } catch (IOException e) {
            System.out.println("Error disconnecting client: " + e.getMessage());
        }
    }
}
