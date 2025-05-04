package models;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Movie {
    // === Attributes ===
    private final String title;
    private final int id;
    private final int releaseYear;
    private final Set<String> genre;
    private final List<Tuple<String, Integer>> cast;
    private final List<Tuple<String, Integer>> crew;
    

    // === Constructor ===
    public Movie(String title, int id, int releaseYear, Set<String> genre, List<Tuple<String, Integer>> cast,
                    List<Tuple<String, Integer>> crew) {
        this.title = title;
        this.id = id;
        this.releaseYear = releaseYear;
        this.genre = genre;
        this.cast = cast; // Convert list to HashSet for O(1) lookups
        this.crew = crew;
    }

    // === Getters ===
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public Set<String> getGenre() {
        return genre;
    }

    public List<Tuple<String, Integer>> getCasts() {
        return cast; // Return copy to protect internal set
    }

    public List<Tuple<String, Integer>> getCrew() {
        return crew;
    }


    // === Functional Methods ===

    /**
     * Checks if the movie has a given actor.
     */
    public boolean hasCast(int castId) {
        for (Tuple<String, Integer> tuple : cast) {
            if (tuple.getRight() == castId) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCrew(int crewId) {
        for (Tuple<String, Integer> tuple : crew) {
            if (tuple.getRight() == crewId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this movie shares any of the connection attributes with another movie.
     */
    public boolean isConnectedTo(Movie other) {
        if (other == null) return false;

        // Check cast intersection
        for (Tuple<String, Integer> castMember : this.cast) {
            if (other.hasCast(castMember.getRight())) return true;
        }

        // Check crew intersection
        for (Tuple<String, Integer> crewMember : this.crew) {
            if (other.hasCrew(crewMember.getRight())) return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return title + " (" + releaseYear + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Movie)) return false;
        Movie other = (Movie) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
