package models;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;
import models.Movie;

public class Autocomplete {
    // Trie 节点类
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

    // 插入电影到 Trie
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

    // 根据前缀搜索匹配的电影
    public List<Movie> search(String prefix) {
        return search(prefix, 10); // 默认返回10个结果
    }

    // 根据前缀搜索匹配的电影，限制返回数量
    public List<Movie> search(String prefix, int limit) {
        List<Movie> results = new ArrayList<>();
        TrieNode current = root;
        String lowerPrefix = prefix.toLowerCase();

        // 先找到前缀的最后一个节点
        for (int i = 0; i < lowerPrefix.length(); i++) {
            char c = lowerPrefix.charAt(i);
            
            if (!current.children.containsKey(c)) {
                return results; // 如果没有找到前缀，返回空列表
            }
            current = current.children.get(c);
        }

        // 从该节点开始，收集所有可能的电影
        collectMovies(current, results);

        // 按匹配度排序并限制返回数量
        results.sort(new MovieComparator(prefix));
        return results.subList(0, Math.min(limit, results.size()));
    }

    // 递归收集所有可能的电影
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

    // 电影比较器，用于按匹配度排序
    private static class MovieComparator implements Comparator<Movie> {
        private final String prefix;

        public MovieComparator(String prefix) {
            this.prefix = prefix.toLowerCase();
        }

        @Override
        public int compare(Movie m1, Movie m2) {
            String title1 = m1.getTitle().toLowerCase();
            String title2 = m2.getTitle().toLowerCase();

            // 如果前缀完全匹配，优先返回
            if (title1.startsWith(prefix) && !title2.startsWith(prefix)) {
                return -1;
            }
            if (!title1.startsWith(prefix) && title2.startsWith(prefix)) {
                return 1;
            }

            // 如果都完全匹配或都不完全匹配，按标题长度排序
            // 标题更短的优先（通常更接近用户想要的结果）
            return Integer.compare(title1.length(), title2.length());
        }
    }
} 