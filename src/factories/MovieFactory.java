package factories;

import models.Movie;
import models.Tuple;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * factory class, responsible for creating movie objects
 */
public class MovieFactory {
    
    /**
     * create a new movie object
     */
    public static Movie createMovie(String title, int id, int releaseYear, Set<String> genre, 
                                  List<Tuple<String, Integer>> cast, List<Tuple<String, Integer>> crew) {
        return new Movie(title, id, releaseYear, genre, cast, crew);
    }
    
    /**
     * create a movie object from data map
     */
    public static Movie createMovieFromData(Map<String, Object> data) {
        // implement the logic to create a movie object from data map
        String title = (String) data.get("title");
        int id = (int) data.get("id");
        int releaseYear = (int) data.get("releaseYear");
        
        @SuppressWarnings("unchecked")
        Set<String> genre = (Set<String>) data.get("genre");
        
        @SuppressWarnings("unchecked")
        List<Tuple<String, Integer>> cast = (List<Tuple<String, Integer>>) data.get("cast");
        
        @SuppressWarnings("unchecked")
        List<Tuple<String, Integer>> crew = (List<Tuple<String, Integer>>) data.get("crew");
        
        return new Movie(title, id, releaseYear, genre, cast, crew);
    }
}
