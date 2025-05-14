package com.dl_labs.chatroom.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.dl_labs.chatroom.user_stuff.Person;



public class ClientHandler {
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

    @Override
    public void run() {
        try {
            output = new PrintWriter(clientSocket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String userName = input.readLine()
            boolean isHost = false;

            if (chatServer.getUsers().isEmpty()) {
                isHost = true;
            }

            person = new Person(userName, isHost);
            chatServer.addUser(person);

            sendMessage("Welcome " + userName + "! You are now connected to the chat server.");
            chatServer.broadcastMessage(userName + " has joined the chat.", this);

            String inputLine;

            while (running && (inputLine = in.readLine()) != null) {
                System.out.println("Message from " + userName + ": " + inputLine);
                chatServer.broadcastMessage(userName + ": " + inputLine, this);
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

            if (user != null) {
                chatServer.broadcastMessage(user.getName() + " has left the chat.", this);
            }
        }
    }
}
