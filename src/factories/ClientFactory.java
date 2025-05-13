package factories;

import models.Client;
import services.MovieService;
import factories.ServiceFactory;

import java.util.Set;

/**
 * Factory class, responsible for creating client/player objects
 */
public class ClientFactory {
    
    /**
     * Create a new client/player object
     * @param name Player name
     * @param winGenre Player target genre
     * @param winThreshold Number of movies needed to win
     * @return New created Client object
     * @throws IllegalArgumentException If target genre does not exist in the movie database
     */
    public static Client createClient(String name, String winGenre, int winThreshold) {
        // Validate target genre
        validateGenre(winGenre);
        return new Client(name, winGenre, winThreshold);
    }
    
    /**
     * Validate if the target genre exists in the movie database
     * @param genre Genre to validate
     * @throws IllegalArgumentException If the target genre does not exist in the movie database
     */
    private static void validateGenre(String genre) {
        System.out.println("[DEBUG] Start validating genre: " + genre);
        
        if (genre == null || genre.trim().isEmpty()) {
            System.out.println("[DEBUG] Genre is empty");
            throw new IllegalArgumentException("Genre cannot be empty");
        }
        
        // Get MovieService instance
        MovieService movieService = ServiceFactory.getMovieService();
        System.out.println("[DEBUG] Got MovieService instance: " + (movieService != null ? "成功" : "失败"));
        
        // Get all available movie genres
        Set<String> availableGenres = movieService.getAllGenres();
        System.out.println("[DEBUG] Got movie genres count: " + availableGenres.size());
        System.out.println("[DEBUG] Available genres: " + availableGenres);
        
        // Check if target genre exists
        if (availableGenres.isEmpty()) {
            System.out.println("[DEBUG] Movie database is empty");
            throw new IllegalArgumentException("Movie database is empty, cannot validate target genre");
        }
        
        // Convert input genre to lowercase for comparison
        String inputGenre = genre.trim();
        System.out.println("[DEBUG] Processed input genre: " + inputGenre);
        
        // Print all available genres for debugging
        System.out.println("[DEBUG] All available genres:");
        for (String availableGenre : availableGenres) {
            System.out.println("  - " + availableGenre);
        }
        
        // Check if target genre exists in available genres
        // Use strict exact matching, not partial matching
        boolean genreExists = false;
        for (String availableGenre : availableGenres) {
            // Ignore case for exact matching
            if (availableGenre.equalsIgnoreCase(inputGenre)) {
                genreExists = true;
                System.out.println("[DEBUG] Found exact match genre: " + availableGenre);
                break;
            }
        }
        
        // If no exact match found, throw exception
        if (!genreExists) {
            System.out.println("[DEBUG] Genre does not exist in the database");
            System.out.println("[DEBUG] Valid genres include: " + String.join(", ", availableGenres));
            throw new IllegalArgumentException("Genre '" + genre + "' does not exist in the movie database. Valid genres include: " + String.join(", ", availableGenres));
        }
        
        System.out.println("[DEBUG] Genre validation successful");
    }
}
