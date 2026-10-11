import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);

    private static final String QUESTION_FILE = "questions.dat";
    private static final String RESULT_FILE = "results.dat";

    private static ArrayList<Question> questions = new ArrayList<>();
    private static ArrayList<QuizResult> results = new ArrayList<>();

    // Represents one quiz question
    static class Question implements Serializable {
        private static final long serialVersionUID = 1L;

        int id;
        String text;
        String[] options;
        int correctOption;

        Question(int id, String text, String[] options, int correctOption) {
            this.id = id;
            this.text = text;
            this.options = options;
            this.correctOption = correctOption;
        }
    }

    // Stores the result of a quiz attempt
    static class QuizResult implements Serializable {
        private static final long serialVersionUID = 1L;

        String playerName;
        int totalQuestions;
        int correctAnswers;
        double percentage;
        String dateTime;

        QuizResult(String playerName, int totalQuestions, int correctAnswers) {
            this.playerName = playerName;
            this.totalQuestions = totalQuestions;
            this.correctAnswers = correctAnswers;
            this.percentage = (correctAnswers * 100.0) / totalQuestions;
            this.dateTime = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        }
    }

    public static void main(String[] args) {
        loadData();

        boolean running = true;

        System.out.println("======================================");
        System.out.println("        QUIZ MANAGEMENT SYSTEM");
        System.out.println("======================================");

        while (running) {
            System.out.println("\n1. Add Question");
            System.out.println("2. View All Questions");
            System.out.println("3. Search Question");
            System.out.println("4. Update Question");
            System.out.println("5. Delete Question");
            System.out.println("6. Start Quiz");
            System.out.println("7. View Quiz Results");
            System.out.println("8. Save and Exit");

            int choice = readInt("Enter your choice: ", 1, 8);

            switch (choice) {
                case 1 -> addQuestion();
                case 2 -> viewQuestions();
                case 3 -> searchQuestion();
                case 4 -> updateQuestion();
                case 5 -> deleteQuestion();
                case 6 -> startQuiz();
                case 7 -> viewResults();
                case 8 -> {
                    saveData();
                    System.out.println("Data saved. Goodbye!");
                    running = false;
                }
            }
        }

        sc.close();
    }

    private static int readInt(String message, int min, int max) {
        while (true) {
            System.out.print(message);
            String input = sc.nextLine().trim();

            try {
                int value = Integer.parseInt(input);

                if (value >= min && value <= max) {
                    return value;
                }

                System.out.println(
                        "Please enter a number between " + min + " and " + max + ".");

            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a whole number.");
            }
        }
    }

    private static String readText(String message) {
        while (true) {
            System.out.print(message);
            String input = sc.nextLine().trim();

            if (!input.isEmpty()) {
                return input;
            }

            System.out.println("Input cannot be empty.");
        }
    }

    private static Question findQuestion(int id) {
        for (Question q : questions) {
            if (q.id == id) {
                return q;
            }
        }

        return null;
    }

    private static int nextQuestionId() {
        int maxId = 0;

        for (Question q : questions) {
            if (q.id > maxId) {
                maxId = q.id;
            }
        }

        return maxId + 1;
    }

    private static String[] readOptions() {
        String[] options = new String[4];

        for (int i = 0; i < 4; i++) {
            options[i] = readText("Enter option " + (i + 1) + ": ");
        }

        return options;
    }

    private static void addQuestion() {
        System.out.println("\n--- ADD QUESTION ---");

        String text = readText("Enter question: ");
        String[] options = readOptions();

        int correct = readInt("Enter correct option (1-4): ", 1, 4);

        Question q = new Question(
                nextQuestionId(), text, options, correct);

        questions.add(q);
        saveData();

        System.out.println("Question added successfully!");
        System.out.println("Question ID: " + q.id);
    }

    private static void viewQuestions() {
        System.out.println("\n--- ALL QUESTIONS ---");

        if (questions.isEmpty()) {
            System.out.println("No questions available. Add some questions first.");
            return;
        }

        for (Question q : questions) {
            displayQuestion(q);
        }
    }

    private static void displayQuestion(Question q) {
        System.out.println("\nQuestion ID: " + q.id);
        System.out.println("Question: " + q.text);

        for (int i = 0; i < q.options.length; i++) {
            System.out.println((i + 1) + ". " + q.options[i]);
        }
    }

    private static void searchQuestion() {
        System.out.println("\n--- SEARCH QUESTION ---");

        int id = readInt("Enter question ID: ", 1, Integer.MAX_VALUE);
        Question q = findQuestion(id);

        if (q == null) {
            System.out.println("Question not found.");
        } else {
            displayQuestion(q);
        }
    }

    private static void updateQuestion() {
        System.out.println("\n--- UPDATE QUESTION ---");

        int id = readInt("Enter question ID to update: ", 1, Integer.MAX_VALUE);
        Question q = findQuestion(id);

        if (q == null) {
            System.out.println("Question not found.");
            return;
        }

        System.out.println("Current question:");
        displayQuestion(q);

        q.text = readText("Enter new question: ");
        q.options = readOptions();
        q.correctOption = readInt("Enter correct option (1-4): ", 1, 4);

        saveData();

        System.out.println("Question updated successfully!");
    }

    private static void deleteQuestion() {
        System.out.println("\n--- DELETE QUESTION ---");

        int id = readInt("Enter question ID to delete: ", 1, Integer.MAX_VALUE);
        Question q = findQuestion(id);

        if (q == null) {
            System.out.println("Question not found.");
            return;
        }

        String confirmation = readText(
                "Type YES to confirm deletion: ");

        if (confirmation.equals("YES")) {
            questions.remove(q);
            saveData();
            System.out.println("Question deleted successfully!");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private static void startQuiz() {
        System.out.println("\n--- START QUIZ ---");

        if (questions.isEmpty()) {
            System.out.println("No questions available. Add questions first.");
            return;
        }

        String playerName = readText("Enter your name: ");

        ArrayList<Question> quizQuestions = new ArrayList<>(questions);
        Collections.shuffle(quizQuestions);

        int score = 0;
        int number = 1;

        for (Question q : quizQuestions) {
            System.out.println("\nQuestion " + number + " of " + quizQuestions.size());
            System.out.println(q.text);

            for (int i = 0; i < q.options.length; i++) {
                System.out.println((i + 1) + ". " + q.options[i]);
            }

            int answer = readInt("Your answer (1-4): ", 1, 4);

            if (answer == q.correctOption) {
                System.out.println("Correct answer!");
                score++;
            } else {
                System.out.println("Wrong answer!");
                System.out.println("Correct answer: "
                        + q.options[q.correctOption - 1]);
            }

            number++;
        }

        QuizResult result = new QuizResult(
                playerName, quizQuestions.size(), score);

        results.add(result);
        saveData();

        System.out.println("\n========== QUIZ RESULT ==========");
        System.out.println("Player: " + result.playerName);
        System.out.println("Total questions: " + result.totalQuestions);
        System.out.println("Correct answers: " + result.correctAnswers);
        System.out.println("Wrong answers: "
                + (result.totalQuestions - result.correctAnswers));
        System.out.printf("Percentage: %.2f%%%n", result.percentage);

        if (result.percentage >= 80) {
            System.out.println("Performance: Excellent!");
        } else if (result.percentage >= 60) {
            System.out.println("Performance: Good!");
        } else if (result.percentage >= 40) {
            System.out.println("Performance: Average");
        } else {
            System.out.println("Performance: Keep Practicing!");
        }
    }

    private static void viewResults() {
        System.out.println("\n--- QUIZ RESULTS ---");

        if (results.isEmpty()) {
            System.out.println("No quiz results available.");
            return;
        }

        for (QuizResult r : results) {
            System.out.println("\nPlayer: " + r.playerName);
            System.out.println("Date: " + r.dateTime);
            System.out.println("Score: " + r.correctAnswers
                    + "/" + r.totalQuestions);
            System.out.printf("Percentage: %.2f%%%n", r.percentage);
        }
    }

    private static void saveData() {
        saveObject(QUESTION_FILE, questions);
        saveObject(RESULT_FILE, results);
    }

    private static void saveObject(String filename, Object data) {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(filename))) {

            out.writeObject(data);

        } catch (IOException e) {
            System.out.println("Error saving " + filename + ": " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadData() {
        questions = loadObject(QUESTION_FILE);
        results = loadObject(RESULT_FILE);
    }

    @SuppressWarnings("unchecked")
    private static <T> ArrayList<T> loadObject(String filename) {
        File file = new File(filename);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(file))) {

            return (ArrayList<T>) in.readObject();

        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            System.out.println("Could not load " + filename
                    + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}