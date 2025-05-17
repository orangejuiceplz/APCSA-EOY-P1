package com.dl_labs.chatroom.games.tictactoe;

import java.util.ArrayList;

import com.dl_labs.chatroom.games.Game;
import com.dl_labs.chatroom.server.ChatServer;
import com.dl_labs.chatroom.user_stuff.Message;
import com.dl_labs.chatroom.user_stuff.Person;
import com.dl_labs.chatroom.user_stuff.Message.MessageType;

public class TicTacToeAdapter implements Game {
    private final ChatServer chatServer;
    private final ArrayList<Person> players = new ArrayList<>();
    private TicTacToe game;
    private boolean gameStarted = false;
    private boolean gameOver = false;
    private int currentPlayerIndex = 0;
    
    public TicTacToeAdapter(ChatServer chatServer) {
        this.chatServer = chatServer;
    }
    
    @Override
    public String getName() {
        return "TicTacToe";
    }
    
    @Override
    public int getMinPlayers() {
        return 2;
    }
    
    @Override
    public int getMaxPlayers() {
        return 2;
    }
    
    @Override
    public boolean addPlayer(Person player) {
        if (players.size() < getMaxPlayers() && !players.contains(player)) {
            players.add(player);
            return true;
        }
        return false;
    }
    
    @Override
    public void removePlayer(Person player) {
        players.remove(player);
        if (gameStarted && !gameOver) {
            endGameDueToPlayerLeaving(player);
        }
    }
    
    private void endGameDueToPlayerLeaving(Person player) {
        Message leaveMessage = new Message(player.getName() + " has left the game. Game over!", 
                null, MessageType.SYSTEM);
        chatServer.broadcastMessage(leaveMessage.format(), null);
        gameOver = true;
    }
    
    @Override
    public boolean isGameFull() {
        return players.size() >= getMaxPlayers();
    }
    
    @Override
    public boolean isGameReady() {
        return players.size() >= getMinPlayers();
    }
    
    @Override
    public void startGame() {
        if (isGameReady() && !gameStarted) {
            game = new TicTacToe('X', 'O');
            gameStarted = true;
            gameOver = false;
            currentPlayerIndex = 0;
            
            // Send initial game state to all players
            sendGameBoard();
            promptCurrentPlayer();
        }
    }
    
    private void sendGameBoard() {
        if (game != null) {
            String boardState = getBoardDisplay();
            Message boardMessage = new Message(boardState, null, MessageType.SYSTEM);
            for (Person player : players) {
                sendMessageToPerson(boardMessage.format(), player);
            }
        }
    }
    
    private void sendMessageToPerson(String message, Person person) {
        // This is a helper method to handle the missing ChatServer.sendMessageToPerson
        for (Person player : players) {
            if (player.equals(person)) {
                Message privateMsg = new Message(message, null, MessageType.SYSTEM);
                chatServer.broadcastMessage(privateMsg.format(), null);
                return;
            }
        }
    }
    
    private void broadcastMessageToPlayers(String message, ArrayList<Person> recipients) {
        // This is a helper method to handle the missing ChatServer.broadcastMessageToPlayers
        Message gameMsg = new Message(message, null, MessageType.SYSTEM);
        chatServer.broadcastMessage(gameMsg.format(), null);
    }
    
    private String getBoardDisplay() {
        StringBuilder display = new StringBuilder("Current Board:\n");
        display.append("┌───┬───┬───┐\n");
        
        for (int i = 0; i < 3; i++) {
            display.append("│");
            for (int j = 0; j < 3; j++) {
                char cell = game.getBoardCell(i, j);
                display.append(" ");
                display.append(cell == '_' ? ' ' : cell);
                display.append(" │");
            }
            display.append("\n");
            
            if (i < 2) {
                display.append("├───┼───┼───┤\n");
            }
        }
        
        display.append("└───┴───┴───┘\n");
        display.append("Positions: 1-9 (left to right, top to bottom)");
        
        return display.toString();
    }
    
    private void promptCurrentPlayer() {
        if (currentPlayerIndex < players.size()) {
            Person currentPlayer = players.get(currentPlayerIndex);
            Message promptMessage = new Message(currentPlayer.getName() + ", it's your turn! Enter a position (1-9):", 
                    null, MessageType.SYSTEM);
            sendMessageToPerson(promptMessage.format(), currentPlayer);
        }
    }
    
    @Override
    public boolean isGameOver() {
        return gameOver;
    }
    
    @Override
    public void handleInput(Person player, String input) {
        if (!gameStarted || gameOver) {
            return;
        }
        
        // Check if it's this player's turn
        if (!players.get(currentPlayerIndex).equals(player)) {
            Message notYourTurnMessage = new Message("It's not your turn!", null, MessageType.SYSTEM);
            sendMessageToPerson(notYourTurnMessage.format(), player);
            return;
        }
        
        try {
            int position = Integer.parseInt(input.trim());
            if (position < 1 || position > 9) {
                Message invalidInputMessage = new Message("Please enter a number between 1 and 9.", 
                        null, MessageType.SYSTEM);
                sendMessageToPerson(invalidInputMessage.format(), player);
                return;
            }
            
            // Convert 1-9 position to row, col
            int row = (position - 1) / 3;
            int col = (position - 1) % 3;
            
            char currentPlayerSymbol = (currentPlayerIndex == 0) ? 'X' : 'O';
            
            if (game.addMove(row, col, currentPlayerSymbol)) {
                // Broadcast the move to all players
                Message moveMessage = new Message(player.getName() + " placed " + currentPlayerSymbol + 
                        " at position " + position, null, MessageType.SYSTEM);
                broadcastMessageToPlayers(moveMessage.format(), players);
                
                // Check for win condition
                if (game.checkWin(currentPlayerSymbol)) {
                    Message winMessage = new Message(player.getName() + " wins the game!", 
                            null, MessageType.SYSTEM);
                    broadcastMessageToPlayers(winMessage.format(), players);
                    gameOver = true;
                    return;
                }
                
                // Check for draw (board full)
                if (isBoardFull()) {
                    Message drawMessage = new Message("Game ended in a draw!", null, MessageType.SYSTEM);
                    broadcastMessageToPlayers(drawMessage.format(), players);
                    gameOver = true;
                    return;
                }
                
                // Switch to the next player
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
                
                // Send updated board and prompt next player
                sendGameBoard();
                promptCurrentPlayer();
            } else {
                Message invalidMoveMessage = new Message("Invalid move! That position is already taken.", 
                        null, MessageType.SYSTEM);
                sendMessageToPerson(invalidMoveMessage.format(), player);
            }
        } catch (NumberFormatException e) {
            Message invalidInputMessage = new Message("Please enter a valid number.", 
                    null, MessageType.SYSTEM);
            sendMessageToPerson(invalidInputMessage.format(), player);
        }
    }
    
    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (game.getBoardCell(i, j) == '_') {
                    return false;
                }
            }
        }
        return true;
    }
    
    @Override
    public ArrayList<Person> getPlayers() {
        return new ArrayList<>(players);
    }
}