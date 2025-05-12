package models;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import models.Client;
import models.Movie;
import models.Tuple;
import java.util.*;

public class ClientTest {
    private Client client;
    private Movie testMovie1;
    private Movie testMovie2;
    private Set<String> genres1;
    private Set<String> genres2;
    private List<Tuple<String, Integer>> cast;
    private List<Tuple<String, Integer>> crew;

    @Before
    public void setUp() {
        client = new Client("Test Player", "Action", 3);
        genres1 = new HashSet<>();
        genres1.add("Action");
        genres2 = new HashSet<>();
        genres2.add("Sci-Fi");
        cast = new ArrayList<>();
        cast.add(new Tuple<>("Keanu Reeves", 1));
        crew = new ArrayList<>();
        crew.add(new Tuple<>("Lana Wachowski", 1));
        testMovie1 = new Movie("The Matrix", 1, 1999, genres1, cast, crew);
        testMovie2 = new Movie("Inception", 2, 2010, genres2, cast, crew);
    }

    @Test
    public void testGetName() {
        assertEquals("Test Player", client.getName());
    }

    @Test
    public void testGetWinGenre() {
        assertEquals("action", client.getWinGenre());
    }

    @Test
    public void testGetTargetGenre() {
        assertEquals("action", client.getTargetGenre());
    }

    @Test
    public void testGetWinThreshold() {
        assertEquals(3, client.getWinThreshold());
    }

    @Test
    public void testGetGenreCount() {
        Map<String, Integer> counts = client.getGenreCount();
        assertTrue(counts.isEmpty());
    }

    @Test
    public void testGetMovies() {
        assertTrue(client.getMovies().isEmpty());
    }

    @Test
    public void testAddMovie() {
        client.addMovie(testMovie1);
        assertEquals(1, client.getGenreCount().get("action").intValue());
        assertTrue(client.getMovies().contains(testMovie1));
        assertTrue(client.hasUsedMovie(testMovie1.getId()));
    }

    @Test
    public void testAddNullMovie() {
        client.addMovie(null);
        assertTrue(client.getMovies().isEmpty());
        assertTrue(client.getGenreCount().isEmpty());
    }

    @Test
    public void testAddDuplicateMovie() {
        client.addMovie(testMovie1);
        client.addMovie(testMovie1);
        assertEquals(1, client.getMovies().size());
        assertEquals(1, client.getGenreCount().get("action").intValue());
    }

    @Test
    public void testAddMovieWithMultipleGenres() {
        Set<String> multipleGenres = new HashSet<>(Arrays.asList("Action", "Sci-Fi", "Thriller"));
        Movie multiGenreMovie = new Movie("Inception", 3, 2010, multipleGenres, cast, crew);
        client.addMovie(multiGenreMovie);
        
        Map<String, Integer> counts = client.getGenreCount();
        assertEquals(1, counts.get("action").intValue());
        assertEquals(1, counts.get("sci-fi").intValue());
        assertEquals(1, counts.get("thriller").intValue());
    }

    @Test
    public void testIsSkipAvailable() {
        assertTrue(client.isSkipAvailable());
        client.useBlock();
        assertFalse(client.isSkipAvailable());
    }

    @Test
    public void testIsBlockAvailable() {
        assertTrue(client.isBlockAvailable());
        client.useBlock();
        assertFalse(client.isBlockAvailable());
    }

    @Test
    public void testIsSkipped() {
        assertFalse(client.isSkipped());
        client.activateSkip();
        assertTrue(client.isSkipped());
        client.clearSkip();
        assertFalse(client.isSkipped());
    }

    @Test
    public void testIsBlocked() {
        assertFalse(client.isBlocked());
        client.activateBlock();
        assertTrue(client.isBlocked());
        client.clearBlock();
        assertFalse(client.isBlocked());
    }

    @Test
    public void testHasBlocked() {
        assertFalse(client.hasBlocked());
        client.useBlock();
        assertTrue(client.hasBlocked());
    }

    @Test
    public void testHasSelectedMovie() {
        assertFalse(client.hasSelectedMovie());
        client.selectMovie();
        assertTrue(client.hasSelectedMovie());
        client.clearSelectedMovie();
        assertFalse(client.hasSelectedMovie());
    }

    @Test
    public void testHasMetWinCondition() {
        assertFalse(client.hasMetWinCondition());
        
        // Add one movie of target genre
        client.addMovie(testMovie1);
        assertFalse(client.hasMetWinCondition());
        
        // Add second movie of target genre
        Movie actionMovie2 = new Movie("Die Hard", 3, 1988, genres1, cast, crew);
        client.addMovie(actionMovie2);
        assertFalse(client.hasMetWinCondition());
        
        // Add third movie of target genre
        Movie actionMovie3 = new Movie("Terminator", 4, 1984, genres1, cast, crew);
        client.addMovie(actionMovie3);
        assertTrue(client.hasMetWinCondition());
        
        // Add movie of different genre
        client.addMovie(testMovie2);
        assertTrue(client.hasMetWinCondition()); // Should still be true
    }

    @Test
    public void testToString() {
        String expected = "Test Player | Genre Goal: action (0/3)";
        assertEquals(expected, client.toString());
        
        client.addMovie(testMovie1);
        expected = "Test Player | Genre Goal: action (1/3)";
        assertEquals(expected, client.toString());
    }
} 