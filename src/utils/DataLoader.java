package utils;

import models.Movie;
import services.MovieService;
import factories.ServiceFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 数据加载工具类，用于从CSV文件中加载电影数据
 */
public class DataLoader {
    private MovieService movieService;
    
    /**
     * 构造函数
     */
    public DataLoader(MovieService movieService) {
        this.movieService = movieService;
    }
    
    /**
     * 无参构造函数，自动从ServiceFactory获取MovieService实例
     */
    public DataLoader() {
        this.movieService = ServiceFactory.getMovieService();
    }
    
    /**
     * 从CSV文件加载电影数据
     */
    public void loadMoviesFromCsv(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // 跳过标题行
            String line = reader.readLine();
            int lineCount = 1;
            int successCount = 0;
            int skipCount = 0;
            
            // 读取数据行
            while ((line = reader.readLine()) != null) {
                lineCount++;
                try {
                    Movie movie = MovieCsvParser.parseMovieLine(line);
                    if (movie != null) {
                        movieService.addMovie(movie);
                        successCount++;
                    } else {
                        System.err.println("跳过第" + lineCount + "行: 无法解析电影数据");
                        skipCount++;
                    }
                } catch (MovieCsvParser.MovieParseException e) {
                    // 只记录错误，不中断处理
                    System.err.println("跳过第" + lineCount + "行: " + e.getMessage());
                    skipCount++;
                }
            }
            
            System.out.println("电影数据加载完成: 总行数=" + (lineCount-1) + ", 成功=" + successCount + ", 跳过=" + skipCount);
        }
    }
}
