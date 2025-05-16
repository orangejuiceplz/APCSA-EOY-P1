 package com.dl_labs.chatroom.games;

import java.util.*;

public class TicTacToe {
    private char p1Char;
    private char p2Char;    
    private char[][] board = new char[3][3];
    private char currentPlayer;
    

    private ArrayList<String[][]> winningComboes = new ArrayList<>();
    

    public TicTacToe(char p1Char, char p2Char) {
        this.p1Char = p1Char;
        this.p2Char = p2Char;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = '_';
            }
        }
    }
    
    public boolean checkWin(char currentPlayer) {
        for (int i = 0; i < 3; i++) { //cols, rows, diags
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

        return false;
    }

    public void addMove(int row, int col, char player) {
        //System.out.println("|" + board[row][col] + "|");
        if (board[row][col] == '_') {
            board[row][col] = player;
        } 
        if (checkWin(player)) {
            System.out.println("Game Over, " + player + " won!");
        }
    }

    public int getMove() {

        System.out.println("[1 2 3] |  [" + board[0][0] + " " + board[0][1] + " " + board[0][2] + " ]");
        System.out.println("[4 5 6] |  [" + board[1][0] + " " + board[1][1] + " " + board[1][2] + " ]");
        System.out.println("[7 8 9] |  [" + board[2][0] + " " + board[2][1] + " " + board[2][2] + " ]");
        int answer = scanner.nextLine();
        try {
            if (bo)
        }
        }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Tic Tac Toe. Please input your character choice for player 1");
        
        String firstLetter = scanner.nextLine();
        while (firstLetter.length() > 1) {
            System.out.println("Invalid - please choose one character");
            firstLetter = scanner.nextLine();
        }
        
        System.out.println("Player 1 will be " + firstLetter + ". Please input your character choice for player 2");
        String secondLetter = scanner.nextLine();
        while (secondLetter.length() > 1) {
            System.out.println("Invalid - please choose one character");
            secondLetter = scanner.nextLine();
        }
        System.out.println("Player one is '" + firstLetter + "', Player two is '" + secondLetter + "'");
        char firstChar = firstLetter.charAt(0);
        char secondChar = secondLetter.charAt(0);
        TicTacToe game = new TicTacToe(firstChar, secondChar);


        while (game.checkWin(firstChar) == false && game.checkWin(secondChar) == false) {
            System.out.println("Player 1, choose your location");
            game.addMove(0, 0, 'X');
            game.addMove(2, 2, 'O');
            game.getMove();
            
            break;
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
}

final String firstCharColor = RED;
final String secondCharColor = BLACK;

        System.out.println("[1 2 3] |  [" + board[0][0] + " " + board[0][1] + " " + board[0][2] + " ]");
        System.out.println("[4 5 6] |  [" + board[1][0] + " " + board[1][1] + " " + board[1][2] + " ]");
        System.out.println("[7 8 9] |  [" + board[2][0] + " " + board[2][1] + " " + board[2][2] + " ]");

 System.out.print("[1 2 3] |  ["
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
