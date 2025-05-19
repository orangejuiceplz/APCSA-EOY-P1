//package com.dl_labs.chatroom.games.CardsAgainstHumanity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math.*;

public class CardsAgainstHumanity {
    private ArrayList<Integer> numsUsed = new ArrayList<>();
    private int numPlayers;
    private int numApples;
    private boolean boolRequireTwoCards = false;
    private int[] requiresTwoCards = {7, 8, 32, 37, 42, 70, 76, 81, 88, 91, 110, 111, 124, 125, 132, 135, 143, 154, 155, 172, 196, 200, 203, 218, 283, 298, 301, 309};

    public CardsAgainstHumanity(int numberOfPlayers) {
        numPlayers = numberOfPlayers;
    }

     public static String getElementFromArray(int rowIndex, int colIndex) throws Exception {
        String csvURL = "https://docs.google.com/spreadsheets/d/1lsy7lIwBe-DWOi2PALZPf5DgXHx9MEvKfRw1GaWQkzg/export?format=csv&gid=10";
        List<String[]> data = new ArrayList<>();
        rowIndex++;
        colIndex++;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URI(csvURL).toURL().openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = parseCSVLine(line);
                data.add(values);
            }
        }

        if (rowIndex < 0 || rowIndex >= data.size()) {
            return null; // Row index out of bounds
        }

        String[] row = data.get(rowIndex);
        if (colIndex < 0 || colIndex >= row.length) {
            return null; // Column index out of bounds
        }

        return row[colIndex];
    }

    private static String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }

     

    public String getRandomPrompt() {
    int randomNum = (int)((Math.random() * 316) + 7);
    if (randomNum == 158 || randomNum == 6 || numsUsed.indexOf(randomNum) != -1) { // ^^^^
        return getRandomPrompt();
    }
        try {
        numsUsed.add(randomNum);
        if (requiresTwoCards.indexOf(randomNum) !( -1{
            boolRequireTwoCards = true;
        } else {boolRequireTwoCards = false; }
        return getElementFromArray(randomNum, 0);
        /*if () {
            require2Cards = true;
        }*/
    } catch (Exception e) {
        return getRandomPrompt();
    }
}



    public static void main(String[] args) throws Exception {

        System.out.println("Throws exception");
   
    CardsAgainstHumanity e = new CardsAgainstHumanity(5);
        
    //System.out.println(e.getRandomPrompt());
    System.out.println(e.getRandomPrompt());
}


//To do: find a way to determine whether card requires 2 inputs or 1 (Row C)
//       Code basic game (w/ help from Shiven for multiplayer);

}


   

   

