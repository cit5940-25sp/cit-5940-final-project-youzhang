package utils;

import factories.MovieFactory;
import models.Movie;
import models.Tuple;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 电影CSV解析器，专门用于解析特定格式的电影CSV文件
 */
public class MovieCsvParser {
    
    // 用于解析演员和剧组成员的正则表达式
    private static final Pattern CAST_PATTERN = Pattern.compile("\\('([^']+)', (\\d+)\\)");
    private static final Pattern GENRES_PATTERN = Pattern.compile("'([^']+)'");
    
    /**
     * 解析CSV行，创建电影对象
     * @throws MovieParseException 如果解析过程中出现错误
     */
    public static Movie parseMovieLine(String line) throws MovieParseException {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 将行分割为列，但保留引号内的逗号
            List<String> columns = splitCsvLine(line);
            
            // 确保有足够的列
            if (columns.size() < 6) {
                return null;
            }
            
            // 解析基本信息
            int rowNumber = parseIntOrDefault(columns.get(0), 0);
            String genresStr = columns.get(1);
            int id = parseIntOrDefault(columns.get(2), rowNumber); // 如果id为空，使用行号
            String releaseDateStr = columns.get(3);
            String title = columns.get(4);
            String castStr = columns.get(5);
            String crewStr = columns.size() > 6 ? columns.get(6) : "";
            
            // 解析发行年份
            int releaseYear = parseReleaseYear(releaseDateStr);
            
            // 解析类型
            Set<String> genres = parseGenres(genresStr);
            
            // 解析演员
            List<Tuple<String, Integer>> cast = parseCastOrCrew(castStr);
            
            // 解析剧组
            List<Tuple<String, Integer>> crew = parseCastOrCrew(crewStr);
            
            // 创建电影对象
            return MovieFactory.createMovie(title, id, releaseYear, genres, cast, crew);
            
        } catch (Exception e) {
            if (e instanceof MovieParseException) {
                throw (MovieParseException) e;
            }
            return null;
        }
    }
    
    /**
     * 分割CSV行，保留引号内的逗号
     */
    private static List<String> splitCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentColumn = new StringBuilder();
        
        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentColumn.toString());
                currentColumn = new StringBuilder();
            } else {
                currentColumn.append(c);
            }
        }
        
        result.add(currentColumn.toString());
        return result;
    }
    
    /**
     * 解析整数，如果解析失败则返回默认值
     */
    private static int parseIntOrDefault(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 从日期字符串解析年份
     */
    private static int parseReleaseYear(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return 0;
        }
        
        // 尝试解析YYYY-MM-DD格式
        if (dateStr.length() >= 4) {
            try {
                return Integer.parseInt(dateStr.substring(0, 4));
            } catch (NumberFormatException e) {
                // 忽略错误，继续尝试其他格式
            }
        }
        
        // 如果无法解析，返回0
        return 0;
    }
    
    /**
     * 解析电影类型
     */
    private static Set<String> parseGenres(String genresStr) {
        Set<String> genres = new HashSet<>();
        
        if (genresStr == null || genresStr.isEmpty()) {
            return genres;
        }
        
        // 使用正则表达式匹配所有类型
        Matcher matcher = GENRES_PATTERN.matcher(genresStr);
        while (matcher.find()) {
            genres.add(matcher.group(1));
        }
        
        return genres;
    }
    
    /**
     * 解析演员或剧组成员
     */
    private static List<Tuple<String, Integer>> parseCastOrCrew(String str) {
        List<Tuple<String, Integer>> result = new ArrayList<>();
        
        if (str == null || str.isEmpty()) {
            return result;
        }
        
        // 使用正则表达式匹配所有演员/剧组成员
        Matcher matcher = CAST_PATTERN.matcher(str);
        while (matcher.find()) {
            String name = matcher.group(1);
            int id = Integer.parseInt(matcher.group(2));
            result.add(new Tuple<>(name, id));
        }
        
        return result;
    }
    
    /**
     * 电影解析异常类
     */
    public static class MovieParseException extends Exception {
        public MovieParseException(String message) {
            super(message);
        }
        
        public MovieParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
