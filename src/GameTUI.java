import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * A simple Text User Interface (TUI) for the Movie Connection Game
 */
@SuppressWarnings("unchecked")
public class GameTUI {
    private static final String API_BASE_URL = "http://localhost:8080/api";
    private static final Scanner scanner = new Scanner(System.in);
    private static final JSONParser jsonParser = new JSONParser();
    
    // 倒计时状态变量
    private static int timerRemainingSeconds = 0;
    private static boolean timerActive = false;
    private static boolean timerExpired = false;
    private static boolean movieSelected = false;
    
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
        System.out.println("\n===== Movie Connection Game =====\n");
        
        // 如果倒计时活跃，显示当前剩余时间
        if (timerActive) {
            System.out.println("⏰ TIMER: " + timerRemainingSeconds + " seconds remaining");
        }
        
        System.out.println("1. Setup Game");
        System.out.println("2. Start Game");
        System.out.println("3. Get Game Status");
        System.out.println("4. Search and Select Movie");
        System.out.println("5. Use Ability (Skip/Block)");
        System.out.println("6. Next Player");
        System.out.println("7. Exit");
        System.out.print("\nEnter your choice (1-7): ");
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
        
        // Check response format, supporting two possible formats
        boolean isSuccess = false;
        JSONObject data = null;
        
        // Check new API response format: {"code":200,"message":"success","data":{...}}
        if (response.containsKey("code") && response.containsKey("message") && response.containsKey("data")) {
            Long code = (Long) response.get("code");
            isSuccess = (code != null && code == 200);
            if (isSuccess) {
                data = (JSONObject) response.get("data");
            }
        } 
        // Check old API response format: {"success":true,"data":{...}}
        else if (response.containsKey("success")) {
            isSuccess = (boolean) response.get("success");
            if (isSuccess && response.containsKey("data")) {
                data = (JSONObject) response.get("data");
            }
        }
        
        if (isSuccess && data != null) {
            gameStarted = true;
            currentPlayerIndex = ((Long) data.get("currentPlayerIndex")).intValue();
            
            // Display initial movie information
            if (data.containsKey("initialMovie")) {
                JSONObject initialMovie = (JSONObject) data.get("initialMovie");
                lastMovieTitle = (String) initialMovie.get("title");
                
                System.out.println("Game started successfully!");
                System.out.println("Initial movie: " + lastMovieTitle);
                System.out.println("Genre: " + initialMovie.get("genre"));
                System.out.println("Release Year: " + initialMovie.get("releaseYear"));
                System.out.println("Current player: " + getCurrentPlayerName());
            } else {
                System.out.println("Game started, but no initial movie was provided.");
            }
        } else {
            String errorMessage = "Unknown error";
            if (response.containsKey("message")) {
                errorMessage = (String) response.get("message");
            }
            System.out.println("Error: " + errorMessage);
            
            // Display more detailed error if available
            if (response.containsKey("error")) {
                System.out.println("Error details: " + response.get("error"));
            }
        }
    }
    
    private static void getGameStatus() throws IOException, ParseException {
        if (!gameStarted) {
            System.out.println("Game not started yet. Please setup the game first (option 1)");
            return;
        }
        
        System.out.println("\n=== Game Status ===");
        
        String endpoint = "/game/status";
        JSONObject response = sendGetRequest(endpoint);
        
        // Process response
        boolean isSuccess = false;
        JSONObject data = null;
        
        // Check new API response format
        if (response.containsKey("code") && response.containsKey("message") && response.containsKey("data")) {
            Long code = (Long) response.get("code");
            isSuccess = (code != null && code == 200);
            if (isSuccess) {
                data = (JSONObject) response.get("data");
            }
        } 
        // Check old API response format
        else if (response.containsKey("success")) {
            isSuccess = (boolean) response.get("success");
            if (isSuccess && response.containsKey("data")) {
                data = (JSONObject) response.get("data");
            }
        }
        
        if (isSuccess && data != null) {
            boolean gameOver = (boolean) data.get("gameOver");
            currentPlayerIndex = ((Long) data.get("currentPlayerIndex")).intValue();
            int turnCount = data.containsKey("turnCount") ? ((Long) data.get("turnCount")).intValue() : 0;
            
            // Game status information
            System.out.println("\n──────────────────────────────────────────");
            System.out.println("Game Status: " + (gameOver ? "GAME OVER" : "IN PROGRESS"));
            System.out.println("Turn Count: " + turnCount);
            System.out.println("Current Player: " + getCurrentPlayerName());
            
            // Last movie information
            if (data.containsKey("lastMovie")) {
                JSONObject lastMovie = (JSONObject) data.get("lastMovie");
                lastMovieTitle = (String) lastMovie.get("title");
                System.out.println("\nLast Movie: " + lastMovieTitle);
                System.out.println("Genre: " + lastMovie.get("genre"));
                System.out.println("Release Year: " + lastMovie.get("releaseYear"));
            }
            
            // Player information
            if (data.containsKey("players")) {
                JSONArray players = (JSONArray) data.get("players");
                System.out.println("\n──────────────────────────────────────────");
                System.out.println("PLAYER PROGRESS");
                
                for (int i = 0; i < players.size(); i++) {
                    JSONObject player = (JSONObject) players.get(i);
                    String playerName = (String) player.get("name");
                    String targetGenre = (String) player.get("targetGenre");
                    int targetGenreCount = ((Long) player.get("targetGenreCount")).intValue();
                    int winThreshold = ((Long) player.get("winThreshold")).intValue();
                    boolean skipAvailable = (boolean) player.get("skipAvailable");
                    boolean blockAvailable = (boolean) player.get("blockAvailable");
                    
                    System.out.println("\nPlayer: " + playerName + (i == currentPlayerIndex ? " (Current)" : ""));
                    System.out.println("Target Genre: " + targetGenre);
                    System.out.println("Progress: " + targetGenreCount + "/" + winThreshold + " movies");
                    
                    // Calculate progress percentage
                    int progressPercentage = (int) ((double) targetGenreCount / winThreshold * 100);
                    int filledBlocks = progressPercentage / 10;
                    int emptyBlocks = 10 - filledBlocks;
                    
                    StringBuilder progressBar = new StringBuilder("[");
                    for (int k = 0; k < filledBlocks; k++) {
                        progressBar.append("■"); // Filled block
                    }
                    for (int k = 0; k < emptyBlocks; k++) {
                        progressBar.append("□"); // Empty block
                    }
                    progressBar.append("] ").append(progressPercentage).append("%");
                    System.out.println(progressBar.toString());
                    
                    System.out.println("Abilities: " + 
                                   (skipAvailable ? "Skip Available" : "Skip Used") + ", " + 
                                   (blockAvailable ? "Block Available" : "Block Used"));
                    
                    // Display player's movie list
                    if (player.containsKey("movies")) {
                        JSONArray movies = (JSONArray) player.get("movies");
                        if (!movies.isEmpty()) {
                            System.out.println("\nSelected Movies:");
                            for (int j = 0; j < movies.size(); j++) {
                                JSONObject movie = (JSONObject) movies.get(j);
                                System.out.printf("  %d. %s (%d)\n", j + 1, movie.get("title"), movie.get("releaseYear"));
                            }
                        }
                    }
                }
            }
            
            if (gameOver && data.containsKey("winner")) {
                System.out.println("\n──────────────────────────────────────────");
                System.out.println("WINNER: " + data.get("winner"));
            }
            
            // Check time
            JSONObject timeResponse = sendGetRequest("/game/check-time");
            boolean timeSuccess = false;
            JSONObject timeData = null;
            
            // Process time check response
            if (timeResponse.containsKey("code") && timeResponse.containsKey("data")) {
                Long code = (Long) timeResponse.get("code");
                timeSuccess = (code != null && code == 200);
                if (timeSuccess) {
                    timeData = (JSONObject) timeResponse.get("data");
                }
            } else if (timeResponse.containsKey("success")) {
                timeSuccess = (boolean) timeResponse.get("success");
                if (timeSuccess && timeResponse.containsKey("data")) {
                    timeData = (JSONObject) timeResponse.get("data");
                }
            }
            
            System.out.println("\n──────────────────────────────────────────");
        } else {
            String errorMessage = "Unknown error";
            if (response.containsKey("message")) {
                errorMessage = (String) response.get("message");
            }
            System.out.println("Failed to get game status: " + errorMessage);
        }
    }
    
    private static void searchAndSelectMovie() throws IOException, ParseException {
        if (!gameStarted) {
            System.out.println("Game not started yet. Please setup the game first (option 1)");
            return;
        }
        
        System.out.println("\n=== Search and Select Movie ===");
        
        // 初始化倒计时状态
        timerRemainingSeconds = 30;
        timerActive = true;
        timerExpired = false;
        movieSelected = false;
        
        System.out.println("⏰ 30-SECOND COUNTDOWN STARTED! ⏰");
        System.out.println("⏰ Time remaining: 30 seconds");
        
        // 启动倒计时线程 - 在控制台显示倒计时，但不干扰用户输入
        Thread timerThread = new Thread(() -> {
            try {
                // 创建一个专用于倒计时显示的区域
                System.out.println("\n--- COUNTDOWN TIMER STARTED ---");
                
                while (timerRemainingSeconds > 0 && !movieSelected) {
                    // 清晰地显示当前倒计时状态
                    System.out.println("\n⏰ COUNTDOWN: " + timerRemainingSeconds + " seconds remaining");
                    
                    // 特殊警告
                    if (timerRemainingSeconds == 10) {
                        System.out.println("⚠️ WARNING: Only 10 seconds remaining! ⚠️");
                    }
                    
                    // 等待一秒
                    Thread.sleep(1000);
                    timerRemainingSeconds--;
                    
                    // 时间到期处理
                    if (timerRemainingSeconds <= 0) {
                        timerExpired = true;
                        timerActive = false;
                        
                        // 显示时间到期信息
                        System.out.println("\n⏰ TIME'S UP! ⏰");
                        System.out.println("You ran out of time to select a movie.");
                        System.out.println("Your opponent wins!");
                        
                        // 获取对手名称
                        String opponentName = (currentPlayerIndex == 0) ? player2Name : player1Name;
                        System.out.println("Winner: " + opponentName);
                        
                        // 重置游戏状态
                        gameStarted = false;
                        currentPlayerIndex = 0;
                        lastMovieTitle = "";
                        
                        System.out.println("\nPlease start a new game (option 2).");
                        break;
                    }
                }
                
                // 倒计时结束（正常或被中断）
                if (!timerExpired && movieSelected) {
                    System.out.println("\n--- COUNTDOWN STOPPED ---");
                }
                
            } catch (InterruptedException e) {
                // 线程被中断
            }
        });
        
        timerThread.setDaemon(true); // 设置为守护线程
        timerThread.start();
        
        // Get game status to check if current player is blocked or has already selected a movie
        String statusEndpoint = "/game/status";
        JSONObject statusResponse = sendGetRequest(statusEndpoint);
        
        // Process status response
        boolean isStatusSuccess = false;
        JSONObject statusData = null;
        
        // Check new API response format
        if (statusResponse.containsKey("code") && statusResponse.containsKey("data")) {
            Long code = (Long) statusResponse.get("code");
            isStatusSuccess = (code != null && code == 200);
            if (isStatusSuccess) {
                statusData = (JSONObject) statusResponse.get("data");
            }
        } 
        // Check old API response format
        else if (statusResponse.containsKey("success")) {
            isStatusSuccess = (boolean) statusResponse.get("success");
            if (isStatusSuccess && statusResponse.containsKey("data")) {
                statusData = (JSONObject) statusResponse.get("data");
            }
        }
        
        if (isStatusSuccess && statusData != null) {
            boolean gameOver = (boolean) statusData.get("gameOver");
            int turnCount = statusData.containsKey("turnCount") ? ((Long) statusData.get("turnCount")).intValue() : 0;
            
            if (gameOver) {
                System.out.println("\n──────────────────────────────────────────");
                System.out.println("Game is already over. Cannot select a movie.");
                if (statusData.containsKey("winner")) {
                    System.out.println("Winner: " + statusData.get("winner"));
                }
                System.out.println("──────────────────────────────────────────");
                return;
            }
            
            // Display game status information
            System.out.println("\n──────────────────────────────────────────");
            System.out.println("GAME INFO");
            System.out.println("Turn Count: " + turnCount);
            
            // Display last movie information
            if (statusData.containsKey("lastMovie")) {
                JSONObject lastMovie = (JSONObject) statusData.get("lastMovie");
                lastMovieTitle = (String) lastMovie.get("title");
                System.out.println("\nLast Movie: " + lastMovieTitle);
                System.out.println("Genre: " + lastMovie.get("genre"));
                System.out.println("Release Year: " + lastMovie.get("releaseYear"));
            }
            
            // Get current player info
            JSONArray players = (JSONArray) statusData.get("players");
            JSONObject currentPlayer = (JSONObject) players.get(currentPlayerIndex);
            
            // Check if player is blocked
            if (currentPlayer.containsKey("isBlocked") && (boolean) currentPlayer.get("isBlocked")) {
                System.out.println("\n──────────────────────────────────────────");
                System.out.println("\u26a0\ufe0f You are blocked for this turn and cannot select a movie.");
                System.out.println("Please use 'Next Player' option to continue.");
                System.out.println("──────────────────────────────────────────");
                return;
            }
            
            // Display current player information
            String playerName = (String) currentPlayer.get("name");
            String targetGenre = (String) currentPlayer.get("targetGenre");
            int targetGenreCount = ((Long) currentPlayer.get("targetGenreCount")).intValue();
            int winThreshold = ((Long) currentPlayer.get("winThreshold")).intValue();
            boolean skipAvailable = (boolean) currentPlayer.get("skipAvailable");
            boolean blockAvailable = (boolean) currentPlayer.get("blockAvailable");
            
            System.out.println("\n──────────────────────────────────────────");
            System.out.println("CURRENT PLAYER: " + playerName);
            System.out.println("Target Genre: " + targetGenre);
            System.out.println("Progress: " + targetGenreCount + "/" + winThreshold + " movies");
            
            // Calculate progress percentage
            int progressPercentage = (int) ((double) targetGenreCount / winThreshold * 100);
            int filledBlocks = progressPercentage / 10;
            int emptyBlocks = 10 - filledBlocks;
            
            StringBuilder progressBar = new StringBuilder("[");
            for (int k = 0; k < filledBlocks; k++) {
                progressBar.append("■"); // Filled block
            }
            for (int k = 0; k < emptyBlocks; k++) {
                progressBar.append("□"); // Empty block
            }
            progressBar.append("] ").append(progressPercentage).append("%");
            System.out.println(progressBar.toString());
            
            System.out.println("Abilities: " + 
                           (skipAvailable ? "Skip Available" : "Skip Used") + ", " + 
                           (blockAvailable ? "Block Available" : "Block Used"));
            
            // Display player's movie list
            if (currentPlayer.containsKey("movies")) {
                JSONArray movies = (JSONArray) currentPlayer.get("movies");
                if (!movies.isEmpty()) {
                    System.out.println("\nYour Selected Movies:");
                    for (int j = 0; j < movies.size(); j++) {
                        JSONObject movie = (JSONObject) movies.get(j);
                        System.out.printf("  %d. %s (%d)\n", j + 1, movie.get("title"), movie.get("releaseYear"));
                    }
                }
            }
            System.out.println("──────────────────────────────────────────");
        } else {
            String errorMessage = "Unknown error";
            if (statusResponse.containsKey("message")) {
                errorMessage = (String) statusResponse.get("message");
            }
            System.out.println("Failed to get game status: " + errorMessage);
            return;
        }
        
        // 检查倒计时是否已过期
        if (timerExpired) {
            return; // 如果倒计时已过期，立即返回
        }
        
        // 不再在这里显示剩余时间，因为倒计时线程会定期显示
        
        System.out.print("Enter search term: ");
        // 使用BufferedReader代替Scanner.nextLine()，避免被其他线程干扰
        String searchTerm = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            searchTerm = reader.readLine();
        } catch (IOException e) {
            System.out.println("Error reading input: " + e.getMessage());
            return;
        }
        
        if (searchTerm.isEmpty()) {
            System.out.println("Search term cannot be empty");
            return;
        }
        
        // 再次检查倒计时是否已过期（输入后）
        if (timerExpired) {
            System.out.println("Sorry, time expired while you were typing.");
            return;
        }
        
        String endpoint = "/movies/search?term=" + searchTerm + "&limit=10";
        JSONObject response = sendGetRequest(endpoint);
        
        // Process search response
        boolean isSuccess = false;
        JSONObject data = null;
        
        // Check new API response format
        if (response.containsKey("code") && response.containsKey("data")) {
            Long code = (Long) response.get("code");
            isSuccess = (code != null && code == 200);
            if (isSuccess) {
                data = (JSONObject) response.get("data");
            }
        } 
        // Check old API response format
        else if (response.containsKey("success")) {
            isSuccess = (boolean) response.get("success");
            if (isSuccess && response.containsKey("data")) {
                data = (JSONObject) response.get("data");
            }
        }
        
        if (isSuccess && data != null) {
            // Stop timer as soon as search is successful
            timerActive = false;
            movieSelected = true;
            System.out.println("⏰ Timer stopped - search completed!");
            
            JSONArray movies = (JSONArray) data.get("movies");
            if (movies.isEmpty()) {
                System.out.println("No movies found matching '" + searchTerm + "'");
                System.out.println("No movies found. You can still use your abilities or pass to the next player.");
                System.out.println("Return to the main menu to select your next action.");
                return;
            }
            
            System.out.println("\nSearch results:");
            for (int i = 0; i < movies.size(); i++) {
                JSONObject movie = (JSONObject) movies.get(i);
                System.out.printf("%d. %s (%d)\n", i + 1, movie.get("title"), movie.get("releaseYear"));
                
                // Display genres if available
                if (movie.containsKey("genre")) {
                    System.out.println("   Genres: " + movie.get("genre"));
                }
            }
            
            System.out.print("\nSelect a movie (1-" + movies.size() + ") or 0 to cancel: ");
            int selection = getUserChoice(0, movies.size());
            
            if (selection == 0) {
                System.out.println("Selection cancelled");
                return;
            }
            
            JSONObject selectedMovie = (JSONObject) movies.get(selection - 1);
            int movieId = ((Long) selectedMovie.get("id")).intValue();
            
            // Check if countdown has expired
            if (timerExpired) {
                return; // If countdown has expired, return immediately
            }
            
            // 标记电影已选择，停止倒计时
            movieSelected = true;
            timerActive = false;
            System.out.println("⏰ Timer stopped - movie selected successfully!");
            
            // Select the movie
            endpoint = "/movies/select?id=" + movieId;
            JSONObject selectResponse = sendPostRequest(endpoint, "");
            
            // Process movie selection response
            boolean isSelectSuccess = false;
            
            // Check new API response format
            if (selectResponse.containsKey("code")) {
                Long code = (Long) selectResponse.get("code");
                isSelectSuccess = (code != null && code == 200);
            } 
            // Check old API response format
            else if (selectResponse.containsKey("success")) {
                isSelectSuccess = (boolean) selectResponse.get("success");
            }
            
            if (isSelectSuccess) {
                System.out.println("Movie selected successfully: " + selectedMovie.get("title"));
                lastMovieTitle = (String) selectedMovie.get("title");
                
                // Check if the game is over after this selection
                getGameStatus();
            } else {
                String errorMessage = "Unknown error";
                if (selectResponse.containsKey("message")) {
                    errorMessage = (String) selectResponse.get("message");
                }
                System.out.println("Failed to select movie: " + errorMessage);
                
                // Display more detailed error if available
                if (selectResponse.containsKey("error")) {
                    System.out.println("Error details: " + selectResponse.get("error"));
                }
            }
        } else {
            String errorMessage = "Unknown error";
            if (response.containsKey("message")) {
                errorMessage = (String) response.get("message");
            }
            System.out.println("Failed to search movies: " + errorMessage);
        }
    }
    
    private static void useAbility() throws IOException, ParseException {
        if (!gameStarted) {
            System.out.println("Game not started yet. Please start the game first (option 2)");
            return;
        }
        
        // Get game status first to check player state
        String statusEndpoint = "/game/status";
        JSONObject statusResponse = sendGetRequest(statusEndpoint);
        
        // Process status response
        boolean isStatusSuccess = false;
        JSONObject statusData = null;
        
        // Check new API response format
        if (statusResponse.containsKey("code") && statusResponse.containsKey("data")) {
            Long code = (Long) statusResponse.get("code");
            isStatusSuccess = (code != null && code == 200);
            if (isStatusSuccess) {
                statusData = (JSONObject) statusResponse.get("data");
            }
        } 
        // Check old API response format
        else if (statusResponse.containsKey("success")) {
            isStatusSuccess = (boolean) statusResponse.get("success");
            if (isStatusSuccess && statusResponse.containsKey("data")) {
                statusData = (JSONObject) statusResponse.get("data");
            }
        }
        
        if (isStatusSuccess && statusData != null) {
            boolean gameOver = (boolean) statusData.get("gameOver");
            
            if (gameOver) {
                System.out.println("Game is already over. Cannot use abilities.");
                if (statusData.containsKey("winner")) {
                    System.out.println("Winner: " + statusData.get("winner"));
                }
                return;
            }
            
            // Get current player info
            JSONArray players = (JSONArray) statusData.get("players");
            JSONObject currentPlayer = (JSONObject) players.get(currentPlayerIndex);
            
            // Check if player is blocked
            if (currentPlayer.containsKey("isBlocked") && (boolean) currentPlayer.get("isBlocked")) {
                System.out.println("You are blocked for this turn and cannot use abilities.");
                return;
            }
        } else {
            String errorMessage = "Unknown error";
            if (statusResponse.containsKey("message")) {
                errorMessage = (String) statusResponse.get("message");
            }
            System.out.println("Failed to get game status: " + errorMessage);
            return;
        }
        
        System.out.println("\n=== Use Ability（Only One Ability per Game)===");
        System.out.println("Current player: " + getCurrentPlayerName());
        
        // Get current player info from status data
        JSONArray players = (JSONArray) statusData.get("players");
        JSONObject currentPlayer = (JSONObject) players.get(currentPlayerIndex);
        
        // Check available abilities
        boolean skipAvailable = (boolean) currentPlayer.get("skipAvailable");
        boolean blockAvailable = (boolean) currentPlayer.get("blockAvailable");
        
        if (!skipAvailable && !blockAvailable) {
            System.out.println("You have already used your special abilities.");
            return;
        }
        
        System.out.println(skipAvailable ? "1. Skip (skip opponent's next turn)" : "1. Skip (not available)");
        System.out.println(blockAvailable ? "2. Block (block opponent's next move)" : "2. Block (not available)");
        System.out.println("3. Cancel");
        System.out.print("Choose ability: ");
        
        int choice = getUserChoice(1, 3);
        
        if (choice == 3) {
            System.out.println("Cancelled");
            return;
        }
        
        // Check if selected ability is available
        if ((choice == 1 && !skipAvailable) || (choice == 2 && !blockAvailable)) {
            System.out.println("This ability is not available.");
            return;
        }
        
        String endpoint = choice == 1 ? "/actions/skip" : "/actions/block";
        JSONObject response = sendPostRequest(endpoint, "");
        
        // Process ability usage response
        boolean isSuccess = false;
        
        // Check new API response format
        if (response.containsKey("code")) {
            Long code = (Long) response.get("code");
            isSuccess = (code != null && code == 200);
        } 
        // Check old API response format
        else if (response.containsKey("success")) {
            isSuccess = (boolean) response.get("success");
        }
        
        if (isSuccess) {
            System.out.println("Ability used successfully: " + (choice == 1 ? "Skip" : "Block"));
            System.out.println("Remember to end your turn by selecting 'Next Player' from the menu.");
        } else {
            String errorMessage = "Unknown error";
            if (response.containsKey("message")) {
                errorMessage = (String) response.get("message");
            }
            System.out.println("Failed to use ability: " + errorMessage);
            
            // Display more detailed error if available
            if (response.containsKey("error")) {
                System.out.println("Error details: " + response.get("error"));
            }
        }
    }
    
    private static void nextPlayer() throws IOException, ParseException {
        if (!gameStarted) {
            System.out.println("Game not started yet. Please start the game first (option 2)");
            return;
        }
        
        // Get game status first to check if game is over
        String statusEndpoint = "/game/status";
        JSONObject statusResponse = sendGetRequest(statusEndpoint);
        
        // Process status response
        boolean isStatusSuccess = false;
        JSONObject statusData = null;
        
        // Check new API response format
        if (statusResponse.containsKey("code") && statusResponse.containsKey("data")) {
            Long code = (Long) statusResponse.get("code");
            isStatusSuccess = (code != null && code == 200);
            if (isStatusSuccess) {
                statusData = (JSONObject) statusResponse.get("data");
            }
        } 
        // Check old API response format
        else if (statusResponse.containsKey("success")) {
            isStatusSuccess = (boolean) statusResponse.get("success");
            if (isStatusSuccess && statusResponse.containsKey("data")) {
                statusData = (JSONObject) statusResponse.get("data");
            }
        }
        
        if (isStatusSuccess && statusData != null) {
            boolean gameOver = (boolean) statusData.get("gameOver");
            
            if (gameOver) {
                System.out.println("Game is already over. Cannot switch to next player.");
                if (statusData.containsKey("winner")) {
                    System.out.println("Winner: " + statusData.get("winner"));
                }
                return;
            }
        } else {
            String errorMessage = "Unknown error";
            if (statusResponse.containsKey("message")) {
                errorMessage = (String) statusResponse.get("message");
            }
            System.out.println("Failed to get game status: " + errorMessage);
            return;
        }
        
        System.out.println("\n=== Next Player ===");
        
        String endpoint = "/actions/next";
        JSONObject response = sendPostRequest(endpoint, "");
        
        // Process next player response
        boolean isSuccess = false;
        JSONObject data = null;
        
        // Check new API response format
        if (response.containsKey("code") && response.containsKey("data")) {
            Long code = (Long) response.get("code");
            isSuccess = (code != null && code == 200);
            if (isSuccess) {
                data = (JSONObject) response.get("data");
            }
        } 
        // Check old API response format
        else if (response.containsKey("success")) {
            isSuccess = (boolean) response.get("success");
            if (isSuccess && response.containsKey("data")) {
                data = (JSONObject) response.get("data");
            }
        }
        
        if (isSuccess && data != null) {
            currentPlayerIndex = ((Long) data.get("currentPlayerIndex")).intValue();
            
            System.out.println("Switched to next player: " + getCurrentPlayerName());
            
            // Check if the next player is blocked or skipped
            if (data.containsKey("players")) {
                JSONArray players = (JSONArray) data.get("players");
                JSONObject currentPlayer = (JSONObject) players.get(currentPlayerIndex);
                
                if (currentPlayer.containsKey("isBlocked") && (boolean) currentPlayer.get("isBlocked")) {
                    System.out.println("Note: " + getCurrentPlayerName() + " is blocked for this turn.");
                    System.out.println("They can only use the 'Next Player' option.");
                } else if (currentPlayer.containsKey("isSkipped") && (boolean) currentPlayer.get("isSkipped")) {
                    System.out.println("Note: " + getCurrentPlayerName() + " is skipped for this turn.");
                    System.out.println("They can only use the 'Next Player' option.");
                }
            }
        } else {
            String errorMessage = "Unknown error";
            if (response.containsKey("message")) {
                errorMessage = (String) response.get("message");
            }
            System.out.println("Failed to switch to next player: " + errorMessage);
            
            // Display more detailed error if available
            if (response.containsKey("error")) {
                System.out.println("Error details: " + response.get("error"));
            }
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
        try {
            URL url = new URL(API_BASE_URL + endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000); // 5 seconds timeout
            connection.setReadTimeout(5000);    // 5 seconds timeout
            
            if (body != null && !body.isEmpty()) {
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = body.getBytes("utf-8");
                    os.write(input, 0, input.length);
                    os.flush();
                }
            }
            
            return handleResponse(connection);
        } catch (Exception e) {
            System.out.println("Error sending POST request: " + e.getMessage());
            e.printStackTrace();
            
            // Create an error JSON object
            JSONObject errorJson = new JSONObject();
            errorJson.put("success", Boolean.FALSE);
            errorJson.put("message", "Error sending request: " + e.getMessage());
            return errorJson;
        }
    }
    
    private static JSONObject handleResponse(HttpURLConnection connection) throws IOException, ParseException {
        int responseCode = connection.getResponseCode();
        
        // Read the response body regardless of status code
        InputStream stream = responseCode < 400 ? connection.getInputStream() : connection.getErrorStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        String inputLine;
        StringBuilder response = new StringBuilder();
        
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        
        String responseBody = response.toString();
        
        // Try to parse as JSON
        try {
            if (responseBody != null && !responseBody.isEmpty()) {
                return (JSONObject) jsonParser.parse(responseBody);
            } else {
                // Create a simple error JSON if response is empty
                JSONObject errorJson = new JSONObject();
                errorJson.put("success", Boolean.FALSE);
                errorJson.put("message", "Empty response from server");
                return errorJson;
            }
        } catch (ParseException e) {
            // If not valid JSON, create an error JSON object
            System.out.println("Failed to parse response as JSON: " + e.getMessage());
            JSONObject errorJson = new JSONObject();
            errorJson.put("success", Boolean.FALSE);
            errorJson.put("message", "Invalid JSON response: " + responseBody);
            return errorJson;
        }
    }
}
