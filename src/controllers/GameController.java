package controllers;
/*
 * test for GameController.java
 */
import api.responses.ApiResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import factories.ClientFactory;
import factories.ServiceFactory;
import models.Client;
import models.Movie;
import models.Autocomplete;
import services.GameService;
import services.MovieService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Game controller, handles game logic and implements HTTP handler functionality
 */
public class GameController implements HttpHandler {
    private final GameService gameService;
    private final MovieService movieService;
    private final Autocomplete autocomplete;
    
    /**
     * Constructor
     */
    public GameController() {
        this.gameService = ServiceFactory.getGameService();
        this.movieService = ServiceFactory.getMovieService();
        this.autocomplete = new Autocomplete();
        
        // Initialize autocomplete with all movies
        List<Movie> allMovies = movieService.getAllMovies();
        System.out.println("Initializing autocomplete with " + allMovies.size() + " movies");
        for (Movie movie : allMovies) {
            autocomplete.insert(movie);
        }
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        // Remove /api prefix, as HttpServer has already handled this part
        if (path.startsWith("/api")) {
            path = path.substring(4);
        }
        String method = exchange.getRequestMethod();
        String query = exchange.getRequestURI().getQuery();
        
        try {
            // Handle preflight requests
            if (method.equals("OPTIONS")) {
                handleOptionsRequest(exchange);
                return;
            }
            
            // Use path and method combination as key
            String key = method + " " + path;
            System.out.println("Route key: " + key);
            
            switch (key) {
                case "POST /game/start":
                    handleStartGame(exchange);
                    break;
                case "GET /game/status":
                    handleGetGameStatus(exchange);
                    break;
                case "GET /movies/search":
                    handleSearchMovies(exchange, query);
                    break;
                case "POST /movies/select":
                    handleSelectMovie(exchange, query);
                    break;
                case "POST /actions/skip":
                    handleSkipAction(exchange);
                    break;
                case "POST /actions/block":
                    handleBlockAction(exchange);
                    break;
                case "POST /actions/next":
                    handleNextPlayer(exchange);
                    break;
                case "GET /game/check-time":
                    handleCheckTime(exchange);
                    break;
                default:
                    sendResponse(exchange, ApiResponse.error(404, "Not Found"), 404);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, ApiResponse.error(500, e.getMessage()), 500);
        }
    }
    
    /**
     * Handle preflight requests
     */
    private void handleOptionsRequest(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(204, -1);
    }
    
    /**
     * Handle start game request
     */
    private void handleStartGame(HttpExchange exchange) throws IOException {
        System.out.println("Handling start game request");
        
        // Parse request body
        String requestBody = readRequestBody(exchange);
        System.out.println("Request body content: " + requestBody);
        
        Map<String, String> params = parseRequestBody(requestBody);
        System.out.println("Parsed parameters: " + params);
        
        // Get parameters
        String player1Name = params.getOrDefault("player1Name", "Player1");
        String player1Genre = params.getOrDefault("player1Genre", "sci-fi");
        String player2Name = params.getOrDefault("player2Name", "Player2");
        String player2Genre = params.getOrDefault("player2Genre", "action");
        int winThreshold = Integer.parseInt(params.getOrDefault("winThreshold", "3"));
        
        // Create players
        Client player1 = ClientFactory.createClient(player1Name, player1Genre, winThreshold);
        Client player2 = ClientFactory.createClient(player2Name, player2Genre, winThreshold);
        
        List<Client> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        
        // Initialize game
        gameService.initGame(players);
        
        // Get game status
        boolean gameOver = gameService.isGameOver();
        Client winner = gameService.getWinner();
        int turnCount = gameService.getTurnCount();
        int currentPlayerIndex = gameService.getCurrentPlayerIndex();
        
        // Build response data
        Map<String, Object> data = new HashMap<>();
        
        // Add player information
        List<Map<String, Object>> playersData = new ArrayList<>();
        for (Client player : players) {
            Map<String, Object> playerData = new HashMap<>();
            playerData.put("name", player.getName());
            playerData.put("targetGenre", player.getTargetGenre());
            playerData.put("winThreshold", player.getWinThreshold());
            playerData.put("skipAvailable", player.isSkipAvailable());
            playerData.put("blockAvailable", player.isBlockAvailable());
            
            // Add target genre movie count
            int targetGenreCount = player.getGenreCount().getOrDefault(player.getWinGenre().toLowerCase(), 0);
            playerData.put("targetGenreCount", targetGenreCount);
            
            // Add player's movies
            List<Map<String, Object>> moviesData = new ArrayList<>();
            for (Movie movie : player.getMovies()) {
                Map<String, Object> movieData = new HashMap<>();
                movieData.put("id", movie.getId());
                movieData.put("title", movie.getTitle());
                movieData.put("releaseYear", movie.getReleaseYear());
                movieData.put("genre", movie.getGenre());
                moviesData.add(movieData);
            }
            playerData.put("movies", moviesData);
            
            playersData.add(playerData);
        }
        data.put("players", playersData);
        data.put("currentPlayerIndex", currentPlayerIndex);
        data.put("turnCount", turnCount);
        data.put("gameOver", gameOver);
        
        if (winner != null) {
            data.put("winner", winner.getName());
        }
        
        sendResponse(exchange, ApiResponse.success(data), 200);
    }
    
    /**
     * Handle get game status request
     */
    private void handleGetGameStatus(HttpExchange exchange) throws IOException {
        System.out.println("Handling get game status request");
        
        // Get game status
        boolean gameOver = gameService.isGameOver();
        Client winner = gameService.getWinner();
        int turnCount = gameService.getTurnCount();
        int currentPlayerIndex = gameService.getCurrentPlayerIndex();
        
        // Build response data
        Map<String, Object> data = new HashMap<>();
        data.put("gameOver", gameOver);
        data.put("turnCount", turnCount);
        data.put("currentPlayerIndex", currentPlayerIndex);
        
        if (winner != null) {
            data.put("winner", winner.getName());
        }
        
        // Add player information
        List<Map<String, Object>> playersData = new ArrayList<>();
        for (Client player : gameService.getPlayers()) {
            Map<String, Object> playerData = new HashMap<>();
            playerData.put("name", player.getName());
            playerData.put("targetGenre", player.getTargetGenre());
            playerData.put("winThreshold", player.getWinThreshold());
            playerData.put("skipAvailable", player.isSkipAvailable());
            playerData.put("blockAvailable", player.isBlockAvailable());
            playerData.put("isSkipped", player.isSkipped());
            playerData.put("isBlocked", player.isBlocked());
            playerData.put("hasSelectedMovie", player.hasSelectedMovie());
            
            // Add target genre movie count
            int targetGenreCount = player.getGenreCount().getOrDefault(player.getWinGenre().toLowerCase(), 0);
            playerData.put("targetGenreCount", targetGenreCount);
            
            // Add player's movies
            List<Map<String, Object>> moviesData = new ArrayList<>();
            for (Movie movie : player.getMovies()) {
                Map<String, Object> movieData = new HashMap<>();
                movieData.put("id", movie.getId());
                movieData.put("title", movie.getTitle());
                movieData.put("releaseYear", movie.getReleaseYear());
                movieData.put("genre", movie.getGenre());
                moviesData.add(movieData);
            }
            playerData.put("movies", moviesData);
            
            playersData.add(playerData);
        }
        data.put("players", playersData);
        
        // Add last movie information
        Movie lastMovie = gameService.getLastMovie();
        if (lastMovie != null) {
            Map<String, Object> lastMovieData = new HashMap<>();
            lastMovieData.put("id", lastMovie.getId());
            lastMovieData.put("title", lastMovie.getTitle());
            lastMovieData.put("releaseYear", lastMovie.getReleaseYear());
            lastMovieData.put("genre", lastMovie.getGenre());
            data.put("lastMovie", lastMovieData);
        }
        
        sendResponse(exchange, ApiResponse.success(data), 200);
    }
    
    /**
     * Handle search movies request
     */
    private void handleSearchMovies(HttpExchange exchange, String query) throws IOException {
        // Parse query parameters
        Map<String, String> params = parseQueryParams(query);
        String searchTerm = params.getOrDefault("term", "");
        int limit = Integer.parseInt(params.getOrDefault("limit", "10"));
        
        System.out.println("Searching movies with term: " + searchTerm + ", limit: " + limit);
        
        // Use autocomplete to search movies
        List<Movie> movies = autocomplete.search(searchTerm, limit);
        
        System.out.println("Found " + movies.size() + " movies matching '" + searchTerm + "'");
        
        // Build response data
        List<Map<String, Object>> moviesData = new ArrayList<>();
        for (Movie movie : movies) {
            Map<String, Object> movieData = new HashMap<>();
            movieData.put("id", movie.getId());
            movieData.put("title", movie.getTitle());
            movieData.put("releaseYear", movie.getReleaseYear());
            movieData.put("genre", movie.getGenre());
            moviesData.add(movieData);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("movies", moviesData);
        
        sendResponse(exchange, ApiResponse.success(data), 200);
    }
    
    /**
     * Handle select movie request
     */
    private void handleSelectMovie(HttpExchange exchange, String query) throws IOException {
        // Parse query parameters
        Map<String, String> params = parseQueryParams(query);
        String movieIdStr = params.getOrDefault("id", "");
        
        if (movieIdStr.isEmpty()) {
            sendResponse(exchange, ApiResponse.error(400, "Missing movie ID"), 400);
            return;
        }
        
        try {
            int movieId = Integer.parseInt(movieIdStr);
            boolean success = gameService.processMovieSelection(movieId);
            
            if (success) {
                sendResponse(exchange, ApiResponse.success(null), 200);
            } else {
                sendResponse(exchange, ApiResponse.error(400, "Invalid movie selection"), 400);
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, ApiResponse.error(400, "Invalid movie ID format"), 400);
        }
    }
    
    /**
     * Handle skip action request
     */
    private void handleSkipAction(HttpExchange exchange) throws IOException {
        boolean success = gameService.useSkipPowerUp();
        
        if (success) {
            sendResponse(exchange, ApiResponse.success(null), 200);
        } else {
            sendResponse(exchange, ApiResponse.error(400, "Cannot use skip power-up"), 400);
        }
    }
    
    /**
     * Handle block action request
     */
    private void handleBlockAction(HttpExchange exchange) throws IOException {
        boolean success = gameService.useBlockPowerUp();
        
        if (success) {
            sendResponse(exchange, ApiResponse.success(null), 200);
        } else {
            sendResponse(exchange, ApiResponse.error(400, "Cannot use block power-up"), 400);
        }
    }
    
    /**
     * Handle next player request
     */
    private void handleNextPlayer(HttpExchange exchange) throws IOException {
        // Call the nextPlayer method in the game service
        gameService.nextPlayer();
        
        // Get updated game status
        boolean gameOver = gameService.isGameOver();
        Client winner = gameService.getWinner();
        int turnCount = gameService.getTurnCount();
        int currentPlayerIndex = gameService.getCurrentPlayerIndex();
        
        // Build response data
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("gameOver", gameOver);
        data.put("turnCount", turnCount);
        data.put("currentPlayerIndex", currentPlayerIndex);
        
        if (winner != null) {
            data.put("winner", winner.getName());
        }
        
        // Add player information
        List<Map<String, Object>> playersData = new ArrayList<>();
        for (Client player : gameService.getPlayers()) {
            Map<String, Object> playerData = new HashMap<>();
            playerData.put("name", player.getName());
            playerData.put("targetGenre", player.getTargetGenre());
            playerData.put("winThreshold", player.getWinThreshold());
            playerData.put("skipAvailable", player.isSkipAvailable());
            playerData.put("blockAvailable", player.isBlockAvailable());
            playerData.put("isSkipped", player.isSkipped());
            playerData.put("isBlocked", player.isBlocked());
            playerData.put("hasSelectedMovie", player.hasSelectedMovie());
            
            // Add target genre movie count
            int targetGenreCount = player.getGenreCount().getOrDefault(player.getWinGenre().toLowerCase(), 0);
            playerData.put("targetGenreCount", targetGenreCount);
            
            // Add player's movies
            List<Map<String, Object>> moviesData = new ArrayList<>();
            for (Movie movie : player.getMovies()) {
                Map<String, Object> movieData = new HashMap<>();
                movieData.put("id", movie.getId());
                movieData.put("title", movie.getTitle());
                movieData.put("releaseYear", movie.getReleaseYear());
                movieData.put("genre", movie.getGenre());
                moviesData.add(movieData);
            }
            playerData.put("movies", moviesData);
            
            playersData.add(playerData);
        }
        data.put("players", playersData);
        
        // Add last movie information
        Movie lastMovie = gameService.getLastMovie();
        if (lastMovie != null) {
            Map<String, Object> lastMovieData = new HashMap<>();
            lastMovieData.put("id", lastMovie.getId());
            lastMovieData.put("title", lastMovie.getTitle());
            lastMovieData.put("releaseYear", lastMovie.getReleaseYear());
            lastMovieData.put("genre", lastMovie.getGenre());
            data.put("lastMovie", lastMovieData);
        }
        
        sendResponse(exchange, ApiResponse.success(data), 200);
    }
    
    /**
     * Read request body
     */
    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        byte[] buffer = new byte[1024];
        int bytesRead;
        StringBuilder requestBody = new StringBuilder();
        
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            requestBody.append(new String(buffer, 0, bytesRead));
        }
        
        return requestBody.toString();
    }
    
    /**
     * Parse request body
     */
    private Map<String, String> parseRequestBody(String requestBody) {
        Map<String, String> params = new HashMap<>();
        
        if (requestBody == null || requestBody.isEmpty()) {
            return params;
        }
        
        // Check if it's JSON format
        if (requestBody.startsWith("{") && requestBody.endsWith("}")) {
            // Simple JSON parsing
            requestBody = requestBody.substring(1, requestBody.length() - 1);
            String[] pairs = requestBody.split(",");
            
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replace("\"", "");
                    String value = keyValue[1].trim().replace("\"", "");
                    params.put(key, value);
                }
            }
        } else {
            // Form data format
            String[] pairs = requestBody.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        
        return params;
    }
    
    /**
     * Parse query parameters
     */
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        
        if (query == null || query.isEmpty()) {
            return params;
        }
        
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }
        
        return params;
    }
    
    /**
     * Send response
     */
    private void sendResponse(HttpExchange exchange, ApiResponse response, int statusCode) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        
        String responseBody = response.toJson();
        byte[] responseBytes = responseBody.getBytes();
        
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.close();
    }
    
    /**
     * Handle check time request
     */
    private void handleCheckTime(HttpExchange exchange) throws IOException {
        boolean isTurnTimeOut = gameService.isTurnTimeOut();
        boolean isGameOver = gameService.isGameOver();
        
        // construct response data
        Map<String, Object> data = new HashMap<>();
        data.put("isTurnTimeOut", isTurnTimeOut);
        data.put("isGameOver", isGameOver);
        data.put("remainingTurnTime", gameService.getRemainingTurnTime());
        
        // If turn time out and game is not over yet, we need to handle it consistently with processMovieSelection
        // We'll call processMovieSelection with an invalid movie ID to trigger the timeout logic
        if (isTurnTimeOut && !isGameOver) {
            // This will trigger the timeout logic in processMovieSelection
            // which sets the opponent as winner and marks the game as over
            gameService.processMovieSelection(-1); // Using an invalid movie ID
            
            // Update our local variables after the game state has changed
            isGameOver = gameService.isGameOver();
            data.put("isGameOver", isGameOver);
        }
        
        // add player information
        List<Map<String, Object>> playersData = new ArrayList<>();
        for (Client player : gameService.getPlayers()) {
            Map<String, Object> playerData = new HashMap<>();
            playerData.put("name", player.getName());
            playersData.add(playerData);
        }
        data.put("players", playersData);
        
        // if game is over, add winner information
        if (isGameOver) {
            Client winner = gameService.getWinner();
            if (winner != null) {
                data.put("winner", winner.getName());
            }
        }
        
        sendResponse(exchange, ApiResponse.success(data), 200);
    }
}
