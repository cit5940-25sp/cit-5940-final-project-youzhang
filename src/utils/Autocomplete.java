package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;
import models.Movie;

public class Autocomplete {
    // Trie node class
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

    public Autocomplete() {
        root = new TrieNode();
    }

    // Insert movie into Trie
    public void insert(Movie movie) {
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
    }

    // Search movies by prefix
    public List<Movie> search(String prefix) {
        return search(prefix, 10); // Default return 10 results
    }

    // Search movies by prefix with limit
    public List<Movie> search(String prefix, int limit) {
        List<Movie> results = new ArrayList<>();
        TrieNode current = root;
        String lowerPrefix = prefix.toLowerCase();

        // Find the last node of the prefix
        for (int i = 0; i < lowerPrefix.length(); i++) {
            char c = lowerPrefix.charAt(i);
            
            if (!current.children.containsKey(c)) {
                return results; // If prefix not found, return empty list
            }
            current = current.children.get(c);
        }

        // Collect all possible movies from this node
        collectMovies(current, results);

        // Sort by match quality and limit results
        results.sort(new MovieComparator(prefix));
        return results.subList(0, Math.min(limit, results.size()));
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

    // Movie comparator for sorting by match quality
    private static class MovieComparator implements Comparator<Movie> {
        private final String prefix;

        public MovieComparator(String prefix) {
            this.prefix = prefix.toLowerCase();
        }

        @Override
        public int compare(Movie m1, Movie m2) {
            String title1 = m1.getTitle().toLowerCase();
            String title2 = m2.getTitle().toLowerCase();

            // If prefix matches exactly, prioritize
            if (title1.startsWith(prefix) && !title2.startsWith(prefix)) {
                return -1;
            }
            if (!title1.startsWith(prefix) && title2.startsWith(prefix)) {
                return 1;
            }

            // If both match or neither matches, sort by title length
            // Shorter titles are usually more relevant
            return Integer.compare(title1.length(), title2.length());
        }
    }
} 