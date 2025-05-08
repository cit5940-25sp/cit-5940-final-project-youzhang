package utils;

import models.Movie;
import models.Tuple;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;

public class MovieIndexerTest {
    private MovieIndexer movieIndexer;
    private Autocomplete mockAutocomplete;
    private Movie testMovie1;
    private Movie testMovie2;

    @Before
    public void setUp() {
        mockAutocomplete = new Autocomplete();
        movieIndexer = new MovieIndexer(mockAutocomplete);
        
        // Create test movies
        testMovie1 = new Movie("The Matrix", 1, 1999, 
            new HashSet<>(Arrays.asList("Action", "Sci-Fi")),
            Arrays.asList(new Tuple<>("Keanu Reeves", 1)),
            Arrays.asList(new Tuple<>("Lana Wachowski", 2)));
            
        testMovie2 = new Movie("The Matrix Reloaded", 2, 2003,
            new HashSet<>(Arrays.asList("Action", "Sci-Fi")),
            Arrays.asList(new Tuple<>("Keanu Reeves", 1)),
            Arrays.asList(new Tuple<>("Lana Wachowski", 2)));
    }

    @Test
    public void testConstructor() {
        assertNotNull("MovieIndexer should be created", movieIndexer);
        assertNotNull("Autocomplete should be initialized", movieIndexer.getAutocomplete());
    }

    @Test
    public void testSearchMovies() {
        // Insert test movies into autocomplete
        mockAutocomplete.insert(testMovie1);
        mockAutocomplete.insert(testMovie2);

        // Test search with prefix
        List<Movie> results = movieIndexer.searchMovies("The Matrix");
        assertEquals("Should find 2 movies", 2, results.size());
        assertTrue("Should contain The Matrix", results.contains(testMovie1));
        assertTrue("Should contain The Matrix Reloaded", results.contains(testMovie2));

        // Test search with limit
        results = movieIndexer.searchMovies("The Matrix", 1);
        assertEquals("Should return only 1 movie", 1, results.size());
    }

    @Test
    public void testSearchMoviesNoResults() {
        List<Movie> results = movieIndexer.searchMovies("Nonexistent");
        assertTrue("Should return empty list for nonexistent prefix", results.isEmpty());
    }

    @Test
    public void testGetAutocomplete() {
        assertSame("Should return the same autocomplete instance", 
            mockAutocomplete, movieIndexer.getAutocomplete());
    }
} 