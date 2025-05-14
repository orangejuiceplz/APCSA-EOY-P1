package com.dl_labs.chatroom.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import com.dl_labs.chatroom.user_stuff.*;


public class ChatServer {
    private ServerSocket serverSocket;
    private final ArrayList<ClientHandler> clients = new ArrayList<>();
    private final ArrayList<Person> people = new ArrayList<>();
    private final int port;
    private boolean isServerRunning = false;

    public ChatServer(int port) {
        this.port = port;
    }

    public void tryStart() {
        try {
            serverSocket = new ServerSocket(port);
            isServerRunning = true;
            System.out.println("created a server started on port " + port);
            while (isServerRunning) {
                Socket socket = serverSocket.accept(); // https://docs.oracle.com/javase/8/docs/api/java/net/ServerSocket.html
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        } finally {
            stopServer();
        }
    }

    public void stopServer() {
        isServerRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("Server stopped.");
            } catch (IOException e) {
                System.out.println("Error stopping server: " + e.getMessage());
            }
        }
    }

    public void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }
    
    // New method to send private messages between users
    public boolean sendPrivateMessage(Message message, String recipientName) {
        for (ClientHandler client : clients) {
            Person person = client.getPerson();
            if (person != null && person.getName().equals(recipientName)) {
                client.sendMessage(message.format());
                return true;
            }
        }
        return false; // Recipient not found
    }
    
    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        
        // Also remove the person associated with this client
        if (clientHandler.getPerson() != null) {
            people.remove(clientHandler.getPerson());
        }
    }

    public void addUser(Person person) {
        people.add(person);
    }
    
    public ArrayList<Person> getPeople() {
        return people;
    }

}
