package com.dl_labs.chatroom.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

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
                
                if (chatServer.getGameManager().hasActiveGame() && 
                    chatServer.getGameManager().isPlayerInGame(person) && 
                    isGameInput(inputLine)) {
                    
                    // Route input to game manager
                    chatServer.getGameManager().handleGameInput(person, inputLine);
                    continue;
                }
                
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
                    } else if (inputLine.startsWith("/game")) {
                        handleGameCommand(inputLine);
                    } else if (inputLine.startsWith("/join")) {
                        joinGame();
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

    private void handleGameCommand(String input) {
        if (!person.isHost()) {
            Message errorMsg = new Message("Only the host can start games.", null, MessageType.SYSTEM);
            sendMessage(errorMsg.format());
            return;
        }
        
        String[] parts = input.split("\\s+", 2);
        if (parts.length == 1) {
            // Just "/game" - show available games
            ArrayList<String> games = chatServer.getGameManager().getAvailableGames();
            if (games.isEmpty()) {
                Message errorMsg = new Message("No games are available.", null, MessageType.SYSTEM);
                sendMessage(errorMsg.format());
            } else {
                StringBuilder gameList = new StringBuilder("Available games:\n");
                for (String game : games) {
                    gameList.append("- ").append(game).append("\n");
                }
                gameList.append("To start a game: /game <game_name>");
                Message gamesMsg = new Message(gameList.toString(), null, MessageType.SYSTEM);
                sendMessage(gamesMsg.format());
            }
        } else {
            // "/game <name>" - start the specified game
            String gameName = parts[1].trim();
            if (chatServer.getGameManager().hasActiveGame()) {
                Message errorMsg = new Message("A game is already in progress.", null, MessageType.SYSTEM);
                sendMessage(errorMsg.format());
            } else {
                boolean started = chatServer.getGameManager().startGame(gameName, person);
                if (!started) {
                    Message errorMsg = new Message("Failed to start game '" + gameName + "'. Game may not exist.", null, MessageType.SYSTEM);
                    sendMessage(errorMsg.format());
                }
            }
        }
    }

    private void joinGame() {
        if (!chatServer.getGameManager().hasActiveGame()) {
            Message errorMsg = new Message("No game is currently active to join.", null, MessageType.SYSTEM);
            sendMessage(errorMsg.format());
            return;
        }
        
        boolean joined = chatServer.getGameManager().joinGame(person);
        if (!joined) {
            Message errorMsg = new Message("Failed to join the game. The game may be full or you may already be in it.", null, MessageType.SYSTEM);
            sendMessage(errorMsg.format());
        }
    }

    // Helper method to determine if input should be treated as a game command
    private boolean isGameInput(String input) {
        // This is a simple check for TicTacToe (1-9)
        // Can be expanded for other games as needed
        if (input.trim().matches("\\d+")) {
            return true;
        }
        return false;
    }
}
