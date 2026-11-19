import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StudentMarksAnalyzer {

    // ================== YOUR ORIGINAL LOGIC (unchanged) ==================

    // Display all student marks
    static void displayMarks(int[] marks) {
        System.out.println("\nStudent Marks:");
        for (int i = 0; i < marks.length; i++) {
            System.out.println("Student " + (i + 1) + ": " + marks[i]);
        }
    }

    // Find highest marks
    static int findHighest(int[] marks) {
        int highest = marks[0];
        for (int i = 1; i < marks.length; i++) {
            if (marks[i] > highest) {
                highest = marks[i];
            }
        }
        return highest;
    }

    // Find lowest marks
    static int findLowest(int[] marks) {
        int lowest = marks[0];
        for (int i = 1; i < marks.length; i++) {
            if (marks[i] < lowest) {
                lowest = marks[i];
            }
        }
        return lowest;
    }

    // Calculate average marks
    static double calculateAverage(int[] marks) {
        int sum = 0;
        for (int i = 0; i < marks.length; i++) {
            sum += marks[i];
        }
        return (double) sum / marks.length;
    }

    // Sort marks using Bubble Sort
    static void sortMarks(int[] marks) {
        for (int i = 0; i < marks.length - 1; i++) {
            for (int j = 0; j < marks.length - 1 - i; j++) {
                if (marks[j] > marks[j + 1]) {
                    int temp = marks[j];
                    marks[j] = marks[j + 1];
                    marks[j + 1] = temp;
                }
            }
        }
    }

    // Search marks using Linear Search (console version)
    static void searchMarks(int[] marks, int target) {
        boolean found = false;
        for (int i = 0; i < marks.length; i++) {
            if (marks[i] == target) {
                System.out.println("Marks found at Student " + (i + 1));
                found = true;
            }
        }
        if (!found) {
            System.out.println("Marks not found!");
        }
    }

    // ================== CONSOLE MODE (your original main) ==================

    static void runConsole() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of students: ");
        int n = sc.nextInt();

        if (n <= 0) {
            System.out.println("Invalid number of students!");
            sc.close();
            return;
        }

        int[] marks = new int[n];

        // Input student marks
        for (int i = 0; i < n; i++) {
            System.out.print("Enter marks of Student " + (i + 1) + ": ");
            marks[i] = sc.nextInt();
            if (marks[i] < 0 || marks[i] > 100) {
                System.out.println("Marks must be between 0 and 100.");
                i--;
            }
        }

        int choice;
        do {
            System.out.println("\n===== STUDENT MARKS ANALYZER =====");
            System.out.println("1. Display Marks");
            System.out.println("2. Find Highest Marks");
            System.out.println("3. Find Lowest Marks");
            System.out.println("4. Calculate Average");
            System.out.println("5. Sort Marks");
            System.out.println("6. Search Marks");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    displayMarks(marks);
                    break;
                case 2:
                    System.out.println("Highest Marks: " + findHighest(marks));
                    break;
                case 3:
                    System.out.println("Lowest Marks: " + findLowest(marks));
                    break;
                case 4:
                    System.out.println("Average Marks: " + calculateAverage(marks));
                    break;
                case 5:
                    sortMarks(marks);
                    System.out.println("Marks sorted successfully!");
                    displayMarks(marks);
                    break;
                case 6:
                    System.out.print("Enter marks to search: ");
                    int target = sc.nextInt();
                    searchMarks(marks, target);
                    break;
                case 7:
                    System.out.println("Thank you!");
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        } while (choice != 7);

        sc.close();
    }

    // ================== WEB SERVER (new) ==================

    public static void main(String[] args) throws IOException {
        // Run "java -cp out StudentMarksAnalyzer --console" for the old menu
        if (args.length > 0 && args[0].equals("--console")) {
            runConsole();
            return;
        }

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/analyze", StudentMarksAnalyzer::handleAnalyze);
        server.createContext("/", StudentMarksAnalyzer::handleStatic);
        server.start();

        System.out.println("Server running at http://localhost:" + port);
        System.out.println("Press Ctrl+C to stop.");
    }

    // Handles POST /api/analyze -> {"action": "...", "marks": [...], "target": n}
    static void handleAnalyze(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            send(ex, 405, "application/json", "{\"ok\":false,\"message\":\"Only POST is allowed.\"}");
            return;
        }

        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        try {
            int[] marks = parseMarks(body);
            String action = matchString(body, "action");
            int[] result = marks.clone();
            List<Integer> highlight = new ArrayList<>();
            String message;

            switch (action) {
                case "display": {
                    message = "Showing all " + marks.length + " student marks.";
                    break;
                }
                case "highest": {
                    int highest = findHighest(marks);
                    highlight = indicesOf(marks, highest);
                    message = "Highest marks: " + highest + " (Student " + studentList(highlight) + ")";
                    break;
                }
                case "lowest": {
                    int lowest = findLowest(marks);
                    highlight = indicesOf(marks, lowest);
                    message = "Lowest marks: " + lowest + " (Student " + studentList(highlight) + ")";
                    break;
                }
                case "average": {
                    message = String.format(Locale.US, "Average marks: %.2f", calculateAverage(marks));
                    break;
                }
                case "sort": {
                    sortMarks(result);
                    message = "Marks sorted successfully!";
                    break;
                }
                case "search": {
                    int target = parseTarget(body);
                    highlight = indicesOf(marks, target);
                    message = highlight.isEmpty()
                            ? "Marks not found!"
                            : "Marks found at Student " + studentList(highlight);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Invalid action.");
            }

            String json = "{"
                    + "\"ok\":true,"
                    + "\"message\":\"" + escape(message) + "\","
                    + "\"marks\":" + arrayToJson(result) + ","
                    + "\"highlight\":" + listToJson(highlight) + ","
                    + "\"stats\":{"
                    + "\"highest\":" + findHighest(marks) + ","
                    + "\"lowest\":" + findLowest(marks) + ","
                    + "\"average\":" + String.format(Locale.US, "%.2f", calculateAverage(marks))
                    + "}}";

            send(ex, 200, "application/json", json);

        } catch (IllegalArgumentException e) {
            // Also catches NumberFormatException
            send(ex, 400, "application/json",
                    "{\"ok\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    // Serves files from the web/ folder (index.html, style.css, script.js)
    static void handleStatic(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }

        Path webRoot = Paths.get("web").toAbsolutePath().normalize();
        Path file = webRoot.resolve(path.substring(1)).normalize();

        if (!file.startsWith(webRoot) || !Files.isRegularFile(file)) {
            send(ex, 404, "text/plain", "Not found");
            return;
        }

        String type = "text/html";
        if (path.endsWith(".css"))
            type = "text/css";
        if (path.endsWith(".js"))
            type = "application/javascript";

        send(ex, 200, type, Files.readAllBytes(file));
    }

    // ================== HELPERS ==================

    // Reads {"marks":[...]} from the request body and validates each value
    static int[] parseMarks(String body) {
        Matcher m = Pattern.compile("\"marks\"\\s*:\\s*\\[([^\\]]*)\\]").matcher(body);
        if (!m.find() || m.group(1).isBlank()) {
            throw new IllegalArgumentException("No marks were provided.");
        }

        String[] parts = m.group(1).split(",");
        int[] marks = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            marks[i] = Integer.parseInt(parts[i].trim());
            if (marks[i] < 0 || marks[i] > 100) {
                throw new IllegalArgumentException("Marks must be between 0 and 100.");
            }
        }
        return marks;
    }

    static String matchString(String body, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([a-z]+)\"").matcher(body);
        if (!m.find()) {
            throw new IllegalArgumentException("Missing action.");
        }
        return m.group(1);
    }

    static int parseTarget(String body) {
        Matcher m = Pattern.compile("\"target\"\\s*:\\s*(-?\\d+)").matcher(body);
        if (!m.find()) {
            throw new IllegalArgumentException("Enter a mark to search.");
        }
        return Integer.parseInt(m.group(1));
    }

    static List<Integer> indicesOf(int[] arr, int value) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value)
                list.add(i);
        }
        return list;
    }

    static String studentList(List<Integer> indices) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indices.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(indices.get(i) + 1);
        }
        return sb.toString();
    }

    static String arrayToJson(int[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(arr[i]);
        }
        return sb.append("]").toString();
    }

    static String listToJson(List<Integer> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0)
                sb.append(",");
            sb.append(list.get(i));
        }
        return sb.append("]").toString();
    }

    static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    static void send(HttpExchange ex, int code, String contentType, String body) throws IOException {
        send(ex, code, contentType, body.getBytes(StandardCharsets.UTF_8));
    }

    static void send(HttpExchange ex, int code, String contentType, byte[] body) throws IOException {
        ex.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        ex.sendResponseHeaders(code, body.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(body);
        }
    }
}