import java.util.Scanner;  // Import the Scanner class

class Main {
  public static void main(String[] args) {
    Scanner myObj = new Scanner(System.in);  // Create a Scanner object
    System.out.println("What's your zip code?");

    String zipCode = myObj.nextLine();  // Read user input
    System.out.println("ZipCode is: " + zipCode);  // Output user input

    /* call to API's w/ zip code
    Recieve weather info */

    int temp = 100;
    String dashes = "-------------------------------------";
    String spaces = "                                       ";
    String halfSpaces = "                  ";
    for (int i = 0; i < 15; i++) {
      
      if (i == 0 || i == 7 || i == 14) {System.out.println("|" + dashes + "|");}
      else {System.out.println("|" + halfSpaces + "|" + halfSpaces + "|");}
    }
    System.out.println();
  }
}