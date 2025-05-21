package com.dl_labs.chatroom.games;

import java.util.ArrayList;

import com.dl_labs.chatroom.games.tictactoe.TicTacToeAdapter;
import com.dl_labs.chatroom.user_stuff.Message;
import com.dl_labs.chatroom.user_stuff.Person;
import com.dl_labs.chatroom.server.ChatServer;
import com.dl_labs.chatroom.user_stuff.Message.MessageType;

public class GameManager {
    private final ChatServer chatServer;
    private Game activeGame = null;
    private final ArrayList<GameInfo> availableGames = new ArrayList<>();
    private final ArrayList<Person> waitingPlayers = new ArrayList<>();

    private static class GameInfo {
        private final String name;
        private final GameSupplier supplier;

        public GameInfo(String name, GameSupplier supplier) {
            this.name = name;
            this.supplier = supplier;
        }

        public String getName() {
            return name;
        }

        public Game createGame() {
            return supplier.createGame();
        }
    }

    private interface GameSupplier {
        Game createGame();
    }

    public GameManager(ChatServer chatServer) {
        this.chatServer = chatServer;
        registerGames();
    }

    private void registerGames() {
        availableGames.add(new GameInfo("Tic Tac Toe", () -> new TicTacToeAdapter(chatServer)));
    }

    public ArrayList<String> getAvailableGames() {
        ArrayList<String> gamesNames = new ArrayList<>();
        for (GameInfo gameInfo : availableGames) {
            gamesNames.add(gameInfo.getName());
        }
        return gamesNames;
    }

    public boolean startGame(String gameName, Person host) {
        if (activeGame != null) {
            return false;
        }

        GameInfo selectedGame = null;

        for (GameInfo gameInfo : availableGames) {
            if (gameInfo.getName().equalsIgnoreCase(gameName)) {
                selectedGame = gameInfo;
                break;
            }
        }

        if (selectedGame == null) {
            return false;
        }

        activeGame = selectedGame.createGame();
        activeGame.addPlayer(host);
        waitingPlayers.clear();

        Message gameMessage = new Message("game " + gameName + " has been started by " + host.getName() + 
                ". type /join to participate! " + 
                "(" + activeGame.getMinPlayers() + "-" + activeGame.getMaxPlayers() + " players)",
                null, MessageType.SYSTEM);
        chatServer.broadcastMessage(gameMessage.format(), null);
        
        return true;
    }

    public boolean joinGame(Person player) {
        if (activeGame == null) {
            return false; 
        }
        
        if (activeGame.isGameFull()) {
            return false; 
        }
        
        if (activeGame.getPlayers().contains(player)) {
            return false; 
        }
        
        boolean joined = activeGame.addPlayer(player);
        if (joined) {
            Message joinMessage = new Message(player.getName() + " has joined the game! " + 
                    activeGame.getPlayers().size() + "/" + activeGame.getMaxPlayers() + " players.",
                    null, MessageType.SYSTEM);
            chatServer.broadcastMessage(joinMessage.format(), null);
            
            if (activeGame.isGameReady()) {
                startActiveGame();
            }
        }
        
        return joined;
    }

    private void startActiveGame() {
        if (activeGame != null && activeGame.isGameReady()) {
            Message startMessage = new Message("Game is starting! All players have joined.", 
                    null, MessageType.SYSTEM);
            chatServer.broadcastMessage(startMessage.format(), null);
            
            activeGame.startGame();
        }
    }
    
    public void handleGameInput(Person player, String input) {
        if (activeGame != null && activeGame.getPlayers().contains(player)) {
            Message debugMsg = new Message(
                "processing game input '" + input + "' from " + player.getName(),
                null, MessageType.SYSTEM);
            chatServer.sendMessageToPerson(debugMsg.format(), player);
            
            activeGame.handleInput(player, input);
            
            if (activeGame.isGameOver()) {
                endGame();
            }
        } else if (activeGame == null) {
            Message errorMsg = new Message("No active game to handle your input.", 
                                     null, MessageType.SYSTEM);
            chatServer.sendMessageToPerson(errorMsg.format(), player);
        } else if (!activeGame.getPlayers().contains(player)) {
            Message errorMsg = new Message("You're not a participant in the current game.", 
                                     null, MessageType.SYSTEM);
            chatServer.sendMessageToPerson(errorMsg.format(), player);
        }
    }
    
    public void endGame() {
        if (activeGame != null) {
            Message endMessage = new Message("Game has ended!", null, MessageType.SYSTEM);
            chatServer.broadcastMessage(endMessage.format(), null);
            activeGame = null;
        }
    }
    
    public boolean hasActiveGame() {
        return activeGame != null;
    }
    
    public Game getActiveGame() {
        return activeGame;
    }
    
    public boolean isPlayerInGame(Person player) {
        return activeGame != null && activeGame.getPlayers().contains(player);
    }
}

