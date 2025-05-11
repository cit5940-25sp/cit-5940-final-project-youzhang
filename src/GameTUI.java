import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * A simple Text User Interface (TUI) for the Movie Connection Game
 */
public class GameTUI {
    private static final String API_BASE_URL = "http://localhost:8080/api";
    private static final Scanner scanner = new Scanner(System.in);
    private static final JSONParser jsonParser = new JSONParser();
    
    private static String player1Name;
    private static String player1Genre;
    private static String player2Name;
    private static String player2Genre;
    private static int winThreshold = 3;
    private static boolean gameStarted = false;
    private static int currentPlayerIndex = 0;
    private static String lastMovieTitle = "";
    
    public static void main(String[] args) {
        System.out.println("=== Movie Connection Game TUI ===");
        
        while (true) {
            displayMenu();
            int choice = getUserChoice(1, 7);
            
            try {
                switch (choice) {
                    case 1:
                        setupGame();
                        break;
                    case 2:
                        startGame();
                        break;
                    case 3:
                        getGameStatus();
                        break;
                    case 4:
                        searchAndSelectMovie();
                        break;
                    case 5:
                        useAbility();
                        break;
                    case 6:
                        nextPlayer();
                        break;
                    case 7:
                        System.out.println("Exiting game. Goodbye!");
                        return;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }
    
    private static void displayMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Setup Game");
        System.out.println("2. Start Game");
        System.out.println("3. Get Game Status");
        System.out.println("4. Search and Select Movie");
        System.out.println("5. Use Ability (Skip/Block)");
        System.out.println("6. Next Player");
        System.out.println("7. Exit");
        System.out.print("Enter your choice (1-7): ");
    }
    
    private static int getUserChoice(int min, int max) {
        int choice = -1;
        while (choice < min || choice > max) {
            try {
                choice = Integer.parseInt(scanner.nextLine());
                if (choice < min || choice > max) {
                    System.out.print("Please enter a number between " + min + " and " + max + ": ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
        return choice;
    }
    
    private static void setupGame() {
        System.out.println("\n=== Game Setup ===");
        
        System.out.print("Enter Player 1 name: ");
        player1Name = scanner.nextLine();
        
        System.out.print("Enter Player 1 target genre (e.g., action, comedy, drama): ");
        player1Genre = scanner.nextLine();
        
        System.out.print("Enter Player 2 name: ");
        player2Name = scanner.nextLine();
        
        System.out.print("Enter Player 2 target genre (e.g., action, comedy, drama): ");
        player2Genre = scanner.nextLine();
        
        System.out.print("Enter win threshold (number of movies needed to win, default is 3): ");
        String thresholdStr = scanner.nextLine();
        if (!thresholdStr.isEmpty()) {
            try {
                winThreshold = Integer.parseInt(thresholdStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number, using default value of 3");
                winThreshold = 3;
            }
        }
        
        System.out.println("\nGame setup complete!");
        System.out.println("Player 1: " + player1Name + " (Target: " + player1Genre + ")");
        System.out.println("Player 2: " + player2Name + " (Target: " + player2Genre + ")");
        System.out.println("Win Threshold: " + winThreshold);
    }
    
    private static void startGame() throws IOException, ParseException {
        if (player1Name == null || player1Genre == null || player2Name == null || player2Genre == null) {
            System.out.println("Please setup the game first (option 1)");
            return;
        }
        
        System.out.println("\n=== Starting Game ===");
        
        String endpoint = "/game/start";
        String requestBody = String.format(
            "{\"player1Name\":\"%s\",\"player1Genre\":\"%s\",\"player2Name\":\"%s\",\"player2Genre\":\"%s\",\"winThreshold\":%d}",
            player1Name, player1Genre, player2Name, player2Genre, winThreshold
        );
        
        JSONObject response = sendPostRequest(endpoint, requestBody);
        
        if ((boolean) response.get("success")) {
            gameStarted = true;
            JSONObject data = (JSONObject) response.get("data");
            currentPlayerIndex = ((Long) data.get("currentPlayerIndex")).intValue();
            
            JSONObject initialMovie = (JSONObject) data.get("initialMovie");
            lastMovieTitle = (String) initialMovie.get("title");
            
            System.out.println("Game started successfully!");
            System.out.println("Initial movie: " + lastMovieTitle);
            System.out.println("Current player: " + getCurrentPlayerName());
        } else {
            System.out.println("Failed to start game: " + response.get("message"));
        }
    }
    
    private static void getGameStatus() throws IOException, ParseException {
        if (!gameStarted) {
            System.out.println("Game not started yet. Please start the game first (option 2)");
            return;
        }
        
        System.out.println("\n=== Game Status ===");
        
        String endpoint = "/game/status";
        JSONObject response = sendGetRequest(endpoint);
        
        if ((boolean) response.get("success")) {
            JSONObject data = (JSONObject) response.get("data");
            boolean gameOver = (boolean) data.get("gameOver");
            currentPlayerIndex = ((Long) data.get("currentPlayerIndex")).intValue();
            
            System.out.println("Game is " + (gameOver ? "over" : "in progress"));
            System.out.println("Current player: " + getCurrentPlayerName());
            
            if (data.containsKey("lastMovie")) {
                JSONObject lastMovie = (JSONObject) data.get("lastMovie");
                lastMovieTitle = (String) lastMovie.get("title");
                System.out.println("Last movie: " + lastMovieTitle);
            }
            
            if (gameOver && data.containsKey("winner")) {
                System.out.println("Winner: " + data.get("winner"));
            }
            
            // Check time
            JSONObject timeResponse = sendGetRequest("/game/check-time");
            if ((boolean) timeResponse.get("success")) {
                JSONObject timeData = (JSONObject) timeResponse.get("data");
                long remainingTurnTime = (Long) timeData.get("remainingTurnTime");
                System.out.println("Remaining turn time: " + (remainingTurnTime / 1000) + " seconds");
            }
        } else {
            System.out.println("Failed to get game status: " + response.get("message"));
        }
    }
    
    private static void searchAndSelectMovie() throws IOException, ParseException {
        if (!gameStarted) {
            System.out.println("Game not started yet. Please start the game first (option 2)");
            return;
        }
        
        System.out.println("\n=== Search and Select Movie ===");
        System.out.println("Last movie: " + lastMovieTitle);
        System.out.println("Current player: " + getCurrentPlayerName());
        
        System.out.print("Enter search term: ");
        String searchTerm = scanner.nextLine();
        
        if (searchTerm.isEmpty()) {
            System.out.println("Search term cannot be empty");
            return;
        }
        
        String endpoint = "/movies/search?term=" + searchTerm + "&limit=10";
        JSONObject response = sendGetRequest(endpoint);
        
        if ((boolean) response.get("success")) {
            JSONObject data = (JSONObject) response.get("data");
            JSONArray movies = (JSONArray) data.get("movies");
            
            if (movies.isEmpty()) {
                System.out.println("No movies found matching '" + searchTerm + "'");
                return;
            }
            
            System.out.println("\nSearch results:");
            for (int i = 0; i < movies.size(); i++) {
                JSONObject movie = (JSONObject) movies.get(i);
                System.out.printf("%d. %s (%d)\n", i + 1, movie.get("title"), movie.get("year"));
            }
            
            System.out.print("\nSelect a movie (1-" + movies.size() + ") or 0 to cancel: ");
            int selection = getUserChoice(0, movies.size());
            
            if (selection == 0) {
                System.out.println("Selection cancelled");
                return;
            }
            
            JSONObject selectedMovie = (JSONObject) movies.get(selection - 1);
            int movieId = ((Long) selectedMovie.get("id")).intValue();
            
            // Select the movie
            endpoint = "/movies/select?id=" + movieId;
            JSONObject selectResponse = sendPostRequest(endpoint, "");
            
            if ((boolean) selectResponse.get("success")) {
                System.out.println("Movie selected successfully: " + selectedMovie.get("title"));
                lastMovieTitle = (String) selectedMovie.get("title");
            } else {
                System.out.println("Failed to select movie: " + selectResponse.get("message"));
            }
        } else {
            System.out.println("Failed to search movies: " + response.get("message"));
        }
    }
    
    private static void useAbility() throws IOException, ParseException {
        if (!gameStarted) {
            System.out.println("Game not started yet. Please start the game first (option 2)");
            return;
        }
        
        System.out.println("\n=== Use Ability ===");
        System.out.println("Current player: " + getCurrentPlayerName());
        
        System.out.println("1. Skip (skip opponent's next turn)");
        System.out.println("2. Block (block opponent's next move)");
        System.out.println("3. Cancel");
        System.out.print("Choose ability: ");
        
        int choice = getUserChoice(1, 3);
        
        if (choice == 3) {
            System.out.println("Cancelled");
            return;
        }
        
        String endpoint = choice == 1 ? "/actions/skip" : "/actions/block";
        JSONObject response = sendPostRequest(endpoint, "");
        
        if ((boolean) response.get("success")) {
            System.out.println("Ability used successfully: " + (choice == 1 ? "Skip" : "Block"));
        } else {
            System.out.println("Failed to use ability: " + response.get("message"));
        }
    }
    
    private static void nextPlayer() throws IOException, ParseException {
        if (!gameStarted) {
            System.out.println("Game not started yet. Please start the game first (option 2)");
            return;
        }
        
        System.out.println("\n=== Next Player ===");
        
        String endpoint = "/actions/next";
        JSONObject response = sendPostRequest(endpoint, "");
        
        if ((boolean) response.get("success")) {
            JSONObject data = (JSONObject) response.get("data");
            currentPlayerIndex = ((Long) data.get("currentPlayerIndex")).intValue();
            
            System.out.println("Switched to next player: " + getCurrentPlayerName());
        } else {
            System.out.println("Failed to switch to next player: " + response.get("message"));
        }
    }
    
    private static String getCurrentPlayerName() {
        return currentPlayerIndex == 0 ? player1Name : player2Name;
    }
    
    private static JSONObject sendGetRequest(String endpoint) throws IOException, ParseException {
        URL url = new URL(API_BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        return handleResponse(connection);
    }
    
    private static JSONObject sendPostRequest(String endpoint, String body) throws IOException, ParseException {
        URL url = new URL(API_BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        
        if (body != null && !body.isEmpty()) {
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = body.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
        }
        
        return handleResponse(connection);
    }
    
    private static JSONObject handleResponse(HttpURLConnection connection) throws IOException, ParseException {
        int responseCode = connection.getResponseCode();
        
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            return (JSONObject) jsonParser.parse(response.toString());
        } else {
            throw new IOException("HTTP error code: " + responseCode);
        }
    }
}
