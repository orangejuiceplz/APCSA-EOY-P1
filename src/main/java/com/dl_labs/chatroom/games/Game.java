package com.dl_labs.chatroom.games;

import java.util.ArrayList;
import com.dl_labs.chatroom.user_stuff.Person;


public interface Game {

    String getName();
    int getMinPlayers();
    int getMaxPlayers();
    boolean addPlayer(Person player);
    void removePlayer(Person player);
    boolean isGameFull();
    boolean isGameReady();
    void startGame();
    boolean isGameOver();
    void handleInput(Person player, String input);
    ArrayList<Person> getPlayers();
 
}
//