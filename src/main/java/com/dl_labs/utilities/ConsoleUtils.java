package com.dl_labs.utilities;

import java.util.Scanner;
import java.util.List;
import java.util.function.Predicate;

public class ConsoleUtils {


    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    
    public static final String BG_BLACK = "\u001B[40m";
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_PURPLE = "\u001B[45m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_WHITE = "\u001B[47m";
    
    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";
    
    private static final Scanner scanner = new Scanner(System.in);
    
    public static void clearScreen() {
        try {
            final String os = System.getProperty("os.name");
            
            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
    
    public static void printHeading(String text) {
        int width = 60;
        String border = "=".repeat(width);
        
        System.out.println(BOLD + CYAN + border + RESET);
        System.out.println(BOLD + CYAN + centerText(text, width) + RESET);
        System.out.println(BOLD + CYAN + border + RESET);
    }
    
    public static String centerText(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text;
    }
    
    public static int showMenu(String title, List<String> options) {
        printHeading(title);
        
        for (int i = 0; i < options.size(); i++) {
            System.out.println(YELLOW + (i + 1) + ". " + RESET + options.get(i));
        }
        
        System.out.print(BOLD + "\nEnter your choice (1-" + options.size() + "): " + RESET);
        
        while (true) {
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice >= 1 && choice <= options.size()) {
                    return choice;
                } else {
                    System.out.print(RED + "Invalid choice. Try again: " + RESET);
                }
            } catch (NumberFormatException e) {
                System.out.print(RED + "Please enter a number: " + RESET);
            }
        }
    }
    
    public static String promptForInput(String prompt, Predicate<String> validator, String errorMessage) {
        System.out.print(BOLD + prompt + RESET);
        
        while (true) {
            String input = scanner.nextLine();
            
            if (validator.test(input)) {
                return input;
            } else {
                System.out.print(RED + errorMessage + RESET + "\n" + BOLD + prompt + RESET);
            }
        }
    }
    

    public static String promptForString(String prompt) {
        System.out.print(BOLD + prompt + RESET);
        return scanner.nextLine();
    }
    
    public static int promptForInt(String prompt, int min, int max) {
        while (true) {
            try {
                String input = promptForString(prompt);
                
                if (input.isEmpty()) {
                    return min;
                }
                
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.println(RED + "Please enter a number between " + min + " and " + max + RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(RED + "Please enter a valid number" + RESET);
            }
        }
    }
    
    public static void displayMessage(String message, MessageType messageType) {
        switch (messageType) {
            case SYSTEM:
                System.out.println(CYAN + "[SYSTEM] " + message + RESET);
                break;
            case ERROR:
                System.out.println(RED + "[ERROR] " + message + RESET);
                break;
            case SUCCESS:
                System.out.println(GREEN + "[SUCCESS] " + message + RESET);
                break;
            case INFO:
                System.out.println(BLUE + "[INFO] " + message + RESET);
                break;
            case WARNING:
                System.out.println(YELLOW + "[WARNING] " + message + RESET);
                break;
            default:
                System.out.println(message);
        }
    }
    
    public enum MessageType {
        SYSTEM,
        ERROR,
        SUCCESS,
        INFO,
        WARNING
    }
    
    /**
     * Displays a simple progress animation for a specified duration
     * @param message The message to display alongside the animation
     * @param durationMs The duration to show the animation in milliseconds
     */
    public static void showProgressAnimation(String message, long durationMs) {
        char[] animationChars = new char[]{'|', '/', '-', '\\'};
        long startTime = System.currentTimeMillis();
        int i = 0;
        
        try {
            while (System.currentTimeMillis() - startTime < durationMs) {
                System.out.print("\r" + message + " " + animationChars[i++ % animationChars.length]);
                Thread.sleep(100);
            }
            System.out.print("\r" + " ".repeat(message.length() + 2) + "\r");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public static void printTable(String[] headers, List<String[]> data) {
        int[] columnWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = headers[i].length();
        }
        
        for (String[] row : data) {
            for (int i = 0; i < row.length; i++) {
                if (i < columnWidths.length && row[i].length() > columnWidths[i]) {
                    columnWidths[i] = row[i].length();
                }
            }
        }
        
        printTableRow(headers, columnWidths, true);
        
        for (int width : columnWidths) {
            System.out.print("+" + "-".repeat(width + 2));
        }
        System.out.println("+");
        
        for (String[] row : data) {
            printTableRow(row, columnWidths, false);
        }
    }
    
    private static void printTableRow(String[] row, int[] columnWidths, boolean isHeader) {
        for (int i = 0; i < row.length; i++) {
            if (i < columnWidths.length) {
                String text = row[i];
                String padding = " ".repeat(columnWidths[i] - text.length());
                
                if (isHeader) {
                    System.out.print("| " + BOLD + text + RESET + padding + " ");
                } else {
                    System.out.print("| " + text + padding + " ");
                }
            }
        }
        System.out.println("|");
    }
    
    public static boolean confirm(String message) {
        System.out.print(YELLOW + message + " (y/n): " + RESET);
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("y") || response.equals("yes");
    }
}
