package com.dl_labs.chatroom.games.tictactoe;

import java.util.Scanner;

public class TicTacToe {
    @SuppressWarnings("unused")
    private char p1Char;
    @SuppressWarnings("unused")
    private char p2Char;    
    private char[][] board = new char[3][3];
    private static char currentPlayer;
    private int turnCount = 0;

    public TicTacToe(char p1Char, char p2Char) {
        this.p1Char = p1Char;
        this.p2Char = p2Char;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = '_';
            }
        }
    }
    
    public class color {
        public static final String RESET = "\033[0m";
        public static final String BLACK = "\033[0;30m";   
        public static final String RED = "\033[0;31m";     
        public static final String GREEN = "\033[0;32m";   
        public static final String YELLOW = "\033[0;33m";  
        public static final String BLUE = "\033[0;34m";    
        public static final String PURPLE = "\033[0;35m";  
        public static final String CYAN = "\033[0;36m";    
        public static final String WHITE = "\033[0;37m";   
        public static final String BOLD = "\033[1m";
    }

    public boolean checkWin(char currentPlayer) {
        for (int i = 0; i < 3; i++) { // cols, rows, diags
            if (board[0][i] == currentPlayer && board[1][i] == currentPlayer && board[2][i] == currentPlayer) {
                return true;
            }       
            if (board[i][0] == currentPlayer && board[i][1] == currentPlayer && board[i][2] == currentPlayer) {
                return true;
            }
        } 
        if (board[0][0] == currentPlayer && board[1][1] == currentPlayer && board[2][2] == currentPlayer) {
            return true;
        }
        if (board[0][2] == currentPlayer && board[1][1] == currentPlayer && board[2][0] == currentPlayer) {
            return true;
        }
        if (turnCount == 9) {return true;}
        return false;
    }

    public boolean addMove(int row, int col, char player) {
        if (board[row][col] == '_') {
            board[row][col] = player;
            return true;
        } 
        return false;
    }

    public void getMove(char currentSymbol, char firstChar, char secondChar) {
        Scanner scanner = new Scanner(System.in);
        //System.out.println("[1 2 3] |  [" + board[0][0] + " " + board[0][1] + " " + board[0][2] + " ]");
        //System.out.println("[4 5 6] |  [" + board[1][0] + " " + board[1][1] + " " + board[1][2] + " ]");
        //System.out.println("[7 8 9] |  [" + board[2][0] + " " + board[2][1] + " " + board[2][2] + " ]");
        for (int i = 0; i < 3; i++) {
            if (i == 0) { System.out.print("[1 2 3] |  ["); }
            else if (i == 1) { System.out.print("[4 5 6] |  ["); }
            else { System.out.print("[7 8 9] |  ["); }
            for (int e = 0; e < 3; e++) {
                if (board[i][e] == firstChar) { 
                    System.out.print(color.RED + board[i][e] + color.RESET);
                } else if (board[i][e] == secondChar) {
                    System.out.print(color.BLACK + board[i][e] + color.RESET); 
                } else {
                    System.out.print("_");
                }
                System.out.print(" ");
                
            }
            System.out.println("]");
        }
        
        int answer = scanner.nextInt();
        try { 
            if (answer <= 9 && answer >= 1) {
                int temp = 1;
                int otherTemp = 1;
                if (answer == 1 || answer == 4 || answer == 7) { temp = 0; }
                else if (answer == 2 || answer == 5 || answer == 8) { temp = 1; }
                else { temp = 2; }
                if (answer == 1 || answer == 2 || answer == 3) { otherTemp = 0; }
                if (answer == 4 || answer == 5 || answer == 6) { otherTemp = 1; }
                if (answer == 7 || answer == 8 || answer == 9) { otherTemp = 2; }
                if (!addMove(otherTemp, temp, currentSymbol)) {
                    System.out.println("Current spot is taken. Please choose another");
                    this.getMove(currentSymbol, firstChar, secondChar);
                }
            }
        } catch (Exception e) {
            //System.out.println("Exception occurred: " + e.getMessage());
            System.out.println("Error occured. Exiting game");

        }
    }

    public char getBoardCell(int row, int col) {
        if (row >= 0 && row < 3 && col >= 0 && col < 3) {
            return board[row][col];
        }
        return ' ';
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Tic Tac Toe. Please input your character choice for player 1");
        
        String firstLetter = scanner.nextLine();
        while (firstLetter.length() != 1) {
            System.out.println("Invalid - please choose one character");
            firstLetter = scanner.nextLine();
        }
        
        System.out.println("Player 1 will be " + firstLetter + ". Please input your character choice for player 2");
        String secondLetter = scanner.nextLine();
        while (secondLetter.length() != 1 || secondLetter.equals(firstLetter)) {
            if (secondLetter.length() != 1) {
                System.out.println("Invalid - please choose one character");
            }
            if (secondLetter.equals(firstLetter)) {
                System.out.println("Invalid - character cannot match player 1");
            }
            secondLetter = scanner.nextLine();
        }
        
        System.out.println("Player one is '" + firstLetter + "', Player two is '" + secondLetter + "'");
        char firstChar = firstLetter.charAt(0);
        char secondChar = secondLetter.charAt(0);
        TicTacToe game = new TicTacToe(firstChar, secondChar);

        while (!game.checkWin(firstChar) && !game.checkWin(secondChar)) {
            System.out.println("Player 1, choose your location");
            currentPlayer = firstChar;
            game.getMove(firstChar, firstChar, secondChar);
            game.turnCount++;
            if (game.checkWin(currentPlayer)) {
                System.out.println("Game Over, " + currentPlayer + " won!");
                break;
            }

            System.out.println("Player 2, choose your location");
            currentPlayer = secondChar;
            game.getMove(secondChar, firstChar, secondChar);
            game.turnCount++;
            if (game.checkWin(currentPlayer)) {
                if (game.turnCount != 9) {
                    System.out.println("Game Over, " + currentPlayer + " won!");
                } else {
                    System.out.println("Game Over, game ended in a tie.");
                }
                break;
            }
        }
    }
}

/* for the future 
public class ColoredConsole {
    // Reset
    public static final String RESET = "\033[0m";

    // Regular Colors
    public static final String BLACK = "\033[0;30m";   
    public static final String RED = "\033[0;31m";     
    public static final String GREEN = "\033[0;32m";   
    public static final String YELLOW = "\033[0;33m";  
    public static final String BLUE = "\033[0;34m";    
    public static final String PURPLE = "\033[0;35m";  
    public static final String CYAN = "\033[0;36m";    
    public static final String WHITE = "\033[0;37m";   
}

// Example usage for colored output:
final String firstCharColor = RED;
final String secondCharColor = BLACK;

System.out.println("[1 2 3] |  [" + board[0][0] + " " + board[0][1] + " " + board[0][2] + " ]");
System.out.println("[4 5 6] |  [" + board[1][0] + " " + board[1][1] + " " + board[1][2] + " ]");
System.out.println("[7 8 9] |  [" + board[2][0] + " " + board[2][1] + " " + board[2][2] + " ]");

// Or, for colored printing:
System.out.print("[1 2 3] |  [");
for (int e = 0; e < 3; e++) {
    if (board[0][e] == firstChar) { 
        System.out.print(RED + board[0][e] + RESET);
    } else if (board[0][e] == secondChar) {
        System.out.print(BLACK + board[0][e] + RESET); 
    } else {
        System.out.print("_");
    }
    System.out.print(" ");
}
System.out.print(" ]");
*/