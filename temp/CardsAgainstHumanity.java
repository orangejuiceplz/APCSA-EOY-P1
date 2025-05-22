//pkg com.dl_labs.chatroom.games.CardsAgainstHumanity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Random;

public class CardsAgainstHumanity {
    private ArrayList<Integer> numsUsed = new ArrayList<>();
    private static int numPlayers;
    private int numApples;
    private boolean boolRequireTwoCards = false;
    private int[] requiresTwoCards = {7, 8, 32, 37, 42, 70, 76, 81, 88, 91, 110, 111, 124, 125, 132, 135, 143, 154, 155, 172, 196, 200, 203, 218, 283, 298, 301, 309};
    private static ArrayList<String[]> cards = new ArrayList<>();
    private ArrayList<Integer> indexesOfUsed = new ArrayList<>();
    private static int cardMasterIndex = 0;
    private static ArrayList<String> chosenCards = new ArrayList<>();
    public static String currentPrompt = "";

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
        currentPrompt = getElementFromArray(randomNum, 0);
        return getElementFromArray(randomNum, 0);
    } catch (Exception e) {
        return getRandomPrompt();
    }
}
public void setCards() {
    int randomNum = (int) ((Math.random() * 1420) + 315);
    //System.out.println("Initial random:" + randomNum);
    try {
        for (int i = 0; i < numPlayers; i++) {
            String[] playerCards = new String[7]; // Create a new array for each player
            for (int j = 0; j < 7; j++) { // Give each player 7 cards
                
                while (indexesOfUsed.contains(randomNum)) {
                    randomNum = (int) ((Math.random() * 1420) + 315);
                }
                //System.out.println("New Random Num:" + randomNum);
                indexesOfUsed.add(randomNum);
                playerCards[j] = getElementFromArray(randomNum, 0);
                //System.out.println("col index 1:" + getElementFromArray(randomNum, 1));
                //System.out.println("col index 0:" + getElementFromArray(randomNum, 0));
            }
            cards.set(i, playerCards); // Set the array for this player
        }
        System.out.println("Cards distributed to players");
        
        /*for (String[] array : cards) {
            System.out.println(Arrays.toString(array));
        }*/

    } catch (Exception e) {
        System.out.println("Error setting cards: " + e.getMessage());
    }
}

public static void showCards(int playerIndex) {
    int indexToUse = playerIndex + 1;
    String toReturn = "\nPlayer " + indexToUse + "'s current cards: \n";
    int i = 1;
    for (String prompt : cards.get(playerIndex)) {
        toReturn += i + ": " + prompt + "\n";
        i++;
    }
    toReturn += "\n";
    System.out.println(toReturn);
}

public static ArrayList<String> shuffleArray(ArrayList<String> arr) {
    Random rand = new Random();
    for (int i = arr.size() - 1; i > 0; i--) {
        int j = rand.nextInt(i + 1); // Generate random index between 0 and i (inclusive)
        arr = swap(arr, i, j);
    }
    return arr;
}

public static ArrayList<String> swap(ArrayList<String> arr, int i, int j) {
    String temp = arr.get(i);
        arr.set(i, arr.get(j));
        arr.set(j, temp);
        return arr;
}
  
/*public static void printArray(int[] arr) {
  for (int element : arr) {
    System.out.print(element + " ");
  }
  System.out.println();
}*/

// Add a method to add a single card to a player's hand
public void addCardToPlayer(int playerIndex) throws Exception {
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
    Scanner scanner = new Scanner(System.in);
    System.out.println("Welcome to Cards Against Humanity! How many players will be playing?");
    int numPlayers = scanner.nextInt();
    while (numPlayers < 2 || numPlayers > 8) {
        System.out.println("Invalid number of players. Must be 2-8.");
        numPlayers = scanner.nextInt();
    }
    System.out.println("Setting up a game with " + numPlayers + " players, this will take a minute.");
    CardsAgainstHumanity e = new CardsAgainstHumanity(numPlayers);
    
    e.setCards();
    //System.out.println(showCards(0));
    //System.out.println(showCards(3));
    System.out.println("Game set up. Which player will be the Card Master first? (1 - " + numPlayers + ")"); //You have to set up a way for everyone to know their numbers (1 - numPlayers) while the indexes used are (0 - numPlayers-1)
    cardMasterIndex = scanner.nextInt();
    while (cardMasterIndex < 1 || cardMasterIndex > numPlayers) {
        System.out.println("Invalid player number. Valid numbers are 1-" + numPlayers + ".");
        cardMasterIndex = scanner.nextInt();
    }
    int index = 0;
    
    int cardChoice = 0;
    int cardMasterIndexThing = 1;
    int cardMasterPlayer = cardMasterIndex + 1;
    while (true) {
        
            if (cardMasterIndex + 1 == numPlayers) {
                cardMasterIndex = 1;
            } else {
                cardMasterIndex++;
            }
            index = 1;
            int indexToUse = 0;
            cardMasterPlayer = cardMasterIndex + 1;
            System.out.println("The current Card Master is player " + (cardMasterIndex + 2));
            System.out.println("Prompt: " + e.getRandomPrompt());
            for (int i = 0; i < numPlayers; i++) {
                if (i != cardMasterIndex + 1) {
                    showCards(i);
                    indexToUse = i+1;
                    System.out.println("Player " + indexToUse + ", which card will you use?");
                    cardChoice = scanner.nextInt();
                    while (cardChoice < 1 || cardChoice > 7) {
                        System.out.println("Not a valid option. Please choose 1-7");
                        cardChoice = scanner.nextInt();
                    }
                    chosenCards.add(cards.get(i)[cardChoice-1]);
                } 
            }
            System.out.println(chosenCards + "\n");
            chosenCards = shuffleArray(chosenCards);
            System.out.println("Card Master, which card will you choose?"); //Add code to make sure the person who responds is the card master
            System.out.print("\n"); //Add code to skip the card master when people are inputting their prompts.

            System.out.println("Prompt: " + currentPrompt);

            for (String str : chosenCards) {
                System.out.println(cardMasterIndexThing + ": " + str);
                cardMasterIndexThing++;
            }
            int bestCard = scanner.nextInt();
            while (bestCard < 1 || bestCard > numPlayers) {
                System.out.println("Please select a card in bounds (1 - " + numPlayers + ")");
                bestCard = scanner.nextInt();
            
            System.out.println("Prompt " + bestCard + " (" + chosenCards.get(bestCard - 1).substring(0, chosenCards.get(bestCard - 1).length()-1) + ") won. Starting next round...");
        } 

    }
 }



   


//To do: find a way to determine whether card requires 2 inputs or 1 (Row C)
//       Code basic game (w/ help from Shiven for multiplayer);

}






