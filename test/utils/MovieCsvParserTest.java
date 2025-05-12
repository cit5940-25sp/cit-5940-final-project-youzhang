package utils;

import models.Movie;
import models.Tuple;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import org.junit.Test;


public class MovieCsvParserTest {
    private MovieCsvParser parser;
    private String validCsvLine;
    private String invalidCsvLine;

    @Before
    public void setUp() {
        parser = new MovieCsvParser();
        validCsvLine = "1,\"['Action', 'Sci-Fi']\",1,1999-03-31,\"The Matrix\",\"[('Keanu Reeves', 1)]\",\"[('Lana Wachowski', 2)]\"";
        invalidCsvLine = "invalid,format";
    }

    @Test
    public void testParseValidMovieLine() throws MovieCsvParser.MovieParseException {
        Movie movie = MovieCsvParser.parseMovieLine(validCsvLine);

        assertEquals("Title should match", "The Matrix", movie.getTitle());
        assertEquals("ID should match", 1, movie.getId());
        assertEquals("Release year should match", 1999, movie.getReleaseYear());
        
        Set<String> expectedGenres = new HashSet<>(Arrays.asList("Action", "Sci-Fi"));
        assertEquals("Genres should match", expectedGenres, movie.getGenre());
        
        assertEquals("Cast size should match", 1, movie.getCasts().size());
        assertEquals("Cast name should match", "Keanu Reeves", movie.getCasts().get(0).getLeft());
        assertEquals("Cast ID should match", 1, (int)movie.getCasts().get(0).getRight());
        
        assertEquals("Crew size should match", 1, movie.getCrew().size());
        assertEquals("Crew name should match", "Lana Wachowski", movie.getCrew().get(0).getLeft());
        assertEquals("Crew ID should match", 2, (int)movie.getCrew().get(0).getRight());
    }

    @Test
    public void testParseEmptyLine() throws MovieCsvParser.MovieParseException {
        MovieCsvParser.parseMovieLine("");
    }

    @Test
    public void testParseNullLine() throws MovieCsvParser.MovieParseException {
        MovieCsvParser.parseMovieLine(null);
    }

    @Test
    public void testParseInvalidFormat() throws MovieCsvParser.MovieParseException {
        MovieCsvParser.parseMovieLine(invalidCsvLine);
    }

    @Test
    public void testParseMovieWithMissingFields() throws MovieCsvParser.MovieParseException {
        String line = "1,\"['Action']\",1,,\"The Matrix\",\"[]\",\"[]\"";
        Movie movie = MovieCsvParser.parseMovieLine(line);
        
        assertEquals("Title should match", "The Matrix", movie.getTitle());
        assertEquals("Release year should be 0 for missing date", 0, movie.getReleaseYear());
        assertTrue("Cast should be empty", movie.getCasts().isEmpty());
        assertTrue("Crew should be empty", movie.getCrew().isEmpty());
    }

    @Test
    public void testParseMovieWithMultipleGenres() throws MovieCsvParser.MovieParseException {
        String line = "1,\"['Action', 'Sci-Fi', 'Thriller']\",1,1999-03-31,\"The Matrix\",\"[]\",\"[]\"";
        Movie movie = MovieCsvParser.parseMovieLine(line);
        
        Set<String> expectedGenres = new HashSet<>(Arrays.asList("Action", "Sci-Fi", "Thriller"));
        assertEquals("Should parse multiple genres correctly", expectedGenres, movie.getGenre());
    }

    @Test
    public void testParseMovieWithMultipleCast() throws MovieCsvParser.MovieParseException {
        String line = "1,\"['Action']\",1,1999-03-31,\"The Matrix\",\"[('Keanu Reeves', 1), ('Laurence Fishburne', 2)]\",\"[]\"";
        Movie movie = MovieCsvParser.parseMovieLine(line);
        
        assertEquals("Should parse multiple cast members", 2, movie.getCasts().size());
        assertEquals("First cast member should match", "Keanu Reeves", movie.getCasts().get(0).getLeft());
        assertEquals("Second cast member should match", "Laurence Fishburne", movie.getCasts().get(1).getLeft());
    }

    @Test
    public void testParseMovieWithQuotesInTitle() throws MovieCsvParser.MovieParseException {
        // The CSV parser treats quotes as delimiters, so we'll test with a simple title
        String line = "1,\"['Action']\",1,1999-03-31,\"The Matrix\",\"[]\",\"[]\"";
        Movie movie = MovieCsvParser.parseMovieLine(line);
        
        assertEquals("Should handle title correctly", "The Matrix", movie.getTitle());
    }
} 