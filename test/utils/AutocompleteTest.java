package utils;

import models.Movie;
import models.Tuple;
import models.Autocomplete;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;

public class AutocompleteTest {
    private Autocomplete autocomplete;
    private Movie testMovie1;
    private Movie testMovie2;
    private Movie testMovie3;

    @Before
    public void setUp() {
        autocomplete = new Autocomplete();
        
        // Create test movies
        testMovie1 = new Movie("The Matrix", 1, 1999, 
            new HashSet<>(Arrays.asList("Action", "Sci-Fi")),
            Arrays.asList(new Tuple<>("Keanu Reeves", 1)),
            Arrays.asList(new Tuple<>("Lana Wachowski", 2)));
            
        testMovie2 = new Movie("The Matrix Reloaded", 2, 2003,
            new HashSet<>(Arrays.asList("Action", "Sci-Fi")),
            Arrays.asList(new Tuple<>("Keanu Reeves", 1)),
            Arrays.asList(new Tuple<>("Lana Wachowski", 2)));
            
        testMovie3 = new Movie("Inception", 3, 2010,
            new HashSet<>(Arrays.asList("Action", "Sci-Fi")),
            Arrays.asList(new Tuple<>("Leonardo DiCaprio", 3)),
            Arrays.asList(new Tuple<>("Christopher Nolan", 4)));
    }

    @Test
    public void testInsert() {
        autocomplete.insert(testMovie1);
        List<Movie> results = autocomplete.search("The Matrix");
        assertEquals("Should find inserted movie", 1, results.size());
        assertEquals("Should find correct movie", testMovie1, results.get(0));
    }

    @Test
    public void testSearchWithPrefix() {
        autocomplete.insert(testMovie1);
        autocomplete.insert(testMovie2);
        autocomplete.insert(testMovie3);

        // Test exact prefix match
        List<Movie> results = autocomplete.search("The Matrix");
        assertEquals("Should find 2 movies", 2, results.size());
        assertTrue("Should contain The Matrix", results.contains(testMovie1));
        assertTrue("Should contain The Matrix Reloaded", results.contains(testMovie2));

        // Test partial prefix match
        results = autocomplete.search("The");
        assertEquals("Should find 2 movies", 2, results.size());
        assertTrue("Should contain The Matrix", results.contains(testMovie1));
        assertTrue("Should contain The Matrix Reloaded", results.contains(testMovie2));

        // Test case insensitive search
        results = autocomplete.search("the matrix");
        assertEquals("Should find 2 movies", 2, results.size());
    }

    @Test
    public void testSearchWithLimit() {
        autocomplete.insert(testMovie1);
        autocomplete.insert(testMovie2);
        autocomplete.insert(testMovie3);

        List<Movie> results = autocomplete.search("The", 1);
        assertEquals("Should return only 1 movie", 1, results.size());
    }

    @Test
    public void testSearchNoResults() {
        List<Movie> results = autocomplete.search("Nonexistent");
        assertTrue("Should return empty list for nonexistent prefix", results.isEmpty());
    }

    @Test
    public void testSearchEmptyPrefix() {
        autocomplete.insert(testMovie1);
        autocomplete.insert(testMovie2);
        autocomplete.insert(testMovie3);

        List<Movie> results = autocomplete.search("");
        assertEquals("Should return all movies up to default limit", 3, results.size());
    }

    @Test
    public void testSearchResultOrdering() {
        autocomplete.insert(testMovie1);
        autocomplete.insert(testMovie2);
        autocomplete.insert(testMovie3);

        List<Movie> results = autocomplete.search("The Matrix");
        // The Matrix should come before The Matrix Reloaded due to length
        assertEquals("First result should be The Matrix", testMovie1, results.get(0));
        assertEquals("Second result should be The Matrix Reloaded", testMovie2, results.get(1));
    }

    @Test
    public void testInsertNullMovie() {
        autocomplete.insert(null);
        List<Movie> results = autocomplete.search("");
        assertEquals("Should not add null movie", 0, results.size());
    }

    @Test
    public void testSearchNullPrefix() {
        autocomplete.insert(testMovie1);
        List<Movie> results = autocomplete.search(null);
        assertTrue("Should return empty list for null prefix", results.isEmpty());
    }
} 