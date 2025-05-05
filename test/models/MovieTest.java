package test.models;

import models.Movie;
import models.Tuple;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class MovieTest {
    private Movie movie;
    private Set<String> genres;
    private List<Tuple<String, Integer>> cast;
    private List<Tuple<String, Integer>> crew;

    @Before
    public void setUp() {
        genres = new HashSet<>();
        genres.add("Sci-Fi");
        cast = new ArrayList<>();
        cast.add(new Tuple<>("Keanu Reeves", 1));
        crew = new ArrayList<>();
        crew.add(new Tuple<>("Lana Wachowski", 1));
        movie = new Movie("The Matrix", 1, 1999, genres, cast, crew);
    }

    @Test
    public void testGetId() {
        assertEquals(1, movie.getId());
    }

    @Test
    public void testGetTitle() {
        assertEquals("The Matrix", movie.getTitle());
    }

    @Test
    public void testGetReleaseYear() {
        assertEquals(1999, movie.getReleaseYear());
    }

    @Test
    public void testGetGenre() {
        Set<String> expectedGenres = new HashSet<>();
        expectedGenres.add("Sci-Fi");
        assertEquals(expectedGenres, movie.getGenre());
    }

    @Test
    public void testGetCasts() {
        assertEquals(cast, movie.getCasts());
    }

    @Test
    public void testGetCrew() {
        assertEquals(crew, movie.getCrew());
    }

    @Test
    public void testToString() {
        assertEquals("The Matrix (1999)", movie.toString());
    }

    @Test
    public void testEquals() {
        Movie sameMovie = new Movie("The Matrix", 1, 1999, genres, cast, crew);
        Movie differentMovie = new Movie("Inception", 2, 2010, genres, cast, crew);

        assertTrue(movie.equals(sameMovie));
        assertFalse(movie.equals(differentMovie));
        assertFalse(movie.equals(null));
        assertTrue(movie.equals(movie));
    }

    @Test
    public void testHashCode() {
        Movie sameMovie = new Movie("The Matrix", 1, 1999, genres, cast, crew);
        assertEquals(movie.hashCode(), sameMovie.hashCode());
    }

    @Test
    public void testHasCast() {
        assertTrue(movie.hasCast(1));
        assertFalse(movie.hasCast(2));
    }

    @Test
    public void testHasCrew() {
        assertTrue(movie.hasCrew(1));
        assertFalse(movie.hasCrew(2));
    }

    @Test
    public void testIsConnectedTo() {
        Set<String> otherGenres = new HashSet<>();
        otherGenres.add("Action");
        List<Tuple<String, Integer>> otherCast = new ArrayList<>();
        otherCast.add(new Tuple<>("Keanu Reeves", 1));
        Movie connectedMovie = new Movie("John Wick", 2, 2014, otherGenres, otherCast, crew);
        
        assertTrue(movie.isConnectedTo(connectedMovie));
        assertFalse(movie.isConnectedTo(null));
    }
} 