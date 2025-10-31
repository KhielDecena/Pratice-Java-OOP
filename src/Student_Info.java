import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Student_Info {
    static Scanner userinput = new Scanner(System.in);
    static Object object = new Object();
    static student stud = new student();

    public static class student {
        static Scanner userinput = new Scanner(System.in);
        String name;
        int age;
        int grade;

        void getname(){
            name = userinput.nextLine();
        }
        void getage(){
            age = userinput.nextInt();

        }
        void getgrade(){
            grade = userinput.nextInt();
        }

    }
    public static class start extends student {
        static void Start(){
            while (true) {
                System.out.println("[1] Add Student");
                System.out.println("[2] Show All Students");
                System.out.println("[3] Delete Student");
                System.out.println("[4] Exit");
                System.out.print("Choice The Number: ");
                String choice = userinput.nextLine();
                switch (choice) {
                    case "1":
                        AddStudent();

                }
                if (choice.equals("4")) {
                    break;
                }
            }
        }
        static void AddStudent(){

            try {
                FileWriter writer = new FileWriter("data.txt", true);
                System.out.print("Enter your name: ");
                stud.getname();
                System.out.print("Enter your age: ");
                stud.getage();
                System.out.print("Enter your grade: ");
                stud.getgrade();

                writer.write("["+object.getingTime()+"]");
                writer.write(
                        "\nName: "+ stud.name +
                                "\n" + "Age: " + stud.age +
                                "\n" + "Grade: " + stud.grade +
                                "\n\n");
                writer.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("❌ Error writing to file: ");
            }
        }
        static void setInfo(){
            String fileName = "data.txt";
            try {
                BufferedReader reader = new BufferedReader(new FileReader(fileName));
                String Line;
                while ((Line= reader.readLine()) != null) {
                    System.out.println(Line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static class Object {
        void TextInBox(String text){
            int width = 50; // lapad ng box

            // Top border
            for (int i = 0; i < width; i++) {
                System.out.print("=");
            }
            System.out.println();

            // Compute padding para nasa gitna
            int totalPadding = width - 2 - text.length();
            int leftPadding = totalPadding / 2;
            int rightPadding = totalPadding - leftPadding;

            // Middle line (may text sa gitna)
            System.out.print("|");
            for (int i = 0; i < leftPadding; i++) {
                System.out.print(" ");
            }
            System.out.print(text);
            for (int i = 0; i < rightPadding; i++) {
                System.out.print(" ");
            }
            System.out.println("|");

            // Bottom border
            for (int i = 0; i < width; i++) {
                System.out.print("=");
            }
            System.out.println();


        }
        String getingTime(){
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
            return now.format(formatter);
        }

    }

    public static void main(String[] args) {
        object.TextInBox("Welcome to the Student Management System");
        start.Start();

   }
}
