package test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import controllers.GameController;
import factories.ClientFactory;
import factories.ServiceFactory;
import models.Client;
import models.Movie;
import services.GameService;
import services.MovieService;
import utils.DataLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test class for GameController
 */
public class GameControllerTest {
    
    private GameController gameController;
    private GameService gameService;
    private MovieService movieService;
    private HttpServer server;
    
    /**
     * Setup work before testing
     */
    // Initialization method
    public void setUp() throws Exception {
        // Load some test movie data
        DataLoader dataLoader = new DataLoader();
        try {
            // Load movie data from CSV
            dataLoader.loadMoviesFromCsv("src/movies.csv");
        } catch (IOException e) {
            System.err.println("Error loading movie data: " + e.getMessage());
        }
        
        // Get service instances
        gameService = ServiceFactory.getGameService();
        movieService = ServiceFactory.getMovieService();
        
        // Create GameController instance
        gameController = new GameController();
        
        // Create a simple HTTP server for testing
        server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.createContext("/api", gameController);
        server.setExecutor(null);
        server.start();
    }
    
    /**
     * Cleanup work after testing
     */
    // Cleanup method
    public void tearDown() {
        // Stop the server
        if (server != null) {
            server.stop(0);
        }
    }
    
    /**
     * Test handling OPTIONS request
     */
    // Test method
    public void testHandleOptionsRequest() throws Exception {
        // Create mock HttpExchange
        MockHttpExchange exchange = new MockHttpExchange("OPTIONS", "/api/game/status");
        
        // Call handle method
        gameController.handle(exchange);
        
        // Verify response
        assert 204 == exchange.getResponseCode() : "Expected response code 204, but got " + exchange.getResponseCode();
        assert exchange.getResponseHeaders().containsKey("Access-Control-Allow-Origin");
        assert exchange.getResponseHeaders().containsKey("Access-Control-Allow-Methods");
        assert exchange.getResponseHeaders().containsKey("Access-Control-Allow-Headers");
    }
    
    /**
     * Test starting a game
     */
    // Test method
    public void testHandleStartGame() throws Exception {
        // Create mock HttpExchange
        String requestBody = "{\"player1Name\":\"TestPlayer1\",\"player1Genre\":\"action\",\"player2Name\":\"TestPlayer2\",\"player2Genre\":\"comedy\",\"winThreshold\":2}";
        MockHttpExchange exchange = new MockHttpExchange("POST", "/api/game/start", requestBody);
        
        // Call handle method
        gameController.handle(exchange);
        
        // Verify response
        assert 200 == exchange.getResponseCode() : "Expected response code 200, but got " + exchange.getResponseCode();
        String response = new String(((MockHttpExchange)exchange).getResponseBodyAsBytes());
        assert response.contains("\"success\":true");
        assert response.contains("\"players\"");
        assert response.contains("\"currentPlayerIndex\":0");
    }
    
    /**
     * Test getting game status
     */
    // Test method
    public void testHandleGetGameStatus() throws Exception {
        // Initialize the game first
        initializeTestGame();
        
        // Create mock HttpExchange
        MockHttpExchange exchange = new MockHttpExchange("GET", "/api/game/status");
        
        // Call handle method
        gameController.handle(exchange);
        
        // Verify response
        assert 200 == exchange.getResponseCode() : "Expected response code 200, but got " + exchange.getResponseCode();
        String response = new String(((MockHttpExchange)exchange).getResponseBodyAsBytes());
        assert response.contains("\"success\":true");
        assert response.contains("\"gameOver\":false");
        assert response.contains("\"players\"");
    }
    
    /**
     * Test searching for movies
     */
    // Test method
    public void testHandleSearchMovies() throws Exception {
        // Create mock HttpExchange
        MockHttpExchange exchange = new MockHttpExchange("GET", "/api/movies/search?term=star&limit=5");
        
        // Call handle method
        gameController.handle(exchange);
        
        // Verify response
        assert 200 == exchange.getResponseCode() : "Expected response code 200, but got " + exchange.getResponseCode();
        String response = new String(((MockHttpExchange)exchange).getResponseBodyAsBytes());
        assert response.contains("\"success\":true");
        assert response.contains("\"movies\"");
    }
    
    /**
     * Test selecting a movie
     */
    // Test method
    public void testHandleSelectMovie() throws Exception {
        // Initialize the game first
        initializeTestGame();
        
        // Get a valid movie ID
        List<Movie> movies = movieService.getAllMovies();
        if (movies.isEmpty()) {
            throw new RuntimeException("No movie data available for testing");
        }
        
        // Get the ID of the first movie
        int movieId = movies.get(0).getId();
        
        // Create mock HttpExchange
        MockHttpExchange exchange = new MockHttpExchange("POST", "/api/movies/select?id=" + movieId);
        
        // Call handle method
        gameController.handle(exchange);
        
        // Verify response
        assert 200 == exchange.getResponseCode() : "Expected response code 200, but got " + exchange.getResponseCode();
        String response = new String(((MockHttpExchange)exchange).getResponseBodyAsBytes());
        assert response.contains("\"success\":true");
    }
    
    /**
     * Test using skip ability
     */
    // Test method
    public void testHandleSkipAction() throws Exception {
        // Initialize the game first
        initializeTestGame();
        
        // Create mock HttpExchange
        MockHttpExchange exchange = new MockHttpExchange("POST", "/api/actions/skip");
        
        // Call handle method
        gameController.handle(exchange);
        
        // Verify response
        assert 200 == exchange.getResponseCode() : "Expected response code 200, but got " + exchange.getResponseCode();
        String response = new String(((MockHttpExchange)exchange).getResponseBodyAsBytes());
        assert response.contains("\"success\":true");
    }
    
    /**
     * Test using block ability
     */
    // Test method
    public void testHandleBlockAction() throws Exception {
        // Initialize the game first
        initializeTestGame();
        
        // Create mock HttpExchange
        MockHttpExchange exchange = new MockHttpExchange("POST", "/api/actions/block");
        
        // Call handle method
        gameController.handle(exchange);
        
        // Verify response
        assert 200 == exchange.getResponseCode() : "Expected response code 200, but got " + exchange.getResponseCode();
        String response = new String(((MockHttpExchange)exchange).getResponseBodyAsBytes());
        assert response.contains("\"success\":true");
    }
    
    /**
     * Test switching to the next player
     */
    // Test method
    public void testHandleNextPlayer() throws Exception {
        // Initialize the game first
        initializeTestGame();
        
        // Record current player index
        int currentPlayerIndex = gameService.getCurrentPlayerIndex();
        
        // Create mock HttpExchange
        MockHttpExchange exchange = new MockHttpExchange("POST", "/api/actions/next");
        
        // Call handle method
        gameController.handle(exchange);
        
        // Verify response
        assert 200 == exchange.getResponseCode() : "Expected response code 200, but got " + exchange.getResponseCode();
        String response = new String(((MockHttpExchange)exchange).getResponseBodyAsBytes());
        assert response.contains("\"success\":true");
        
        // Verify player has been switched
        int newPlayerIndex = gameService.getCurrentPlayerIndex();
        assert currentPlayerIndex != newPlayerIndex : "Player index should have changed";
    }
    
    /**
     * Test checking game time
     */
    // Test method
    public void testGameTimeCheck() throws Exception {
        // Initialize the game first
        initializeTestGame();
        
        // Create mock HttpExchange
        MockHttpExchange exchange = new MockHttpExchange("GET", "/api/game/check-time");
        
        // Call handle method
        gameController.handle(exchange);
        
        // Verify response
        assert 200 == exchange.getResponseCode() : "Expected response code 200, but got " + exchange.getResponseCode();
        String response = new String(((MockHttpExchange)exchange).getResponseBodyAsBytes());
        assert response.contains("\"success\":true");
        assert response.contains("\"isTurnTimeOut\":false");
        assert response.contains("\"isGameOver\":false");
        assert response.contains("\"remainingTurnTime\"");
    }
    
    /**
     * Test turn timeout
     */
    // Test method
    public void testTurnTimeOut() throws Exception {
        // Initialize the game first
        initializeTestGame();
        
        // Simulate turn timeout
        // Since we can't actually wait for 30 seconds, we directly modify the current turn start time in GameService
        java.lang.reflect.Field field = GameService.class.getDeclaredField("currentTurnStartTime");
        field.setAccessible(true);
        field.set(gameService, System.currentTimeMillis() - 31000); // Set to 31 seconds ago
        
        // Create mock HttpExchange
        MockHttpExchange exchange = new MockHttpExchange("GET", "/api/game/check-time");
        
        // Call handle method
        gameController.handle(exchange);
        
        // Verify response
        assert 200 == exchange.getResponseCode() : "Expected response code 200, but got " + exchange.getResponseCode();
        String response = new String(((MockHttpExchange)exchange).getResponseBodyAsBytes());
        assert response.contains("\"isTurnTimeOut\":true");
        assert response.contains("\"isGameOver\":true");
    }
    
    /**
     * Test initial movie selection
     */
    // Test method
    public void testInitialMovieSelection() throws Exception {
        // Create mock HttpExchange
        String requestBody = "{\"player1Name\":\"TestPlayer1\",\"player1Genre\":\"action\",\"player2Name\":\"TestPlayer2\",\"player2Genre\":\"comedy\",\"winThreshold\":2}";
        MockHttpExchange exchange = new MockHttpExchange("POST", "/api/game/start", requestBody);
        
        // Call handle method
        gameController.handle(exchange);
        
        // Verify response
        assert 200 == exchange.getResponseCode() : "Expected response code 200, but got " + exchange.getResponseCode();
        String response = new String(((MockHttpExchange)exchange).getResponseBodyAsBytes());
        
        // Verify initial movie has been selected
        Movie lastMovie = gameService.getLastMovie();
        assert lastMovie != null : "Initial movie should have been selected";
    }
    
    /**
     * Initialize test game
     */
    private void initializeTestGame() {
        // Create test players
        Client player1 = ClientFactory.createClient("TestPlayer1", "action", 2);
        Client player2 = ClientFactory.createClient("TestPlayer2", "comedy", 2);
        
        List<Client> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        
        // Initialize game
        gameService.initGame(players);
    }
    
    /**
     * Mock HttpExchange class for testing
     */
    private static class MockHttpExchange extends HttpExchange {
        private com.sun.net.httpserver.Headers requestHeaders = new com.sun.net.httpserver.Headers();
        private com.sun.net.httpserver.Headers responseHeaders = new com.sun.net.httpserver.Headers();
        private com.sun.net.httpserver.HttpContext httpContext = null;
        private final String method;
        private final URI uri;
        private final ByteArrayInputStream requestBody;
        private final ByteArrayOutputStream responseBody;

        private int responseCode;
        
        public MockHttpExchange(String method, String path) {
            this(method, path, "");
        }
        
        public MockHttpExchange(String method, String path, String requestBody) {
            this.method = method;
            this.uri = URI.create(path);
            this.requestBody = new ByteArrayInputStream(requestBody.getBytes());
            this.responseBody = new ByteArrayOutputStream();

        }
        
        @Override
        public String getRequestMethod() {
            return method;
        }
        
        @Override
        public URI getRequestURI() {
            return uri;
        }
        
        @Override
        public InputStream getRequestBody() {
            return requestBody;
        }
        
        @Override
        public OutputStream getResponseBody() {
            return responseBody;
        }
        
        @Override
        public com.sun.net.httpserver.Headers getRequestHeaders() {
            return requestHeaders;
        }
        
        @Override
        public com.sun.net.httpserver.Headers getResponseHeaders() {
            return responseHeaders;
        }
        
        @Override
        public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
            this.responseCode = rCode;
        }
        
        public int getResponseCode() {
            return responseCode;
        }
        
        public byte[] getResponseBodyAsBytes() {
            return responseBody.toByteArray();
        }
        
        // The following methods don't need to be implemented, they're just to satisfy the abstract class requirements
        @Override public void close() {}
        @Override public void setStreams(InputStream i, OutputStream o) {}
        @Override public Object getAttribute(String name) { return null; }
        @Override public void setAttribute(String name, Object value) {}
        @Override public com.sun.net.httpserver.HttpContext getHttpContext() { return httpContext; }
        @Override public java.net.InetSocketAddress getLocalAddress() { return null; }
        @Override public com.sun.net.httpserver.HttpPrincipal getPrincipal() { return null; }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public InetSocketAddress getRemoteAddress() { return null; }
    }
    
    /**
     * Main method for manually running tests
     */
    public static void main(String[] args) {
        try {
            GameControllerTest test = new GameControllerTest();
            test.setUp();
            
            System.out.println("Running testHandleOptionsRequest...");
            test.testHandleOptionsRequest();
            
            System.out.println("Running testHandleStartGame...");
            test.testHandleStartGame();
            
            System.out.println("Running testHandleGetGameStatus...");
            test.testHandleGetGameStatus();
            
            System.out.println("Running testHandleSearchMovies...");
            test.testHandleSearchMovies();
            
            System.out.println("Running testHandleSelectMovie...");
            test.testHandleSelectMovie();
            
            System.out.println("Running testHandleSkipAction...");
            test.testHandleSkipAction();
            
            System.out.println("Running testHandleBlockAction...");
            test.testHandleBlockAction();
            
            System.out.println("Running testHandleNextPlayer...");
            test.testHandleNextPlayer();
            
            System.out.println("Running testGameTimeCheck...");
            test.testGameTimeCheck();
            
            System.out.println("Running testTurnTimeOut...");
            test.testTurnTimeOut();
            
            System.out.println("Running testInitialMovieSelection...");
            test.testInitialMovieSelection();
            
            System.out.println("All tests passed!");
            
            test.tearDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}