package test.models;

import models.Client;
import models.Movie;
import models.Tuple;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class ClientTest {
    private Client client;
    private Movie movie1;
    private Movie movie2;
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
        movie1 = new Movie("The Matrix", 1, 1999, genres1, cast, crew);
        movie2 = new Movie("Inception", 2, 2010, genres2, cast, crew);
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
        client.addMovie(movie1);
        assertEquals(1, client.getGenreCount().get("action").intValue());
        assertTrue(client.getMovies().contains(movie1));
        assertTrue(client.hasUsedMovie(movie1.getId()));
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
        client.addMovie(movie1);
        client.addMovie(new Movie("Die Hard", 3, 1988, genres1, cast, crew));
        client.addMovie(new Movie("Terminator", 4, 1984, genres1, cast, crew));
        assertTrue(client.hasMetWinCondition());
    }

    @Test
    public void testToString() {
        String expected = "Test Player | Genre Goal: action (0/3)";
        assertEquals(expected, client.toString());
    }
} 