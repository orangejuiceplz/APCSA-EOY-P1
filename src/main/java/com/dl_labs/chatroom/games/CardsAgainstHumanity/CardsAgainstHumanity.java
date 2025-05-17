import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math.*;

public class CardsAgainstHumanity {
    private Arraylist<int> numsUsed = new Arraylist<>();
    
    public String getRandomPrompt() {
        int randomNum = (int)((Math.random() * 316) + 7);
        if (numsUsed.contains(randomNum)) {
            getRandomPrompt();
        } else {
            numsUsed.add(randomNum);
            return row[randomNum];
        }
    }








    public static void main(String[] args) throws Exception {
        // Replace 'YOUR_GID_HERE' with the actual gid of the 'CAH Main Deck' sheet
        String csvUrl = "https://docs.google.com/spreadsheets/d/1lsy7lIwBe-DWOi2PALZPf5DgXHx9MEvKfRw1GaWQkzg/export?format=csv&gid=10";

        List<String[]> data = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(csvUrl).openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Use a CSV parser to handle commas within quoted fields
                String[] values = parseCSVLine(line);
                data.add(values);
            }
        }

        // Print column B (index 1) from rows 6 (index 5) to 311 (index 310)
        for (int i = 7; i < Math.min(data.size(), 322); i++) {
            String[] row = data.get(i);
            if (row.length > 1) { // Ensure column B exists
                System.out.println("Row " + (i - 6) + ": " + row[1]);
            } else {
                System.out.println("Row " + (i - 6) + ": [empty]");
            }
        }
    }

    // Basic CSV parser to handle commas within quoted fields
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
}