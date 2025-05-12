package services;

import models.Client;
import models.Movie;
import factories.ServiceFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service class, handles the overall game logic
 */
public class GameService {
    private final ClientService clientService;
    private final MovieService movieService;
    
    private List<Client> players;
    private int currentPlayerIndex;
    private int turnCount;
    private boolean gameOver;
    private Client winner;
    
    // timing related attributes
    private long currentTurnStartTime; // current turn start time
    private static final long TURN_TIME_LIMIT = 30 * 1000; // 30 seconds, unit milliseconds
    
    /**
     * Constructor
     */
    public GameService(ClientService clientService, MovieService movieService) {
        this.clientService = clientService;
        this.movieService = movieService;
        this.players = new ArrayList<>();
        this.currentPlayerIndex = 0;
        this.turnCount = 1;
        this.gameOver = false;
    }
    
    /**
     * No-arg constructor, automatically gets dependencies from ServiceFactory
     */
    public GameService() {
        this(ServiceFactory.getClientService(), ServiceFactory.getMovieService());
    }
    
    /**
     * Initialize game
     * @param players List of players to initialize the game with
     * @return The initial movie that was selected, or null if no suitable movie was found
     */
    public Movie initGame(List<Client> players) {
        this.players = new ArrayList<>(players);
        this.currentPlayerIndex = 0;
        this.turnCount = 1;
        this.gameOver = false;
        this.winner = null;
        
        // initialize turn timer
        this.currentTurnStartTime = System.currentTimeMillis();
        
        // Select a random movie as the initial movie that matches all players' win genres
        Movie initialMovie = selectRandomInitialMovie();
        
        // If a suitable initial movie was found, add it to all players' collections
        // but don't count it towards their genre counts for win condition
        if (initialMovie != null) {
            for (Client player : players) {
                // We need to add the movie to the player's collection without counting genres
                // This requires direct access to the player's movieCollection
                // Since we can't modify the Client class, we'll use the ClientService
                clientService.addInitialMovieToClient(player, initialMovie);
            }
        }
        
        return initialMovie;
    }
    
    /**
     * Select a random movie as the initial movie that matches ALL player win genres
     * @return The selected movie, or null if no matching movie found
     */
    private Movie selectRandomInitialMovie() {
        if (players.isEmpty()) {
            System.out.println("Warning: No players available for genre matching");
            return null;
        }
        
        Map<Integer, Movie> movieCache = movieService.getMovieCache();
        if (movieCache.isEmpty()) {
            System.out.println("Warning: No movies available for random selection");
            return null;
        }
        
        // Collect all unique win genres from players
        Set<String> playerGenres = new HashSet<>();
        for (Client player : players) {
            playerGenres.add(player.getWinGenre());
        }
        
        // Filter movies that contain ALL players' win genres
        List<Movie> matchingMovies = new ArrayList<>();
        for (Movie movie : movieCache.values()) {
            Set<String> movieGenres = movie.getGenre();
            if (movieGenres != null && !movieGenres.isEmpty()) {
                boolean containsAllGenres = true;
                
                // Check if movie contains all required genres
                for (String requiredGenre : playerGenres) {
                    boolean containsThisGenre = false;
                    
                    for (String movieGenre : movieGenres) {
                        if (movieGenre.equalsIgnoreCase(requiredGenre)) {
                            containsThisGenre = true;
                            break;
                        }
                    }
                    
                    if (!containsThisGenre) {
                        containsAllGenres = false;
                        break;
                    }
                }
                
                if (containsAllGenres) {
                    matchingMovies.add(movie);
                }
            }
        }
        
        // If no matching movies found, return null
        if (matchingMovies.isEmpty()) {
            System.out.println("Warning: No movies matching ALL player win genres");
            return null;
        }
        
        // Select a random movie from matching movies
        int randomIndex = (int) (Math.random() * matchingMovies.size());
        Movie randomMovie = matchingMovies.get(randomIndex);
        
        System.out.println("Selected random initial movie: " + randomMovie.getTitle() + 
                           " with genres: " + randomMovie.getGenre());
        
        return randomMovie;
    }
    
    /**
     * Get current player
     */
    public Client getCurrentPlayer() {
        if (players.isEmpty()) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }
    
    /**
     * Get current player index
     */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }
    
    /**
     * Get next player
     */
    public void nextPlayer() {
        if (players.isEmpty()) {
            return;
        }
        
        // Clear current player's selected movie flag
        Client currentPlayer = getCurrentPlayer();
        if (currentPlayer != null) {
            currentPlayer.clearSelectedMovie();
        }
        
        // Switch to next player
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        
        // Check current player status
        Client nextPlayer = getCurrentPlayer();
        
        // If player is skipped, clear skip status and switch to next player again
        if (nextPlayer.isSkipped()) {
            clientService.clearSkip(nextPlayer);
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
        // If player is blocked, don't clear block status immediately
        // This allows the frontend to see the blocked status, and the player will get an error when trying to select a movie
        else if (nextPlayer.isBlocked()) {
            System.out.println("Player " + nextPlayer.getName() + " is blocked for this turn");
            // Don't immediately clear block status, let frontend detect it
            // Block status will be checked when player tries to select a movie, or cleared next time nextPlayer is called
        }
        
        // If back to first player, increase turn count
        if (currentPlayerIndex == 0) {
            turnCount++;
        }
        
        // reset turn timer
        this.currentTurnStartTime = System.currentTimeMillis();
    }
    
    /**
     * Process movie selection for the current player
     * 
     * @param movieId The ID of the movie to select
     * @return true if the selection was successful, false if the movie doesn't exist
     */
    public boolean processMovieSelection(int movieId) {
        // Get the movie by ID
        Movie movie = movieService.getMovieById(movieId);
        if (movie == null) {
            return false;
        }
        
        Client currentPlayer = getCurrentPlayer();
        
        // Update game state
        clientService.addMovieToClient(currentPlayer, movie);
        currentPlayer.selectMovie();
        
        // Check win condition
        if (clientService.checkWinCondition(currentPlayer)) {
            gameOver = true;
            winner = currentPlayer;
        }
        
        return true;
    }
    
    /**
     * Use skip power-up
     */
    public boolean useSkipPowerUp() {
        Client currentPlayer = getCurrentPlayer();
        if (currentPlayer == null) {
            return false;
        }
        
        // Check if player still has skip ability
        if (!currentPlayer.isSkipAvailable()) {
            return false;
        }
        
        // Get next player index
        int nextPlayerIndex = (currentPlayerIndex + 1) % players.size();
        Client nextPlayer = players.get(nextPlayerIndex);
        
        // Activate skip status for next player
        clientService.activateSkip(nextPlayer);
        
        // Mark current player as having used skip ability
        clientService.useSkip(currentPlayer);
        
        // no longer automatically switch to next player, frontend will call nextPlayer API
        return true;
    }
    
    /**
     * Use block power-up
     */
    public boolean useBlockPowerUp() {
        Client currentPlayer = getCurrentPlayer();
        if (currentPlayer == null) {
            return false;
        }
        
        // Check if player still has block ability
        if (!currentPlayer.isBlockAvailable()) {
            return false;
        }
        
        // Get next player index
        int nextPlayerIndex = (currentPlayerIndex + 1) % players.size();
        Client nextPlayer = players.get(nextPlayerIndex);
        
        // Activate block status for next player
        clientService.activateBlock(nextPlayer);
        
        // Mark current player as having used block ability
        clientService.useBlock(currentPlayer);
        
        // no longer automatically switch to next player, frontend will call nextPlayer API
        return true;
    }
    
    /**
     * Get game status
     */
    public boolean isGameOver() {
        return gameOver;
    }
    
    /**
     * Get winner
     */
    public Client getWinner() {
        return winner;
    }
    
    /**
     * Get current turn count
     */
    public int getTurnCount() {
        return turnCount;
    }
    
    /**
     * Get all players
     */
    public List<Client> getPlayers() {
        return new ArrayList<>(players);
    }
    
    /**
     * check if turn time out
     */
    public boolean isTurnTimeOut() {
        return System.currentTimeMillis() - currentTurnStartTime >= TURN_TIME_LIMIT;
    }
    
    /**
     * get remaining turn time (milliseconds)
     */
    public long getRemainingTurnTime() {
        long elapsed = System.currentTimeMillis() - currentTurnStartTime;
        long remaining = TURN_TIME_LIMIT - elapsed;
        return Math.max(0, remaining);
    }
}
