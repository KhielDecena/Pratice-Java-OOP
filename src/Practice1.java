import java.io.*;
import java.util.*;

/**
 * Practice1 — Reads student information from a text file and
 * displays it in formatted boxes. Demonstrates clean structure,
 * validation, and clear separation of concerns.
 */
public class Practice1 {

    /** Stores all student credentials */
    static ArrayList<Credentials> students = new ArrayList<>();

    /**
     * Inner class representing student information.
     * Encapsulates validation and data presentation.
     */
    public static class Credentials {
        private String firstName, lastName, phoneNo, age;
        private static final int MAX_PHONE_LENGTH = 12;

        /**
         * Constructor ensures valid phone number length before assigning values.
         */
        public Credentials(String firstName, String lastName, String age, String phoneNo) {
            if (phoneNo.length() > MAX_PHONE_LENGTH) {
                System.out.printf(
                        "❌ Phone number for %s %s is too long! Must be 12 digits or less.%n",
                        firstName, lastName
                );
                throw new IllegalArgumentException("Phone number too long.");
            }
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
            this.phoneNo = phoneNo;
        }

        /** Returns formatted information lines for box display */
        public String[] getLines() {
            return new String[]{
                    "First Name: " + firstName,
                    "Last Name: " + lastName,
                    "Age: " + age,
                    "Phone Number: " + phoneNo
            };
        }
    }

    /** Main program entry point */
    public static void main(String[] args) {
        String fileName = "studentsInformation.txt"; // File must be in the same folder
        readStudentsFromFile(fileName);

        int boxWidth = calculateBoxWidth(50);
        displayAllStudents(boxWidth);
    }

    /**
     * Reads student records from a file.
     * Expected format per line: FirstName, LastName, Age, PhoneNumber
     */
    public static void readStudentsFromFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    addStudent(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim());
                } else {
                    System.out.println("⚠️ Invalid line format: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("⚠️ Error reading file: " + e.getMessage());
        }
    }

    /** Adds a validated student to the list */
    public static void addStudent(String first, String last, String age, String phone) {
        try {
            students.add(new Credentials(first, last, age, phone));
        } catch (IllegalArgumentException e) {
            System.out.printf("⚠️ Student %s %s was not added.%n%n", first, last);
        }
    }

    /** Calculates dynamic box width based on the longest line of text */
    public static int calculateBoxWidth(int minWidth) {
        int maxLength = 0;
        for (Credentials c : students)
            for (String line : c.getLines())
                maxLength = Math.max(maxLength, line.length());
        return Math.max(minWidth, maxLength + 6);
    }

    /** Displays all student records in formatted boxes */
    public static void displayAllStudents(int boxWidth) {
        int studentNumber = 1;
        for (Credentials c : students) {
            displayBox(c, boxWidth, studentNumber++);
            System.out.println();
        }
    }

    /** Prints a single student’s information in a formatted box */
    public static void displayBox(Credentials c, int boxWidth, int studentNum) {
        String[] lines = {
                "Student " + studentNum,
                "First Name: " + c.firstName,
                "Last Name: " + c.lastName,
                "Age: " + c.age,
                "Phone Number: " + c.phoneNo
        };

        printBorder(boxWidth);
        printCenteredLine(lines[0], boxWidth);
        printBorder(boxWidth);

        for (int i = 1; i < lines.length; i++)
            printLeftAlignedLine(lines[i], boxWidth);

        printBorder(boxWidth);
    }

    /** Prints the top and bottom border lines */
    public static void printBorder(int width) {
        System.out.println("=".repeat(width));
    }

    /** Centers a given line of text within the box */
    public static void printCenteredLine(String text, int width) {
        int padding = width - 2 - text.length();
        int left = Math.max(padding / 2, 0);
        int right = Math.max(padding - left, 0);
        System.out.printf("|%s%s%s|%n", " ".repeat(left), text, " ".repeat(right));
    }

    /** Left-aligns a given line of text within the box */
    public static void printLeftAlignedLine(String text, int width) {
        int padding = Math.max(width - 3 - text.length(), 0);
        System.out.printf("| %s%s|%n", text, " ".repeat(padding));
    }
}
