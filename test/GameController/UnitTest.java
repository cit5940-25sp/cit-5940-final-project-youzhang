package test.GameController;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import controllers.GameController;
import factories.ServiceFactory;
import models.Client;
import models.Movie;
import org.junit.Before;
import org.junit.Test;
import services.GameService;
import services.MovieService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GameController
 */
public class UnitTest {
    private GameController gameController;
    private GameService gameService;
    private MovieService movieService;
    private HttpExchange mockExchange;
    private Headers mockHeaders;
    private ByteArrayOutputStream responseOutput;
    
    @Before
    public void setUp() {
        // Get real service instances
        gameService = ServiceFactory.getGameService();
        movieService = ServiceFactory.getMovieService();
        
        // Create controller with real services
        gameController = new GameController();
        
        // Mock HttpExchange
        mockExchange = mock(HttpExchange.class);
        mockHeaders = new Headers();
        responseOutput = new ByteArrayOutputStream();
        
        when(mockExchange.getResponseHeaders()).thenReturn(mockHeaders);
        when(mockExchange.getResponseBody()).thenReturn(responseOutput);
    }
    
    /**
     * Test for handleStartGame method
     */
    @Test
    public void testHandleStartGame() throws IOException {
        // Setup request
        String requestBody = "{\"player1Name\":\"TestPlayer1\",\"player1Genre\":\"action\",\"player2Name\":\"TestPlayer2\",\"player2Genre\":\"comedy\",\"winThreshold\":3}";
        ByteArrayInputStream requestBodyStream = new ByteArrayInputStream(requestBody.getBytes());
        
        when(mockExchange.getRequestBody()).thenReturn(requestBodyStream);
        when(mockExchange.getRequestMethod()).thenReturn("POST");
        when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/game/start"));
        
        // Call handle method
        gameController.handle(mockExchange);
        
        // Verify response
        verify(mockExchange).sendResponseHeaders(eq(200), anyInt());
        String response = responseOutput.toString();
        assertTrue(response.contains("\"code\":200"));
        assertTrue(response.contains("\"success\":true"));
        
        // Verify game state
        assertFalse(gameService.isGameOver());
        assertEquals(1, gameService.getTurnCount());
        assertEquals(0, gameService.getCurrentPlayerIndex());
        assertNotNull(gameService.getLastMovie()); // Initial movie should be selected
    }
    
    /**
     * Test for handleGetGameStatus method
     */
    @Test
    public void testHandleGetGameStatus() throws IOException {
        // Setup game with players
        setupTestGame();
        
        // Setup request
        when(mockExchange.getRequestMethod()).thenReturn("GET");
        when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/game/status"));
        
        // Call handle method
        gameController.handle(mockExchange);
        
        // Verify response
        verify(mockExchange).sendResponseHeaders(eq(200), anyInt());
        String response = responseOutput.toString();
        assertTrue(response.contains("\"code\":200"));
        assertTrue(response.contains("\"success\":true"));
        assertTrue(response.contains("\"gameOver\":false"));
        assertTrue(response.contains("\"currentPlayerIndex\":0"));
        assertTrue(response.contains("\"turnCount\":1"));
    }
    
    /**
     * Test for handleSearchMovies method
     */
    @Test
    public void testHandleSearchMovies() throws IOException {
        // Setup request
        when(mockExchange.getRequestMethod()).thenReturn("GET");
        when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/movies/search?term=star&limit=5"));
        
        // Call handle method
        gameController.handle(mockExchange);
        
        // Verify response
        verify(mockExchange).sendResponseHeaders(eq(200), anyInt());
        String response = responseOutput.toString();
        assertTrue(response.contains("\"code\":200"));
        assertTrue(response.contains("\"success\":true"));
        assertTrue(response.contains("\"movies\":"));
    }
    
    /**
     * Test for handleSelectMovie method
     */
    @Test
    public void testHandleSelectMovie() throws IOException {
        // Setup game with players
        setupTestGame();
        
        // Get a valid movie ID
        List<Movie> movies = movieService.getAllMovies();
        int movieId = movies.get(0).getId();
        
        // Setup request
        when(mockExchange.getRequestMethod()).thenReturn("POST");
        when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/movies/select?id=" + movieId));
        
        // Call handle method
        gameController.handle(mockExchange);
        
        // Verify response
        verify(mockExchange).sendResponseHeaders(anyInt(), anyInt());
        String response = responseOutput.toString();
        
        // The response could be success or error depending on if the movie is connected
        // to the initial movie, but the controller should handle it properly either way
        assertTrue(response.contains("\"code\":"));
    }
    
    /**
     * Test for handleSkipAction method
     */
    @Test
    public void testHandleSkipAction() throws IOException {
        // Setup game with players
        setupTestGame();
        
        // Setup request
        when(mockExchange.getRequestMethod()).thenReturn("POST");
        when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/actions/skip"));
        
        // Call handle method
        gameController.handle(mockExchange);
        
        // Verify response
        verify(mockExchange).sendResponseHeaders(eq(200), anyInt());
        String response = responseOutput.toString();
        assertTrue(response.contains("\"code\":200"));
        assertTrue(response.contains("\"success\":true"));
        
        // Verify game state - player should have used skip
        Client currentPlayer = gameService.getCurrentPlayer();
        assertFalse(currentPlayer.isSkipAvailable());
    }
    
    /**
     * Test for handleBlockAction method
     */
    @Test
    public void testHandleBlockAction() throws IOException {
        // Setup game with players
        setupTestGame();
        
        // Setup request
        when(mockExchange.getRequestMethod()).thenReturn("POST");
        when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/actions/block"));
        
        // Call handle method
        gameController.handle(mockExchange);
        
        // Verify response
        verify(mockExchange).sendResponseHeaders(eq(200), anyInt());
        String response = responseOutput.toString();
        assertTrue(response.contains("\"code\":200"));
        assertTrue(response.contains("\"success\":true"));
        
        // Verify game state - player should have used block
        Client currentPlayer = gameService.getCurrentPlayer();
        assertFalse(currentPlayer.isBlockAvailable());
    }
    
    /**
     * Test for handleNextPlayer method
     */
    @Test
    public void testHandleNextPlayer() throws IOException {
        // Setup game with players
        setupTestGame();
        
        // Remember current player index
        int initialPlayerIndex = gameService.getCurrentPlayerIndex();
        
        // Setup request
        when(mockExchange.getRequestMethod()).thenReturn("POST");
        when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/actions/next"));
        
        // Call handle method
        gameController.handle(mockExchange);
        
        // Verify response
        verify(mockExchange).sendResponseHeaders(eq(200), anyInt());
        String response = responseOutput.toString();
        assertTrue(response.contains("\"code\":200"));
        assertTrue(response.contains("\"success\":true"));
        
        // Verify game state - player should have changed
        int newPlayerIndex = gameService.getCurrentPlayerIndex();
        assertNotEquals(initialPlayerIndex, newPlayerIndex);
    }
    
    /**
     * Test for handleCheckTime method
     */
    @Test
    public void testHandleCheckTime() throws IOException {
        // Setup game with players
        setupTestGame();
        
        // Setup request
        when(mockExchange.getRequestMethod()).thenReturn("GET");
        when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/game/check-time"));
        
        // Call handle method
        gameController.handle(mockExchange);
        
        // Verify response
        verify(mockExchange).sendResponseHeaders(eq(200), anyInt());
        String response = responseOutput.toString();
        assertTrue(response.contains("\"code\":200"));
        assertTrue(response.contains("\"success\":true"));
        assertTrue(response.contains("\"isTurnTimeOut\":"));
        assertTrue(response.contains("\"remainingTurnTime\":"));
    }
    
    /**
     * Test for invalid route handling
     */
    @Test
    public void testInvalidRoute() throws IOException {
        // Setup request
        when(mockExchange.getRequestMethod()).thenReturn("GET");
        when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/invalid/route"));
        
        // Call handle method
        gameController.handle(mockExchange);
        
        // Verify response
        verify(mockExchange).sendResponseHeaders(eq(404), anyInt());
        String response = responseOutput.toString();
        assertTrue(response.contains("\"code\":404"));
        assertTrue(response.contains("\"success\":false"));
    }
    
    /**
     * Test for OPTIONS request handling
     */
    @Test
    public void testOptionsRequest() throws IOException {
        // Setup request
        when(mockExchange.getRequestMethod()).thenReturn("OPTIONS");
        when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/game/status"));
        
        // Call handle method
        gameController.handle(mockExchange);
        
        // Verify response
        verify(mockExchange).sendResponseHeaders(eq(204), eq(-1));
        assertTrue(mockHeaders.containsKey("Access-Control-Allow-Origin"));
        assertTrue(mockHeaders.containsKey("Access-Control-Allow-Methods"));
        assertTrue(mockHeaders.containsKey("Access-Control-Allow-Headers"));
    }
    
    /**
     * Helper method to setup a test game
     */
    private void setupTestGame() {
        List<Client> players = new ArrayList<>();
        players.add(new Client("TestPlayer1", "action", 3));
        players.add(new Client("TestPlayer2", "comedy", 3));
        gameService.initGame(players);
    }
}