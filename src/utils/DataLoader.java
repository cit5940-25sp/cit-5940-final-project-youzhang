package utils;

import models.Movie;
import services.MovieService;
import factories.ServiceFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * DataLoader class, responsible for loading movie data from CSV file
 */
public class DataLoader {
    private MovieService movieService;
    
    /**
     * constructor
     */
    public DataLoader(MovieService movieService) {
        this.movieService = movieService;
    }
    
    /**
     * constructor, automatically gets MovieService instance from ServiceFactory
     */
    public DataLoader() {
        this.movieService = ServiceFactory.getMovieService();
    }
    
    /**
     * load movie data from CSV file
     */
    public void loadMoviesFromCsv(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // skip header line
            String line = reader.readLine();
            int lineCount = 1;
            int successCount = 0;
            int skipCount = 0;
            
            // read data lines
            while ((line = reader.readLine()) != null) {
                lineCount++;
                try {
                    Movie movie = MovieCsvParser.parseMovieLine(line);
                    movieService.addMovie(movie);
                    successCount++;
                } catch (MovieCsvParser.MovieParseException e) {
                    // only record error, continue processing
                    System.err.println("skip line " + lineCount + ": " + e.getMessage());
                    skipCount++;
                }
            }
            
            System.out.println("movie data loaded: total lines=" + (lineCount-1) + ", success=" + successCount + ", skipped=" + skipCount);
        }
    }
}
