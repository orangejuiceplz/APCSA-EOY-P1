package com.dl_labs.chatroom.games.cardsgainsthumanity;

import java.util.ArrayList;
import java.util.Collections;

import com.dl_labs.chatroom.games.Game;
import com.dl_labs.chatroom.server.ChatServer;
import com.dl_labs.chatroom.user_stuff.Message;
import com.dl_labs.chatroom.user_stuff.Person;
import com.dl_labs.chatroom.user_stuff.Message.MessageType;

public class CardsAgainstHumanityAdapter implements Game {
    private final ChatServer chatServer;
    private final ArrayList<Person> players = new ArrayList<>();
    private CardsAgainstHumanity game;
    private boolean gameStarted = false;
    private boolean gameOver = false;
    private int cardMasterIndex = 0;
    private ArrayList<String> submittedCards = new ArrayList<>();
    private ArrayList<Person> playersWhoSubmitted = new ArrayList<>();
    private boolean waitingForCardMaster = false;
    private String currentPrompt = "";
    private int roundNumber = 1;
    
    public CardsAgainstHumanityAdapter(ChatServer chatServer) {
        this.chatServer = chatServer;
    }
    
    @Override
    public String getName() {
        return "Cards Against Humanity";
    }
    
    @Override
    public int getMinPlayers() {
        return 3;
    }
    
    @Override
    public int getMaxPlayers() {
        return 8;
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
            try {
                game = new CardsAgainstHumanity(players.size());
                game.setCards();
                gameStarted = true;
                gameOver = false;
                cardMasterIndex = 0;
                roundNumber = 1;
                
                StringBuilder announcement = new StringBuilder();
                announcement.append("=== CARDS AGAINST HUMANITY STARTED ===\n");
                announcement.append("Players in this game:\n");
                for (int i = 0; i < players.size(); i++) {
                    announcement.append((i + 1)).append(". ").append(players.get(i).getName()).append("\n");
                }
                announcement.append("\nEach player has been dealt 7 cards.\n");
                announcement.append("The game will proceed in rounds with rotating Card Masters.");
                
                Message startMsg = new Message(announcement.toString(), null, MessageType.SYSTEM);
                chatServer.broadcastMessage(startMsg.format(), null);
                
                sendCardsToPlayers();
                startNewRound();
                
            } catch (Exception e) {
                Message errorMsg = new Message("Failed to start Cards Against Humanity: " + e.getMessage(), 
                        null, MessageType.SYSTEM);
                chatServer.broadcastMessage(errorMsg.format(), null);
            }
        }
    }
    
    private void sendCardsToPlayers() {
        for (int i = 0; i < players.size(); i++) {
            sendPlayerCards(i);
        }
    }
    
    private void sendPlayerCards(int playerIndex) {
        try {
            Person player = players.get(playerIndex);
            StringBuilder cardsMessage = new StringBuilder();
            cardsMessage.append("=== YOUR CARDS ===\n");
            
            String[] playerCards = CardsAgainstHumanity.getPlayerCards(playerIndex);
            for (int j = 0; j < playerCards.length; j++) {
                if (playerCards[j] != null && !playerCards[j].trim().isEmpty()) {
                    cardsMessage.append((j + 1)).append(": ").append(playerCards[j]).append("\n");
                }
            }
            cardsMessage.append("\nUse the number (1-7) to select a card when prompted.");
            
            Message cardsMsg = new Message(cardsMessage.toString(), null, MessageType.SYSTEM);
            chatServer.sendMessageToPerson(cardsMsg.format(), player);
        } catch (Exception e) {
            Message errorMsg = new Message("Error sending cards to player: " + e.getMessage(), 
                    null, MessageType.SYSTEM);
            chatServer.sendMessageToPerson(errorMsg.format(), players.get(playerIndex));
        }
    }
    
    private void startNewRound() {
        try {
            submittedCards.clear();
            playersWhoSubmitted.clear();
            waitingForCardMaster = false;
            
            currentPrompt = game.getRandomPrompt();
            
            Person cardMaster = players.get(cardMasterIndex);
            StringBuilder roundMsg = new StringBuilder();
            roundMsg.append("=== ROUND ").append(roundNumber).append(" ===\n");
            roundMsg.append("Card Master: ").append(cardMaster.getName()).append("\n\n");
            roundMsg.append("PROMPT: ").append(currentPrompt).append("\n\n");
            roundMsg.append("All players except the Card Master, select a card by typing its number (1-7)!");
            
            Message roundMessage = new Message(roundMsg.toString(), null, MessageType.SYSTEM);
            chatServer.broadcastMessage(roundMessage.format(), null);
            
            Message masterMsg = new Message("You are the Card Master this round! Wait for other players to submit their cards.", 
                    null, MessageType.SYSTEM);
            chatServer.sendMessageToPerson(masterMsg.format(), cardMaster);
            
        } catch (Exception e) {
            Message errorMsg = new Message("Error starting new round: " + e.getMessage(), 
                    null, MessageType.SYSTEM);
            chatServer.broadcastMessage(errorMsg.format(), null);
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
        
        try {
            int playerIndex = players.indexOf(player);
            if (playerIndex == -1) {
                return;
            }
            
            if (playerIndex == cardMasterIndex) {
                if (waitingForCardMaster) {
                    handleCardMasterChoice(player, input);
                } else {
                    Message waitMsg = new Message("You are the Card Master. Wait for other players to submit cards.", 
                            null, MessageType.SYSTEM);
                    chatServer.sendMessageToPerson(waitMsg.format(), player);
                }
                return;
            }
            
            if (!waitingForCardMaster && !playersWhoSubmitted.contains(player)) {
                handleCardSubmission(player, input, playerIndex);
            } else if (playersWhoSubmitted.contains(player)) {
                Message alreadySubmittedMsg = new Message("You have already submitted a card for this round.", 
                        null, MessageType.SYSTEM);
                chatServer.sendMessageToPerson(alreadySubmittedMsg.format(), player);
            }
            
        } catch (Exception e) {
            Message errorMsg = new Message("Error processing your input: " + e.getMessage(), 
                    null, MessageType.SYSTEM);
            chatServer.sendMessageToPerson(errorMsg.format(), player);
        }
    }
    
    private void handleCardSubmission(Person player, String input, int playerIndex) {
        try {
            int cardChoice = Integer.parseInt(input.trim());
            
            if (cardChoice < 1 || cardChoice > 7) {
                Message invalidMsg = new Message("Please enter a number between 1 and 7.", 
                        null, MessageType.SYSTEM);
                chatServer.sendMessageToPerson(invalidMsg.format(), player);
                return;
            }
            
            String[] playerCards = CardsAgainstHumanity.getPlayerCards(playerIndex);
            String selectedCard = playerCards[cardChoice - 1];
            
            if (selectedCard == null || selectedCard.trim().isEmpty()) {
                Message invalidMsg = new Message("That card slot is empty. Please choose a different number.", 
                        null, MessageType.SYSTEM);
                chatServer.sendMessageToPerson(invalidMsg.format(), player);
                return;
            }
            
            submittedCards.add(selectedCard);
            playersWhoSubmitted.add(player);
            
            game.replacePlayerCard(playerIndex, cardChoice - 1);
            
            Message submittedMsg = new Message("Card submitted! Waiting for other players...", 
                    null, MessageType.SYSTEM);
            chatServer.sendMessageToPerson(submittedMsg.format(), player);
            
            Message announceMsg = new Message(player.getName() + " has submitted their card. (" + 
                    playersWhoSubmitted.size() + "/" + (players.size() - 1) + " submitted)", 
                    null, MessageType.SYSTEM);
            chatServer.broadcastMessage(announceMsg.format(), null);
            
            if (playersWhoSubmitted.size() == players.size() - 1) {
                presentCardsToCardMaster();
            }
            
        } catch (NumberFormatException e) {
            Message invalidMsg = new Message("Please enter a valid number.", 
                    null, MessageType.SYSTEM);
            chatServer.sendMessageToPerson(invalidMsg.format(), player);
        } catch (Exception e) {
            Message errorMsg = new Message("Error processing your card choice: " + e.getMessage(), 
                    null, MessageType.SYSTEM);
            chatServer.sendMessageToPerson(errorMsg.format(), player);
        }
    }
    
    private void presentCardsToCardMaster() {
        Collections.shuffle(submittedCards);
        
        Person cardMaster = players.get(cardMasterIndex);
        StringBuilder cardsMsg = new StringBuilder();
        cardsMsg.append("=== ALL CARDS SUBMITTED ===\n");
        cardsMsg.append("PROMPT: ").append(currentPrompt).append("\n\n");
        cardsMsg.append("Choose the best card by typing its number:\n");
        
        for (int i = 0; i < submittedCards.size(); i++) {
            cardsMsg.append((i + 1)).append(": ").append(submittedCards.get(i)).append("\n");
        }
        
        Message cardsMessage = new Message(cardsMsg.toString(), null, MessageType.SYSTEM);
        chatServer.sendMessageToPerson(cardsMessage.format(), cardMaster);
        
        Message waitingMsg = new Message("All cards have been submitted! " + cardMaster.getName() + 
                " is now choosing the winner...", null, MessageType.SYSTEM);
        for (Person player : players) {
            if (!player.equals(cardMaster)) {
                chatServer.sendMessageToPerson(waitingMsg.format(), player);
            }
        }
        
        waitingForCardMaster = true;
    }
    
    private void handleCardMasterChoice(Person cardMaster, String input) {
        try {
            int choice = Integer.parseInt(input.trim());
            
            if (choice < 1 || choice > submittedCards.size()) {
                Message invalidMsg = new Message("Please enter a number between 1 and " + submittedCards.size(), 
                        null, MessageType.SYSTEM);
                chatServer.sendMessageToPerson(invalidMsg.format(), cardMaster);
                return;
            }
            
            String winningCard = submittedCards.get(choice - 1);
            
            StringBuilder resultMsg = new StringBuilder();
            resultMsg.append("=== ROUND ").append(roundNumber).append(" RESULTS ===\n");
            resultMsg.append("PROMPT: ").append(currentPrompt).append("\n");
            resultMsg.append("WINNING CARD: ").append(winningCard).append("\n");
            resultMsg.append("Chosen by Card Master: ").append(cardMaster.getName()).append("\n");
            
            Message resultMessage = new Message(resultMsg.toString(), null, MessageType.SYSTEM);
            chatServer.broadcastMessage(resultMessage.format(), null);
            
            roundNumber++;
            cardMasterIndex = (cardMasterIndex + 1) % players.size();
            
            if (roundNumber > 5) {
                endGame();
            } else {
                sendCardsToPlayers();
                
                Message nextRoundMsg = new Message("Starting next round in 3 seconds...", 
                        null, MessageType.SYSTEM);
                chatServer.broadcastMessage(nextRoundMsg.format(), null);
                
                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        startNewRound();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
            
        } catch (NumberFormatException e) {
            Message invalidMsg = new Message("Please enter a valid number.", 
                    null, MessageType.SYSTEM);
            chatServer.sendMessageToPerson(invalidMsg.format(), cardMaster);
        } catch (Exception e) {
            Message errorMsg = new Message("Error processing your choice: " + e.getMessage(), 
                    null, MessageType.SYSTEM);
            chatServer.sendMessageToPerson(errorMsg.format(), cardMaster);
        }
    }
    
    private void endGame() {
        Message gameOverMsg = new Message("=== CARDS AGAINST HUMANITY GAME OVER ===\n" +
                "Thanks for playing! The game has ended after " + (roundNumber - 1) + " rounds.", 
                null, MessageType.SYSTEM);
        chatServer.broadcastMessage(gameOverMsg.format(), null);
        gameOver = true;
    }
    
    @Override
    public ArrayList<Person> getPlayers() {
        return new ArrayList<>(players);
    }
}
