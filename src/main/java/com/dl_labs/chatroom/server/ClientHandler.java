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

            Message welcomeMessage = new Message("welcome to '" + chatServer.getChatName() + "'! you are now connected.");
            sendMessage(welcomeMessage.format());
            
            Message joinMessage = new Message(userName + " has joined the chat.");
            chatServer.broadcastMessage(joinMessage.format(), this);

            String inputLine;
            while (running && (inputLine = input.readLine()) != null) {
                
                Message message;
                
                if (inputLine.startsWith("@")) {
                    int spaceIndex = inputLine.indexOf(' ');
                    if (spaceIndex > 0) {
                        String recipient = inputLine.substring(1, spaceIndex);
                        message = new Message(inputLine, person, MessageType.PRIVATE);
                        
                        boolean delivered = chatServer.sendPrivateMessage(message, recipient);
                        if (!delivered) {
                            Message errorMsg = new Message("user '" + recipient + "' not found or offline.", 
                                                         null, MessageType.SYSTEM);
                            sendMessage(errorMsg.format());
                            continue;
                        }
                    } else {
                        continue;
                    }
                } else if (inputLine.startsWith("/")) {
                    message = new Message(inputLine, person, MessageType.COMMAND);
                    
                    if (inputLine.equals("/help")) {
                        Message helpMessage = new Message(
                            "the commands:\n" +
                            "/help - show this help\n" +
                            "/users - list online users\n" +
                            "@username message - send private message\n" +
                            "exit - disconnect from chat",
                            null, MessageType.SYSTEM
                        );
                        sendMessage(helpMessage.format());
                        continue;
                    } else if (inputLine.equals("/users")) {
                        StringBuilder userList = new StringBuilder("online users:\n");
                        for (Person p : chatServer.getPeople()) {
                            userList.append("- ").append(p.getName())
                                  .append(p.isHost() ? " (host)" : "").append("\n");
                        }
                        Message usersMessage = new Message(userList.toString(), null, MessageType.SYSTEM);
                        sendMessage(usersMessage.format());
                        continue;
                    }
                } else {
                    message = new Message(inputLine, person);
                }
                
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
            System.out.println("err disconnecting client: " + e.getMessage());
        }
    }
}
