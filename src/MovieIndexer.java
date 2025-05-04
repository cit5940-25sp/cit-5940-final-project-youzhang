import java.io.IOException;
import java.util.List;
import factories.ServiceFactory;
import services.MovieService;
import utils.DataLoader;
import models.Movie;
import models.Autocomplete;

public class MovieIndexer {
    private Autocomplete autocomplete;
    private MovieService movieService;

    public MovieIndexer() {
        this(new Autocomplete());
    }
    
    public MovieIndexer(Autocomplete autocomplete) {
        this.autocomplete = autocomplete;
        movieService = ServiceFactory.getMovieService();
    }

    // 使用DataLoader加载电影数据
    public void loadMoviesFromCSV(String csvFilePath) {
        try {
            // 使用DataLoader加载电影数据
            DataLoader dataLoader = new DataLoader(movieService);
            dataLoader.loadMoviesFromCsv(csvFilePath);
            
            // 将加载的电影添加到Autocomplete中
            for (Movie movie : movieService.getAllMovies()) {
                autocomplete.insert(movie);
            }
        } catch (IOException e) {
            System.err.println("读取CSV文件时出错: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("处理电影数据时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 搜索电影
    public List<Movie> searchMovies(String prefix) {
        return autocomplete.search(prefix);
    }

    // 设置返回结果的数量
    public List<Movie> searchMovies(String prefix, int limit) {
        return autocomplete.search(prefix, limit);
    }

} 