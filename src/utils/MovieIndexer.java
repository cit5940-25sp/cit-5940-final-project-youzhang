package utils;

import java.io.IOException;
import java.util.List;
import factories.ServiceFactory;
import services.MovieService;
import utils.DataLoader;
import models.Movie;

public class MovieIndexer {
    private Autocomplete autocomplete;
    private MovieService movieService;

    public MovieIndexer() {
        this(new Autocomplete());
    }
    
    public MovieIndexer(Autocomplete autocomplete) {
        this.autocomplete = autocomplete;
        movieService = ServiceFactory.getMovieService();
    }

    // Load movies from CSV
    public void loadMoviesFromCSV(String csvFilePath) {
        try {
            // Load movies using DataLoader
            DataLoader dataLoader = new DataLoader(movieService);
            dataLoader.loadMoviesFromCsv(csvFilePath);
            
            // Add loaded movies to Autocomplete
            for (Movie movie : movieService.getAllMovies()) {
                autocomplete.insert(movie);
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error processing movie data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Index a single movie
    public void indexMovie(Movie movie) {
        if (movie != null) {
            autocomplete.insert(movie);
            movieService.addMovie(movie);
        }
    }

    // Search movies by prefix
    public List<Movie> searchByPrefix(String prefix) {
        return autocomplete.search(prefix);
    }

    // Search movies by prefix with limit
    public List<Movie> searchByPrefix(String prefix, int limit) {
        return autocomplete.search(prefix, limit);
    }

    // Get autocomplete instance
    public Autocomplete getAutocomplete() {
        return autocomplete;
    }
} 