package com.dl_labs.chatroom.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import com.dl_labs.chatroom.user_stuff.*;
import com.dl_labs.chatroom.games.GameManager;


public class ChatServer {
    private ServerSocket serverSocket;
    private final ArrayList<ClientHandler> clients = new ArrayList<>();
    private final ArrayList<Person> people = new ArrayList<>();
    private final int port;
    private final String chatName;
    private boolean isServerRunning = false;
    private final GameManager gameManager;

    public ChatServer(int port) {
        this(port, "Default Chatroom");
    }

    public ChatServer(int port, String chatName) {
        this.port = port;
        this.chatName = chatName;
        this.gameManager = new GameManager(this);
    }

    public String getChatName() {
        return chatName;
    }

    public void tryStart() {
        try {
            serverSocket = new ServerSocket(port);
            isServerRunning = true;
            System.out.println("created chatroom '" + chatName + "' on port " + port);
            while (isServerRunning) {
                Socket socket = serverSocket.accept(); // https://docs.oracle.com/javase/8/docs/api/java/net/ServerSocket.html
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            System.out.println("error starting server: " + e.getMessage());
        } finally {
            stopServer();
        }
    }

    public void stopServer() {
        isServerRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("server stopped.");
            } catch (IOException e) {
                System.out.println("error stopping server: " + e.getMessage());
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
    
    public boolean sendPrivateMessage(Message message, String recipientName) {
        for (ClientHandler client : clients) {
            Person person = client.getPerson();
            if (person != null && person.getName().equals(recipientName)) {
                client.sendMessage(message.format());
                return true;
            }
        }
        return false; // only if recipient isn't foudn
    }
    
    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        
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

    public GameManager getGameManager() {
        return gameManager;
    }

    public void sendMessageToPerson(String message, Person person) {
        for (ClientHandler client : clients) {
            if (client.getPerson() != null && client.getPerson().equals(person)) {
                client.sendMessage(message);
                return;
            }
        }
    }

    public void broadcastMessageToPlayers(String message, ArrayList<Person> players) {
        for (ClientHandler client : clients) {
            if (client.getPerson() != null && players.contains(client.getPerson())) {
                client.sendMessage(message);
            }
        }
    }
}
