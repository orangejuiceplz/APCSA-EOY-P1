//pkg com.dl_labs.chatroom.games.CardsAgainstHumanity;

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
    private ArrayList<String[]> cards = new ArrayList<>();
    private ArrayList<Integer> indexesOfUsed = new ArrayList<>();

    public CardsAgainstHumanity(int numberOfPlayers) {
        numPlayers = numberOfPlayers;
        // Initialize the ArrayList with an array for each player
        for (int i = 0; i < numPlayers; i++) {
            cards.add(new String[7]); // Giving each player 7 cards initially
        }
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

     

    private boolean arrayContains(int[] arr, int value) {
        for (int i : arr) {
            if (i == value) {
                return true;
            }
        }
        return false;
    }

public String getRandomPrompt() {
    int randomNum;
    do {
        randomNum = (int) (Math.random() * 312); // Assuming 312 prompts
    } while (numsUsed.contains(randomNum));
    try {
        numsUsed.add(randomNum);
        if (arrayContains(requiresTwoCards, randomNum)) {
            boolRequireTwoCards = true;
        } else {
            boolRequireTwoCards = false;
        }
        return getElementFromArray(randomNum, 0);
    } catch (Exception e) {
        return getRandomPrompt();
    }
}
public void setCards() {
    try {
        for (int i = 0; i < numPlayers; i++) {
            String[] playerCards = new String[7]; // Create a new array for each player
            for (int j = 0; j < 7; j++) { // Give each player 7 cards
                int randomNum = (int) ((Math.random() * 1420) + 315);
                while (indexesOfUsed.contains(randomNum)) {
                    randomNum = (int) ((Math.random() * 1420) + 315);
                }
                indexesOfUsed.add(randomNum);
                playerCards[j] = getElementFromArray(randomNum, 1);
            }
            cards.set(i, playerCards); // Set the array for this player
        }
        System.out.println("Cards distributed to players");
    } catch (Exception e) {
        System.out.println("Error setting cards: " + e.getMessage());
    }
}

// Add a method to add a single card to a player's hand
public void addCardToPlayer(int playerIndex) {
    if (playerIndex >= 0 && playerIndex < numPlayers) {
        String[] currentCards = cards.get(playerIndex);
        String[] newCards = new String[currentCards.length + 1];
        
        // Copy existing cards
        for (int i = 0; i < currentCards.length; i++) {
            newCards[i] = currentCards[i];
        }
        
         int randomNum = (int) ((Math.random() * 1420) + 315);
         while (indexesOfUsed.contains(randomNum)) {
             randomNum = (int) ((Math.random() * 1420) + 315);
         }
         newCards[currentCards.length] = getElementFromArray(randomNum, 1);
         indexesOfUsed.add(randomNum);
        
        // Update the player's cards
        cards.set(playerIndex, newCards);
    }
}


    public static void main(String[] args) throws Exception {

        //System.out.println("Throws exception");
   
    CardsAgainstHumanity e = new CardsAgainstHumanity(5);
        
    //System.out.println(e.getRandomPrompt());
    System.out.println(e.getRandomPrompt());
    e.setCards();
    //System.out.println(this.cards);
}


//To do: find a way to determine whether card requires 2 inputs or 1 (Row C)
//       Code basic game (w/ help from Shiven for multiplayer);

}






