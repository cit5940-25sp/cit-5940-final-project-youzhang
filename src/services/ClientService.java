package services;

import models.Client;
import models.Movie;

/**
 * Service class, handles client/player-related business logic
 */
public class ClientService {
    
    /**
     * Check if player has met win condition
     */
    public boolean checkWinCondition(Client client) {
        return client.hasMetWinCondition();
    }
    
    /**
     * Add movie to player's collection
     */
    public void addMovieToClient(Client client, Movie movie) {
        client.addMovie(movie);
    }
    
    /**
     * Activate player's skip turn status
     */
    public void activateSkip(Client client) {
        client.activateSkip();
    }
    
    /**
     * Clear player's skip status
     */
    public void clearSkip(Client client) {
        client.clearSkip();
    }

    /**
     * Activate player's blocked status
     */
    public void activateBlock(Client client) {
        client.activateBlock();
    }

    /**
     * Clear player's block status
     */
    public void clearBlock(Client client) {
        client.clearBlock();
    }

    /**
     * Use block ability
     */
    public void useBlock(Client client) {
        client.useBlock();
    }
    
    /**
     * Use skip ability
     */
    public void useSkip(Client client) {
        client.useBlock(); // Both skip and block use the same underlying mechanism to mark as used
    }
    
    /**
     * Check if player has already used the specified movie
     */
    public boolean hasUsedMovie(Client client, int movieId) {
        return client.hasUsedMovie(movieId);
    }
    
    /**
     * Add initial movie to player's collection without counting it towards genre counts
     * This is a special method used only for the initial movie in the game
     * 
     * @param client The player to add the movie to
     * @param movie The initial movie to add
     */
    public void addInitialMovieToClient(Client client, Movie movie) {
        if (client == null || movie == null) {
            return;
        }
        
        // Add movie to client's collection
        // Since we can't directly access private fields anymore, we'll use the addMovie method
        // This will update genre counts, but for the initial movie it's acceptable
        client.addInitialMovieToClient(movie);
        
        System.out.println("Added initial movie: " + movie.getTitle() + " to player: " + client.getName());
    }
}
