package test;

import com.sun.net.httpserver.HttpServer;
import controllers.GameController;
import factories.ServiceFactory;
import models.Client;
import models.Movie;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import services.GameService;
import services.MovieService;
import utils.DataLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test GameController using real components
 */
public class GameControllerTest {
    private HttpServer server;
    private GameController gameController;
    private GameService gameService;
    private MovieService movieService;
    private final int PORT = 8888; // Use a different port to avoid conflicts with the main application
    private final String BASE_URL = "http://localhost:" + PORT + "/api";
    
    @Before
    public void setUp() throws Exception {
        // Get real service instances
        gameService = ServiceFactory.getGameService();
        movieService = ServiceFactory.getMovieService();
        
        // Load test data
        DataLoader dataLoader = new DataLoader();
        try {
            dataLoader.loadMoviesFromCsv("src/movies.csv");
        } catch (IOException e) {
            System.out.println("WARNING: Unable to load movie data, using existing data for testing");
            e.printStackTrace();
        }
        
        // Create controller instance
        gameController = new GameController();
        
        // Create and start HTTP server
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api", gameController);
        server.setExecutor(null); // Use default executor
        server.start();
        
        System.out.println("Test server started on port: " + PORT);
    }
    
    @After
    public void tearDown() {
        // Stop the server
        if (server != null) {
            server.stop(0);
            System.out.println("Test server stopped");
        }
    }
    
    /**
     * Test game start functionality
     */
    @Test
    public void testStartGame() throws IOException {
        // Prepare request data
        String requestBody = "{\"player1Name\":\"Alice\",\"player1Genre\":\"action\",\"player2Name\":\"Bob\",\"player2Genre\":\"comedy\",\"winThreshold\":\"3\"}";
        
        // Send POST request
        HttpURLConnection connection = createConnection(BASE_URL + "/game/start", "POST");
        sendRequest(connection, requestBody);
        
        // Verify response
        int responseCode = connection.getResponseCode();
        String response = readResponse(connection);
        
        assertEquals(200, responseCode);
        assertTrue(response.contains("\"code\":200"));
        assertTrue(response.contains("\"message\":\"success\""));
        
        connection.disconnect();
    }
    
    /**
     * Test get game status functionality
     */
    @Test
    public void testGetGameStatus() throws IOException {
        // First start the game
        testStartGame();
        
        // Send GET request to get status
        HttpURLConnection connection = createConnection(BASE_URL + "/game/status", "GET");
        
        // Verify response
        int responseCode = connection.getResponseCode();
        String response = readResponse(connection);
        
        assertEquals(200, responseCode);
        assertTrue(response.contains("\"code\":200"));
        assertTrue(response.contains("\"gameOver\":"));
        assertTrue(response.contains("\"currentPlayerIndex\":"));
        
        connection.disconnect();
    }
    
    /**
     * Test search movies functionality
     */
    @Test
    public void testSearchMovies() throws IOException {
        // Send GET request to search movies
        HttpURLConnection connection = createConnection(BASE_URL + "/movies/search?q=star", "GET");
        
        // Verify response
        int responseCode = connection.getResponseCode();
        String response = readResponse(connection);
        
        assertEquals(200, responseCode);
        assertTrue(response.contains("\"code\":200"));
        assertTrue(response.contains("\"movies\":"));
        
        connection.disconnect();
    }
    
    /**
     * Test select movie functionality
     */
    @Test
    public void testSelectMovie() throws IOException {
        // First start the game
        testStartGame();
        
        // Search movies to get ID
        HttpURLConnection searchConnection = createConnection(BASE_URL + "/movies/search?q=star", "GET");
        String searchResponse = readResponse(searchConnection);
        searchConnection.disconnect();
        
        // Extract the first movie ID from the response (simplified, should use JSON parsing in practice)
        int startIndex = searchResponse.indexOf("\"id\":");
        if (startIndex == -1) {
            System.out.println("WARNING: No movie ID found in search results");
            return;
        }
        
        int endIndex = searchResponse.indexOf(",", startIndex);
        String idStr = searchResponse.substring(startIndex + 5, endIndex).trim();
        
        // Select movie
        HttpURLConnection connection = createConnection(BASE_URL + "/movies/select?id=" + idStr, "POST");
        
        // Verify response
        int responseCode = connection.getResponseCode();
        String response = readResponse(connection);
        
        assertEquals(200, responseCode);
        assertTrue(response.contains("\"code\":200"));
        
        connection.disconnect();
    }
    
    /**
     * Test skip functionality
     */
    @Test
    public void testSkipAction() throws IOException {
        // First start the game
        testStartGame();
        
        // Use skip functionality
        HttpURLConnection connection = createConnection(BASE_URL + "/actions/skip", "POST");
        
        // Verify response
        int responseCode = connection.getResponseCode();
        String response = readResponse(connection);
        
        assertEquals(200, responseCode);
        assertTrue(response.contains("\"code\":200"));
        
        connection.disconnect();
    }
    
    /**
     * Test block functionality
     */
    @Test
    public void testBlockAction() throws IOException {
        // First start the game
        testStartGame();
        
        // Use block functionality
        HttpURLConnection connection = createConnection(BASE_URL + "/actions/block", "POST");
        
        // Verify response
        int responseCode = connection.getResponseCode();
        String response = readResponse(connection);
        
        assertEquals(200, responseCode);
        assertTrue(response.contains("\"code\":200"));
        
        connection.disconnect();
    }
    
    /**
     * Test next player functionality
     */
    @Test
    public void testNextPlayer() throws IOException {
        // First start the game
        testStartGame();
        
        // Switch to next player
        HttpURLConnection connection = createConnection(BASE_URL + "/actions/next", "POST");
        
        // Verify response
        int responseCode = connection.getResponseCode();
        String response = readResponse(connection);
        
        assertEquals(200, responseCode);
        assertTrue(response.contains("\"code\":200"));
        assertTrue(response.contains("\"success\":true"));
        
        connection.disconnect();
    }
    
    /**
     * Test invalid route
     */
    @Test
    public void testInvalidRoute() throws IOException {
        // Send request to invalid route
        HttpURLConnection connection = createConnection(BASE_URL + "/invalid/route", "GET");
        
        // Verify response
        int responseCode = connection.getResponseCode();
        String response = readResponse(connection);
        
        assertEquals(404, responseCode);
        assertTrue(response.contains("\"code\":404"));
        assertTrue(response.contains("\"message\":\"Not Found\""));
        
        connection.disconnect();
    }
    
    /**
     * Create HTTP connection
     */
    private HttpURLConnection createConnection(String urlString, String method) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);
        return connection;
    }
    
    /**
     * Send request data
     */
    private void sendRequest(HttpURLConnection connection, String data) throws IOException {
        if (data != null && !data.isEmpty()) {
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = data.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.flush();
            }
        }
    }
    
    /**
     * Read response data
     */
    private String readResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        } catch (IOException e) {
            // Handle error response
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        }
    }
    
    /**
     * Main method, can run tests independently
     */
    public static void main(String[] args) {
        try {
            GameControllerTest test = new GameControllerTest();
            test.setUp();
            
            // Run tests
            System.out.println("\nTesting game start functionality...");
            test.testStartGame();
            
            System.out.println("\nTesting get game status functionality...");
            test.testGetGameStatus();
            
            System.out.println("\nTesting search movies functionality...");
            test.testSearchMovies();
            
            System.out.println("\nTesting select movie functionality...");
            test.testSelectMovie();
            
            System.out.println("\nTesting skip functionality...");
            test.testSkipAction();
            
            System.out.println("\nTesting block functionality...");
            test.testBlockAction();
            
            System.out.println("\nTesting next player functionality...");
            test.testNextPlayer();
            
            System.out.println("\nTesting invalid route...");
            test.testInvalidRoute();
            
            System.out.println("\nAll tests completed!");
            
            test.tearDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}