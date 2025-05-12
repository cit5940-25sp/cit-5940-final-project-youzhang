package models;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

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
        Movie differentIdMovie = new Movie("The Matrix", 2, 1999, genres, cast, crew);

        assertTrue(movie.equals(sameMovie));
        assertFalse(movie.equals(differentMovie));
        assertFalse(movie.equals(differentIdMovie));
        assertFalse(movie.equals(null));
        assertTrue(movie.equals(movie));
    }

    @Test
    public void testHashCode() {
        Movie sameMovie = new Movie("The Matrix", 1, 1999, genres, cast, crew);
        Movie differentMovie = new Movie("Inception", 2, 2010, genres, cast, crew);
        
        assertEquals(movie.hashCode(), sameMovie.hashCode());
        assertNotEquals(movie.hashCode(), differentMovie.hashCode());
    }

    @Test
    public void testHasCast() {
        assertTrue(movie.hasCast(1));
        assertFalse(movie.hasCast(2));
        
        // Test with multiple cast members
        List<Tuple<String, Integer>> multipleCast = new ArrayList<>();
        multipleCast.add(new Tuple<>("Keanu Reeves", 1));
        multipleCast.add(new Tuple<>("Laurence Fishburne", 2));
        Movie movieWithMultipleCast = new Movie("The Matrix", 1, 1999, genres, multipleCast, crew);
        
        assertTrue(movieWithMultipleCast.hasCast(1));
        assertTrue(movieWithMultipleCast.hasCast(2));
        assertFalse(movieWithMultipleCast.hasCast(3));
    }

    @Test
    public void testHasCrew() {
        assertTrue(movie.hasCrew(1));
        assertFalse(movie.hasCrew(2));
        
        // Test with multiple crew members
        List<Tuple<String, Integer>> multipleCrew = new ArrayList<>();
        multipleCrew.add(new Tuple<>("Lana Wachowski", 1));
        multipleCrew.add(new Tuple<>("Lilly Wachowski", 2));
        Movie movieWithMultipleCrew = new Movie("The Matrix", 1, 1999, genres, cast, multipleCrew);
        
        assertTrue(movieWithMultipleCrew.hasCrew(1));
        assertTrue(movieWithMultipleCrew.hasCrew(2));
        assertFalse(movieWithMultipleCrew.hasCrew(3));
    }

    @Test
    public void testIsConnectedTo() {
        // Test with connected movies (same cast)
        Set<String> otherGenres = new HashSet<>();
        otherGenres.add("Action");
        List<Tuple<String, Integer>> otherCast = new ArrayList<>();
        otherCast.add(new Tuple<>("Keanu Reeves", 1));
        Movie connectedMovie = new Movie("John Wick", 2, 2014, otherGenres, otherCast, crew);
        
        assertTrue(movie.isConnectedTo(connectedMovie));
        
        // Test with connected movies (same crew)
        List<Tuple<String, Integer>> otherCrew = new ArrayList<>();
        otherCrew.add(new Tuple<>("Lana Wachowski", 1));
        Movie connectedMovie2 = new Movie("Cloud Atlas", 3, 2012, otherGenres, otherCast, otherCrew);
        
        assertTrue(movie.isConnectedTo(connectedMovie2));
        
        // Test with unconnected movies
        List<Tuple<String, Integer>> differentCast = new ArrayList<>();
        differentCast.add(new Tuple<>("Tom Cruise", 3));
        List<Tuple<String, Integer>> differentCrew = new ArrayList<>();
        differentCrew.add(new Tuple<>("Christopher McQuarrie", 3));
        Movie unconnectedMovie = new Movie("Mission Impossible", 4, 1996, otherGenres, differentCast, differentCrew);
        
        assertFalse(movie.isConnectedTo(unconnectedMovie));
        assertFalse(movie.isConnectedTo(null));
    }
} 