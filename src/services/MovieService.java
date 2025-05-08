package services;

import models.Movie;
import models.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class, handles movie-related business logic
 */
public class MovieService {
    private final Map<Integer, Movie> movieCache = new HashMap<>();
    
    /**
     * Get movie by ID
     */
    public Movie getMovieById(int id) {
        return movieCache.get(id);
    }
    
    /**
     * Add movie to cache
     */
    public void addMovie(Movie movie) {
        movieCache.put(movie.getId(), movie);
    }
    
    /**
     * Get all movies
     */
    public List<Movie> getAllMovies() {
        return new ArrayList<>(movieCache.values());
    }
    
    /**
     * Get movie cache
     */
    public Map<Integer, Movie> getMovieCache() {
        return movieCache;
    }
    
    /**
     * Search movies
     */
    public List<Movie> searchMovies(String query) {
        query = query.toLowerCase();
        List<Movie> results = new ArrayList<>();
        
        for (Movie movie : movieCache.values()) {
            if (movie.getTitle().toLowerCase().contains(query)) {
                results.add(movie);
            }
        }
        
        return results;
    }
    
    /**
     * Check if two movies are connected
     */
    public boolean areMoviesConnected(Movie movie1, Movie movie2) {
        return movie1 != null && movie2 != null && movie1.isConnectedTo(movie2);
    }
    
    /**
     * Get connections between two movies (common cast or crew members)
     */
    public List<String> getMovieConnections(Movie movie1, Movie movie2) {
        List<String> connections = new ArrayList<>();
        
        if (movie1 == null || movie2 == null) {
            return connections;
        }
        
        // Check common cast members
        for (Tuple<String, Integer> cast1 : movie1.getCasts()) {
            if (movie2.hasCast(cast1.getRight())) {
                connections.add("Actor: " + cast1.getLeft());
            }
        }
        
        // Check common crew members
        for (Tuple<String, Integer> crew1 : movie1.getCrew()) {
            if (movie2.hasCrew(crew1.getRight())) {
                connections.add("Crew: " + crew1.getLeft());
            }
        }
        
        return connections;
    }
}
