package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Client {
    // === Attributes ===
    private final String name;
    private final String winGenre; // The genre the player is trying to collect
    private final int winThreshold; // How many movies of that genre are required to win

    private final Set<Integer> usedMovies; // Prevents movie reuse
    private final Map<String, Integer> genreCount; // Tracks how many times a genre has been played
    private final List<Movie> movieCollection; // Stores the player's collected movies

    private boolean hasBlocked; // Tracks whether block power-up was used
    private boolean isSkipped;  // Flag to indicate if player loses next turn
    private boolean isBlocked;  // Flag to indicate if player is blocked
    private boolean hasSelectedMovie; // Flag to indicate if player has already selected a movie in current turn

    // === Constructor ===
    public Client(String name, String winGenre, int winThreshold) {
        this.name = name;
        this.winGenre = winGenre.toLowerCase();
        this.winThreshold = winThreshold;
        this.usedMovies = new HashSet<>();
        this.genreCount = new HashMap<>();
        this.movieCollection = new ArrayList<>();
        this.hasBlocked = false;
        this.isSkipped = false;
        this.isBlocked = false;
        this.hasSelectedMovie = false;
    }

    // === Getters ===
    public String getName() {
        return name;
    }
    
    public String getWinGenre() {
        return winGenre;
    }
    
    /**
     * Get target genre (for frontend API)
     */
    public String getTargetGenre() {
        return winGenre;
    }
    
    public int getWinThreshold() {
        return winThreshold;
    }
    
    public Map<String, Integer> getGenreCount() {
        return new HashMap<>(genreCount);
    }

    /**
     * Get the player's collected movies
     */
    public List<Movie> getMovies() {
        return new ArrayList<>(movieCollection);
    }
    
    /**
     * Check if skip ability is available
     */
    public boolean isSkipAvailable() {
        return !hasBlocked;
    }
    
    /**
     * Check if block ability is available
     */
    public boolean isBlockAvailable() {
        return !hasBlocked;
    }

    /**
     * Check if player has used a specific movie
     */
    public boolean hasUsedMovie(int movieId) {
        return usedMovies.contains(movieId);
    }

    public boolean isSkipped() {
        return isSkipped;
    }

    public boolean hasBlocked() {
        return hasBlocked;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public boolean hasSelectedMovie() {
        return hasSelectedMovie;
    }

    // === Functional Methods ===

    /**
     * Add a movie to the player's collection
     */
    public void addMovie(Movie movie) {
        if (movie == null) {
            return;
        }
        
        int movieId = movie.getId();
        if (usedMovies.contains(movieId)) {
            return; // Movie already used
        }
        
        usedMovies.add(movieId);
        movieCollection.add(movie); // Add movie object to collection
        
        // Update genre counts
        Set<String> genres = movie.getGenre();
        if (genres != null) {
            for (String genre : genres) {
                String lowerGenre = genre.toLowerCase();
                int count = genreCount.getOrDefault(lowerGenre, 0);
                genreCount.put(lowerGenre, count + 1);
            }
        }
    }

    /**
     * Check if player has met win condition
     */
    public boolean hasMetWinCondition() {
        return genreCount.getOrDefault(winGenre, 0) >= winThreshold;
    }

    /**
     * Activate skip status
     */
    public void activateSkip() {
        isSkipped = true;
    }

    /**
     * Clear skip status
     */
    public void clearSkip() {
        isSkipped = false;
    }

    /**
     * Activate block status
     */
    public void activateBlock() {
        isBlocked = true;
    }

    /**
     * Clear block status
     */
    public void clearBlock() {
        isBlocked = false;
    }

    /**
     * Mark player as having used their block/skip ability
     */
    public void useBlock() {
        hasBlocked = true;
    }

    public void selectMovie() {
        hasSelectedMovie = true;
    }

    public void clearSelectedMovie() {
        hasSelectedMovie = false;
    }

    @Override
    public String toString() {
        return name + " | Genre Goal: " + winGenre + " (" +
                genreCount.getOrDefault(winGenre, 0) + "/" + winThreshold + ")";
    }

    public void addInitialMovieToClient(Movie movie) {
        if (movie == null) {
            return;
        }

        usedMovies.add(movie.getId());
        movieCollection.add(movie);

        return;
    }
}
