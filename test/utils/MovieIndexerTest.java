package utils;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.ArrayList;
import models.Movie;
import models.Tuple;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MovieIndexerTest {
    private MovieIndexer indexer;
    private Movie testMovie1;
    private Movie testMovie2;
    private Set<String> genres;
    private List<Tuple<String, Integer>> cast;
    private List<Tuple<String, Integer>> crew;
    private Path tempCsvFile;

    @Before
    public void setUp() throws IOException {
        indexer = new MovieIndexer();
        
        // Initialize common test data
        genres = new HashSet<>(Arrays.asList("Action", "Sci-Fi"));
        cast = new ArrayList<>();
        cast.add(new Tuple<>("Keanu Reeves", 1));
        crew = new ArrayList<>();
        crew.add(new Tuple<>("Lana Wachowski", 2));
        
        // Create test movies with all required parameters
        testMovie1 = new Movie("The Matrix", 1, 1999, genres, cast, crew);
        testMovie2 = new Movie("The Matrix Reloaded", 2, 2003, genres, cast, crew);
        
        indexer.indexMovie(testMovie1);
        indexer.indexMovie(testMovie2);

        // Create a temporary CSV file for testing
        tempCsvFile = Files.createTempFile("test_movies", ".csv");
        String csvContent = "id,title,releaseYear,genres,cast,crew\n" +
                          "1,The Matrix,1999,Action|Sci-Fi,Keanu Reeves:1,Lana Wachowski:2\n" +
                          "2,The Matrix Reloaded,2003,Action|Sci-Fi,Keanu Reeves:1,Lana Wachowski:2";
        Files.write(tempCsvFile, csvContent.getBytes());
    }

    @Test
    public void testIndexerCreation() {
        assertNotNull("MovieIndexer should be created", indexer);
        assertNotNull("Autocomplete should be initialized", indexer.getAutocomplete());
    }

    @Test
    public void testSearchByPrefix() {
        List<Movie> results = indexer.searchByPrefix("The Matrix");
        assertEquals("Should find 2 movies", 2, results.size());
        assertTrue("Should contain The Matrix", results.contains(testMovie1));
        assertTrue("Should contain The Matrix Reloaded", results.contains(testMovie2));
    }

    @Test
    public void testSearchByExactTitle() {
        List<Movie> results = indexer.searchByPrefix("The Matrix Reloaded");
        assertEquals("Should return only 1 movie", 1, results.size());
        assertTrue("Should contain The Matrix Reloaded", results.contains(testMovie2));
    }

    @Test
    public void testSearchNonexistentPrefix() {
        List<Movie> results = indexer.searchByPrefix("Nonexistent");
        assertTrue("Should return empty list for nonexistent prefix", results.isEmpty());
    }

    @Test
    public void testGetAutocomplete() {
        Autocomplete autocomplete = indexer.getAutocomplete();
        assertSame("Should return the same autocomplete instance", autocomplete, indexer.getAutocomplete());
    }

    @Test
    public void testLoadMoviesFromCSV() {
        // Create a new indexer to start fresh
        MovieIndexer newIndexer = new MovieIndexer();
        
        // Load movies from the temporary CSV file
        newIndexer.loadMoviesFromCSV(tempCsvFile.toString());
        
        // Verify that movies were loaded correctly
        List<Movie> loadedMovies = newIndexer.searchByPrefix("The Matrix");
        assertEquals("Should find 2 movies", 2, loadedMovies.size());
        
        // Verify movie details
        Movie firstMovie = loadedMovies.get(0);
        assertEquals("The Matrix", firstMovie.getTitle());
        assertEquals(1999, firstMovie.getReleaseYear());
        assertTrue(firstMovie.getGenre().contains("Action"));
        assertTrue(firstMovie.getGenre().contains("Sci-Fi"));
        assertTrue(firstMovie.hasCast(1));
        assertTrue(firstMovie.hasCrew(2));
    }

    @Test
    public void testLoadMoviesFromCSVWithIOException() {
        // Create a new indexer
        MovieIndexer newIndexer = new MovieIndexer();
        
        // Try to load from a non-existent file
        newIndexer.loadMoviesFromCSV("nonexistent_file.csv");
        
        // Verify that no movies were loaded
        List<Movie> loadedMovies = newIndexer.searchByPrefix("The Matrix");
        assertTrue("Should not load any movies from non-existent file", loadedMovies.isEmpty());
    }

    
} 