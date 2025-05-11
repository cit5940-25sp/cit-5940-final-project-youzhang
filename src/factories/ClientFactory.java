package factories;

import models.Client;
import services.MovieService;
import factories.ServiceFactory;

import java.util.Set;

/**
 * 工厂类，负责创建客户端/玩家对象
 */
public class ClientFactory {
    
    /**
     * 创建一个新的客户端/玩家对象
     * @param name 玩家名称
     * @param winGenre 玩家目标类型
     * @param winThreshold 胜利所需的电影数量
     * @return 新创建的Client对象
     * @throws IllegalArgumentException 如果目标类型不存在于电影数据库中
     */
    public static Client createClient(String name, String winGenre, int winThreshold) {
        // 验证目标类型是否存在
        validateGenre(winGenre);
        return new Client(name, winGenre, winThreshold);
    }
    
    /**
     * 验证目标类型是否存在于电影数据库中
     * @param genre 需要验证的目标类型
     * @throws IllegalArgumentException 如果目标类型不存在于电影数据库中
     */
    private static void validateGenre(String genre) {
        System.out.println("[DEBUG] 开始验证类型: " + genre);
        
        if (genre == null || genre.trim().isEmpty()) {
            System.out.println("[DEBUG] 类型为空");
            throw new IllegalArgumentException("目标类型不能为空");
        }
        
        // 获取MovieService实例
        MovieService movieService = ServiceFactory.getMovieService();
        System.out.println("[DEBUG] 获取到MovieService实例: " + (movieService != null ? "成功" : "失败"));
        
        // 获取所有可用的电影类型
        Set<String> availableGenres = movieService.getAllGenres();
        System.out.println("[DEBUG] 获取到电影类型数量: " + availableGenres.size());
        System.out.println("[DEBUG] 可用类型: " + availableGenres);
        
        // 检查目标类型是否存在
        if (availableGenres.isEmpty()) {
            System.out.println("[DEBUG] 电影数据库为空");
            throw new IllegalArgumentException("电影数据库为空，无法验证目标类型");
        }
        
        // 将输入的类型转换为小写进行比较
        String inputGenre = genre.trim();
        System.out.println("[DEBUG] 处理后的输入类型: " + inputGenre);
        
        // 打印所有可用类型供调试
        System.out.println("[DEBUG] 所有可用类型:");
        for (String availableGenre : availableGenres) {
            System.out.println("  - " + availableGenre);
        }
        
        // 检查目标类型是否存在于可用类型列表中
        // 使用严格的完全匹配，而不是部分匹配
        boolean genreExists = false;
        for (String availableGenre : availableGenres) {
            // 忽略大小写进行完全匹配
            if (availableGenre.equalsIgnoreCase(inputGenre)) {
                genreExists = true;
                System.out.println("[DEBUG] 找到完全匹配的类型: " + availableGenre);
                break;
            }
        }
        
        // 如果没有找到完全匹配，抛出异常
        if (!genreExists) {
            System.out.println("[DEBUG] 类型不存在于数据库中");
            System.out.println("[DEBUG] 有效的类型包括: " + String.join(", ", availableGenres));
            throw new IllegalArgumentException("目标类型 '" + genre + "' 不存在于电影数据库中。有效的类型包括: " + String.join(", ", availableGenres));
        }
        
        System.out.println("[DEBUG] 类型验证成功");
    }
}
