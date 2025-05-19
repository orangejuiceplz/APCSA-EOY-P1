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
            
            // Announce game start to all players
            String announcement = "=== TIC TAC TOE GAME STARTED ===\n" +
                                 "Player 1 (" + players.get(0).getName() + "): X\n" +
                                 "Player 2 (" + players.get(1).getName() + "): O\n";
            
            Message startMsg = new Message(announcement, null, MessageType.SYSTEM);
            chatServer.broadcastMessage(startMsg.format(), null);
            
            // Send initial game state
            sendGameBoard();
            promptCurrentPlayer();
        }
    }
    
    private void sendGameBoard() {
        if (game != null) {
            String boardState = getBoardDisplay();
            Message boardMessage = new Message("=== CURRENT GAME BOARD ===\n" + boardState, null, MessageType.SYSTEM);
            chatServer.broadcastMessage(boardMessage.format(), null);
        }
    }
    
    private String getBoardDisplay() {
        StringBuilder display = new StringBuilder();
        display.append("```\n");  // Use code block for better formatting
        display.append("┌───┬───┬───┐\n");
        
        for (int i = 0; i < 3; i++) {
            display.append("│");
            for (int j = 0; j < 3; j++) {
                char cell = game.getBoardCell(i, j);
                display.append(" ");
                if (cell == '_') {
                    // Show position number instead of blank space
                    display.append(i * 3 + j + 1);
                } else {
                    display.append(cell);
                }
                display.append(" │");
            }
            display.append("\n");
            
            if (i < 2) {
                display.append("├───┼───┼───┤\n");
            }
        }
        
        display.append("└───┴───┴───┘\n");
        display.append("```\n");
        
        return display.toString();
    }
    
    private void promptCurrentPlayer() {
        if (currentPlayerIndex < players.size()) {
            Person currentPlayer = players.get(currentPlayerIndex);
            String symbol = (currentPlayerIndex == 0) ? "X" : "O";
            
            Message promptMessage = new Message(
                "=== YOUR TURN ===\n" +
                "It's your turn, " + currentPlayer.getName() + "!\n" +
                "You are playing as " + symbol + "\n" +
                "Enter a number (1-9) to place your " + symbol, 
                null, MessageType.SYSTEM);
                
            chatServer.sendMessageToPerson(promptMessage.format(), currentPlayer);
            
            // Let other players know whose turn it is
            Person otherPlayer = players.get(currentPlayerIndex == 0 ? 1 : 0);
            Message waitMessage = new Message(
                "Waiting for " + currentPlayer.getName() + " to make a move...",
                null, MessageType.SYSTEM);
            chatServer.sendMessageToPerson(waitMessage.format(), otherPlayer);
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
            Message notYourTurnMessage = new Message("It's not your turn yet!", null, MessageType.SYSTEM);
            chatServer.sendMessageToPerson(notYourTurnMessage.format(), player);
            return;
        }
        
        try {
            int position = Integer.parseInt(input.trim());
            if (position < 1 || position > 9) {
                Message invalidInputMessage = new Message("Please enter a number between 1 and 9.", 
                        null, MessageType.SYSTEM);
                chatServer.sendMessageToPerson(invalidInputMessage.format(), player);
                return;
            }
            
            // Convert 1-9 position to row, col
            int row = (position - 1) / 3;
            int col = (position - 1) % 3;
            
            char currentPlayerSymbol = (currentPlayerIndex == 0) ? 'X' : 'O';
            
            if (game.addMove(row, col, currentPlayerSymbol)) {
                // Broadcast the move to all players
                Message moveMessage = new Message(
                    "=== GAME MOVE ===\n" + 
                    player.getName() + " placed " + currentPlayerSymbol + " at position " + position, 
                    null, MessageType.SYSTEM);
                chatServer.broadcastMessage(moveMessage.format(), null);
                
                // Check for win condition
                if (game.checkWin(currentPlayerSymbol)) {
                    Message winMessage = new Message(
                        "=== GAME OVER ===\n" +
                        "🎉 " + player.getName() + " wins the game! 🎉", 
                        null, MessageType.SYSTEM);
                    chatServer.broadcastMessage(winMessage.format(), null);
                    sendGameBoard(); // Show final board state
                    gameOver = true;
                    return;
                }
                
                // Check for draw (board full)
                if (isBoardFull()) {
                    Message drawMessage = new Message(
                        "=== GAME OVER ===\n" +
                        "Game ended in a draw!", 
                        null, MessageType.SYSTEM);
                    chatServer.broadcastMessage(drawMessage.format(), null);
                    sendGameBoard(); // Show final board state
                    gameOver = true;
                    return;
                }
                
                // Switch to the next player
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
                
                // Send updated board and prompt next player
                sendGameBoard();
                promptCurrentPlayer();
            } else {
                Message invalidMoveMessage = new Message(
                    "That position is already taken. Please choose another.", 
                    null, MessageType.SYSTEM);
                chatServer.sendMessageToPerson(invalidMoveMessage.format(), player);
            }
        } catch (NumberFormatException e) {
            Message invalidInputMessage = new Message("Please enter a valid number.", 
                    null, MessageType.SYSTEM);
            chatServer.sendMessageToPerson(invalidInputMessage.format(), player);
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