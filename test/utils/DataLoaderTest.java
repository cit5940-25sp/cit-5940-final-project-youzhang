package utils;

import models.Movie;
import services.MovieService;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DataLoaderTest {
    private DataLoader dataLoader;
    private MovieService mockMovieService;
    private File tempFile;

    @Before
    public void setUp() throws IOException {
        mockMovieService = new MovieService();
        dataLoader = new DataLoader(mockMovieService);
        
        // Create a temporary CSV file for testing
        tempFile = File.createTempFile("test_movies", ".csv");
        try (FileWriter writer = new FileWriter(tempFile)) {
            // Write header
            writer.write("row_number,genres,id,release_date,title,cast,crew\n");
            // Write test data
            writer.write("1,\"['Action', 'Sci-Fi']\",1,1999-03-31,\"The Matrix\",\"[('Keanu Reeves', 1)]\",\"[('Lana Wachowski', 2)]\"\n");
            writer.write("2,\"['Action', 'Sci-Fi']\",2,2003-05-15,\"The Matrix Reloaded\",\"[('Keanu Reeves', 1)]\",\"[('Lana Wachowski', 2)]\"\n");
        }
    }

    @Test
    public void testLoadMoviesFromCsv() throws IOException {
        dataLoader.loadMoviesFromCsv(tempFile.getAbsolutePath());
        
        List<Movie> movies = mockMovieService.getAllMovies();
        assertEquals("Should load 2 movies", 2, movies.size());
        
        // Verify first movie
        Movie matrix = movies.get(0);
        assertEquals("First movie title should match", "The Matrix", matrix.getTitle());
        assertEquals("First movie ID should match", 1, matrix.getId());
        assertEquals("First movie year should match", 1999, matrix.getReleaseYear());
        
        // Verify second movie
        Movie matrixReloaded = movies.get(1);
        assertEquals("Second movie title should match", "The Matrix Reloaded", matrixReloaded.getTitle());
        assertEquals("Second movie ID should match", 2, matrixReloaded.getId());
        assertEquals("Second movie year should match", 2003, matrixReloaded.getReleaseYear());
    }

    @Test(expected = IOException.class)
    public void testLoadMoviesFromNonexistentFile() throws IOException {
        dataLoader.loadMoviesFromCsv("nonexistent_file.csv");
    }

    @Test
    public void testLoadMoviesWithInvalidLine() throws IOException {
        // Add an invalid line to the test file
        try (FileWriter writer = new FileWriter(tempFile, true)) {
            writer.write("invalid,format,line\n");
        }
        
        dataLoader.loadMoviesFromCsv(tempFile.getAbsolutePath());
        
        // Should still load the valid movies
        List<Movie> movies = mockMovieService.getAllMovies();
        assertEquals("Should load 2 valid movies", 2, movies.size());
    }

    @Test
    public void testLoadMoviesWithEmptyFile() throws IOException {
        // Create an empty file
        File emptyFile = File.createTempFile("empty_movies", ".csv");
        try (FileWriter writer = new FileWriter(emptyFile)) {
            writer.write("row_number,genres,id,release_date,title,cast,crew\n");
        }
        
        dataLoader.loadMoviesFromCsv(emptyFile.getAbsolutePath());
        
        List<Movie> movies = mockMovieService.getAllMovies();
        assertTrue("Should have no movies", movies.isEmpty());
    }

    @Test
    public void testConstructorWithDefaultMovieService() {
        DataLoader defaultLoader = new DataLoader();
        assertNotNull("Should create DataLoader with default MovieService", defaultLoader);
    }
} 