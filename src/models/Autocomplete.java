package models;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;
import models.Movie;

public class Autocomplete {
    // Trie Node class
    private static class TrieNode {
        private Map<Character, TrieNode> children;
        private boolean isEndOfWord;
        private Movie movie;

        public TrieNode() {
            children = new HashMap<>();
            isEndOfWord = false;
            movie = null;
        }
    }

    private TrieNode root;
    private int totalMovies;

    public Autocomplete() {
        root = new TrieNode();
        totalMovies = 0;
    }

    // Insert movie into Trie
    public void insert(Movie movie) {
        if (movie == null || movie.getTitle() == null) {
            System.out.println("Warning: Attempted to insert null movie or movie with null title");
            return;
        }

        TrieNode current = root;
        String lowerTitle = movie.getTitle().toLowerCase();
        
        for (int i = 0; i < lowerTitle.length(); i++) {
            char c = lowerTitle.charAt(i);
            
            if (!current.children.containsKey(c)) {
                current.children.put(c, new TrieNode());
            }
            current = current.children.get(c);
        }
        
        current.isEndOfWord = true;
        current.movie = movie;
        totalMovies++;
    }

    // Search movies by prefix
    public List<Movie> search(String prefix) {
        return search(prefix, 20); // Default return 20 results
    }

    // Search movies by prefix with limit
    public List<Movie> search(String prefix, int limit) {
        List<Movie> results = new ArrayList<>();
        
        if (prefix == null) {
            System.out.println("Warning: Search prefix is null");
            return results;
        }

        System.out.println("Searching with prefix: '" + prefix + "', limit: " + limit + " (total movies in trie: " + totalMovies + ")");
        
        TrieNode current = root;
        String lowerPrefix = prefix.toLowerCase().trim().replace(" ", "");
        
        // Handle empty prefix - return first N movies
        if (lowerPrefix.isEmpty()) {
            System.out.println("Empty prefix - collecting all movies up to limit");
            collectMovies(root, results);
            results.sort(new MovieComparator(lowerPrefix));
            List<Movie> limitedResults = results.subList(0, Math.min(limit, results.size()));
            System.out.println("Returning " + limitedResults.size() + " results for empty prefix");
            return limitedResults;
        }

        // Navigate to prefix node
        for (int i = 0; i < lowerPrefix.length(); i++) {
            char c = lowerPrefix.charAt(i);
            
            if (!current.children.containsKey(c)) {
                System.out.println("No movies found with prefix '" + prefix + "' (stopped at character '" + c + "')");
                return results;
            }
            current = current.children.get(c);
        }

        // Collect all movies under this prefix
        collectMovies(current, results);
        
        System.out.println("Found " + results.size() + " movies with prefix '" + prefix + "' before sorting");
        
        // Sort by relevance
        results.sort(new MovieComparator(lowerPrefix));
        List<Movie> limitedResults = results.subList(0, Math.min(limit, results.size()));
        
        System.out.println("Returning " + limitedResults.size() + " results after sorting and limiting");
        return limitedResults;
    }

    // Recursively collect all possible movies
    private void collectMovies(TrieNode node, List<Movie> results) {
        if (node == null) {
            return;
        }

        if (node.isEndOfWord && node.movie != null) {
            results.add(node.movie);
        }

        for (TrieNode child : node.children.values()) {
            collectMovies(child, results);
        }
    }

    // Movie comparator, used to sort by relevance
    private static class MovieComparator implements Comparator<Movie> {
        private final String prefix;

        public MovieComparator(String prefix) {
            this.prefix = prefix.toLowerCase();
        }

        @Override
        public int compare(Movie m1, Movie m2) {
            String title1 = m1.getTitle().toLowerCase();
            String title2 = m2.getTitle().toLowerCase();

            // If one title starts with prefix and other doesn't, prioritize the matching one
            boolean starts1 = title1.startsWith(prefix);
            boolean starts2 = title2.startsWith(prefix);
            
            if (starts1 && !starts2) return -1;
            if (!starts1 && starts2) return 1;

            // If both match or don't match, compare by title length
            int lengthDiff = title1.length() - title2.length();
            if (lengthDiff != 0) return lengthDiff;

            // If lengths are equal, sort alphabetically
            return title1.compareTo(title2);
        }
    }
} 