package com.dl_labs.chatroom.games.tictactoe;
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

    public boolean addMove(int row, int col, char player) {
        if (board[row][col] == "") {
            board[row][col] = player;
        } else { return false; }
        if (checkWin(player)) {
            System.out.println("Game Over, " + player + " won!");
        }
    }

    public int getPos()

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

        while (checkWin(firstLetter) == false && checkWin(secondLetter) == false) {
            System.out.println("Player 1, choose your location")
        }
    }
    
}