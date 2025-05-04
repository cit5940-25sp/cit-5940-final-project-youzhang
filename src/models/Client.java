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
    private final List<Movie> movies; // List of movies collected by the player

    private boolean hasBlocked; // Tracks whether block power-up was used
    private boolean isSkipped;  // Flag to indicate if player loses next turn
    private boolean hasSelectedMovie; // Flag to indicate if player has selected a movie this turn

    // === Constructor ===
    public Client(String name, String winGenre, int winThreshold) {
        this.name = name;
        this.winGenre = winGenre.toLowerCase();
        this.winThreshold = winThreshold;
        this.usedMovies = new HashSet<>();
        this.genreCount = new HashMap<>();
        this.hasBlocked = false;
        this.isSkipped = false;
        this.hasSelectedMovie = false;
        this.movies = new ArrayList<>();
    }

    // === Getters ===
    public String getName() {
        return name;
    }

    public String getTargetGenre() {
        return winGenre;
    }

    public int getWinThreshold() {
        return winThreshold;
    }

    public boolean isSkipAvailable() {
        return !hasBlocked;
    }

    public boolean isBlockAvailable() {
        return !hasBlocked;
    }

    public boolean isBlocked() {
        return hasBlocked;
    }

    public boolean hasSelectedMovie() {
        return hasSelectedMovie;
    }

    public Map<String, Integer> getGenreCount() {
        return genreCount;
    }

    public String getWinGenre() {
        return winGenre;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    /**
     * Checks if the player has already used a movie with the given ID.
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

    // === Functional Methods ===

    /**
     * Adds a movie to the player's history and updates genre count.
     * @param movie The movie to add
     */
    public void addMovie(Movie movie) {
        Integer id = movie.getId();
        if (usedMovies.contains(id)) return;

        usedMovies.add(id);
        movies.add(movie);
        for (String genre : movie.getGenre()) {
            String lowerGenre = genre.toLowerCase();
            genreCount.put(lowerGenre, genreCount.getOrDefault(lowerGenre, 0) + 1);
        }
        hasSelectedMovie = true;
    }

    /**
     * Checks if the player has met their win condition.
     */
    public boolean hasMetWinCondition() {
        return genreCount.getOrDefault(winGenre, 0) >= winThreshold;
    }

    /**
     * Activate a skip (used by opponent power-up).
     */
    public void activateSkip() {
        isSkipped = true;
    }

    /**
     * Consume the skip and allow turn again.
     */
    public void clearSkip() {
        isSkipped = false;
    }

    /**
     * Activates block usage flag (only once allowed, depending on game logic).
     */
    public void useBlock() {
        hasBlocked = true;
    }

    /**
     * Reset the selected movie flag for the next turn.
     */
    public void clearSelectedMovie() {
        hasSelectedMovie = false;
    }

    /**
     * Mark that the player has selected a movie this turn.
     */
    public void selectMovie() {
        hasSelectedMovie = true;
    }

    /**
     * Activate block (used by opponent power-up).
     */
    public void activateBlock() {
        hasBlocked = true;
    }

    /**
     * Clear block status.
     */
    public void clearBlock() {
        hasBlocked = false;
    }

    @Override
    public String toString() {
        return name + " | Genre Goal: " + winGenre + " (" +
                genreCount.getOrDefault(winGenre, 0) + "/" + winThreshold + ")";
    }
}

