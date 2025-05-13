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
import services.ClientService;

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
    private final ClientService clientService;
    private final Autocomplete autocomplete;
    
    /**
     * Constructor
     */
    public GameController() {
        this.gameService = ServiceFactory.getGameService();
        this.movieService = ServiceFactory.getMovieService();
        this.clientService = ServiceFactory.getClientService();
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
        
        // Create a key in the format "METHOD PATH" for routing
        String routeKey = method + " " + path;
        
        try {
            switch (routeKey) {
                case "POST /game/start":
                    handleStartGame(exchange);
                    break;
                case "GET /game/status":
                    handleGetGameStatus(exchange);
                    break;
                case "GET /game/check-time":
                    handleCheckTime(exchange);
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
                default:
                    sendResponse(exchange, ApiResponse.error(404, "Not found"), 404);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, ApiResponse.error(500, "Internal server error: " + e.getMessage()), 500);
        }
    }
    
    /**
     * Handle start game request
     */
    private void handleStartGame(HttpExchange exchange) throws IOException {
        // Read request body
        InputStream is = exchange.getRequestBody();
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String requestBody = s.hasNext() ? s.next() : "";
        
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
        Movie initialMovie = gameService.initGame(players);
        
        // Check if a suitable initial movie was found
        if (initialMovie == null) {
            // No movie found that matches both players' genres
            String errorMessage = "No movie found that contains both players' target genres: '" + 
                                 player1Genre + "' and '" + player2Genre + "'. Please choose different genres.";
            sendResponse(exchange, ApiResponse.error(400, errorMessage), 400);
            return;
        }
        
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
        
        // Add initial movie information
        Map<String, Object> initialMovieData = new HashMap<>();
        initialMovieData.put("id", initialMovie.getId());
        initialMovieData.put("title", initialMovie.getTitle());
        initialMovieData.put("releaseYear", initialMovie.getReleaseYear());
        initialMovieData.put("genre", initialMovie.getGenre());
        data.put("initialMovie", initialMovieData);
        
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
        
        // Add last movie information from current player's collection
        Client currentPlayer = gameService.getCurrentPlayer();
        if (currentPlayer != null && !currentPlayer.getMovies().isEmpty()) {
            List<Movie> playerMovies = currentPlayer.getMovies();
            Movie lastMovie = playerMovies.get(playerMovies.size() - 1);
            
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
     * Handle check time request
     */
    private void handleCheckTime(HttpExchange exchange) throws IOException {
        System.out.println("Handling check time request");
        
        // Check if turn time is up
        boolean turnTimeOut = gameService.isTurnTimeOut();
        long remainingTurnTime = gameService.getRemainingTurnTime();
        boolean gameOver = gameService.isGameOver();
        
        // Build response data
        Map<String, Object> data = new HashMap<>();
        data.put("turnTimeOut", turnTimeOut);
        data.put("remainingTurnTime", remainingTurnTime);
        data.put("isGameOver", gameOver);
        
        if (gameOver) {
            Client winner = gameService.getWinner();
            if (winner != null) {
                data.put("winner", winner.getName());
            }
        }
        
        sendResponse(exchange, ApiResponse.success(data), 200);
    }
    
    /**
     * Handle search movies request
     */
    private void handleSearchMovies(HttpExchange exchange, String query) throws IOException {
        Map<String, String> params = parseQueryParams(query);
        String term = params.getOrDefault("term", "");
        int limit = Integer.parseInt(params.getOrDefault("limit", "10"));
        System.out.println("Searching for term: " + term + ", limit: " + limit);
        if (term.isEmpty()) {
            sendResponse(exchange, ApiResponse.error(400, "Missing search term"), 400);
            return;
        }
        
        List<Movie> movies = autocomplete.search(term, limit);
        
        List<Map<String, Object>> movieDataList = new ArrayList<>();
        for (Movie movie : movies) {
            Map<String, Object> movieData = new HashMap<>();
            movieData.put("id", movie.getId());
            movieData.put("title", movie.getTitle());
            movieData.put("releaseYear", movie.getReleaseYear());
            movieData.put("genre", movie.getGenre());
            movieDataList.add(movieData);
        }
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("movies", movieDataList);
        sendResponse(exchange, ApiResponse.success(responseData), 200);
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
        
        // Check if game is over
        if (gameService.isGameOver()) {
            sendResponse(exchange, ApiResponse.error(400, "Game is already over"), 400);
            return;
        }
        
        // Get current player
        Client currentPlayer = gameService.getCurrentPlayer();
        if (currentPlayer == null) {
            sendResponse(exchange, ApiResponse.error(500, "No current player found"), 500);
            return;
        }
        
        // Check if player is blocked
        if (currentPlayer.isBlocked()) {
            // Clear block status for next turn
            currentPlayer.clearBlock();
            sendResponse(exchange, ApiResponse.error(400, "You are blocked for this turn"), 400);
            return;
        }
        
        // Check if player already selected a movie this turn
        if (currentPlayer.hasSelectedMovie()) {
            sendResponse(exchange, ApiResponse.error(400, "You have already selected a movie this turn"), 400);
            return;
        }
        
        try {
            int movieId = Integer.parseInt(movieIdStr);
            
            // Check if movie has been used before
            if (currentPlayer.hasUsedMovie(movieId)) {
                sendResponse(exchange, ApiResponse.error(400, "This movie has already been used"), 400);
                return;
            }
            
            // Get the movie to check if it exists
            Movie movie = movieService.getMovieById(movieId);
            if (movie == null) {
                sendResponse(exchange, ApiResponse.error(400, "Movie not found"), 400);
                return;
            }
            
            // Process the selection
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
        // Check if game is over
        if (gameService.isGameOver()) {
            sendResponse(exchange, ApiResponse.error(400, "Game is already over"), 400);
            return;
        }
        
        // Get current player
        Client currentPlayer = gameService.getCurrentPlayer();
        if (currentPlayer == null) {
            sendResponse(exchange, ApiResponse.error(500, "No current player found"), 500);
            return;
        }
        
        // Check if player is blocked
        if (currentPlayer.isBlocked()) {
            sendResponse(exchange, ApiResponse.error(400, "You are blocked and cannot use abilities"), 400);
            return;
        }
        
        // Check if player has skip ability available
        if (!currentPlayer.isSkipAvailable()) {
            sendResponse(exchange, ApiResponse.error(400, "Skip ability already used"), 400);
            return;
        }
        
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
        // Check if game is over
        if (gameService.isGameOver()) {
            sendResponse(exchange, ApiResponse.error(400, "Game is already over"), 400);
            return;
        }
        
        // Get current player
        Client currentPlayer = gameService.getCurrentPlayer();
        if (currentPlayer == null) {
            sendResponse(exchange, ApiResponse.error(500, "No current player found"), 500);
            return;
        }
        
        // Check if player is blocked
        if (currentPlayer.isBlocked()) {
            sendResponse(exchange, ApiResponse.error(400, "You are blocked and cannot use abilities"), 400);
            return;
        }
        
        // Check if player has block ability available
        if (!currentPlayer.isBlockAvailable()) {
            sendResponse(exchange, ApiResponse.error(400, "Block ability already used"), 400);
            return;
        }
        
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
        gameService.nextPlayer();
        
        // Get updated game status
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
            
            playersData.add(playerData);
        }
        data.put("players", playersData);
        
        // Add last movie information from current player's collection
        Client currentPlayer = gameService.getCurrentPlayer();
        if (currentPlayer != null && !currentPlayer.getMovies().isEmpty()) {
            List<Movie> playerMovies = currentPlayer.getMovies();
            Movie lastMovie = playerMovies.get(playerMovies.size() - 1);
            
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
     * Parse query parameters
     */
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }
        
        for (String param : query.split("&")) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }
        
        return params;
    }
    
    /**
     * Parse request body as JSON
     */
    private Map<String, String> parseRequestBody(String body) {
        Map<String, String> params = new HashMap<>();
        if (body == null || body.isEmpty()) {
            return params;
        }
        
        // Simple JSON parsing
        body = body.trim();
        if (body.startsWith("{") && body.endsWith("}")) {
            body = body.substring(1, body.length() - 1);
            
            for (String pair : body.split(",")) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    
                    // Remove quotes
                    if (key.startsWith("\"") && key.endsWith("\"")) {
                        key = key.substring(1, key.length() - 1);
                    }
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    
                    params.put(key, value);
                }
            }
        }
        
        return params;
    }
    
    /**
     * Send HTTP response
     */
    private void sendResponse(HttpExchange exchange, ApiResponse response, int statusCode) throws IOException {
        String responseJson = response.toJson();
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, responseJson.getBytes("UTF-8").length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseJson.getBytes("UTF-8"));
            os.flush();
        }
    }
}
