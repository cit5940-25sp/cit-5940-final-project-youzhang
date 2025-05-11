import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import controllers.GameController;
import utils.DataLoader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * 游戏服务器，负责启动HTTP服务器和注册控制器
 */
public class GameServer {
    private final int port;
    private HttpServer server;
    
    /**
     * 构造函数
     */
    public GameServer(int port) {
        this.port = port;
    }
    
    /**
     * 初始化服务器
     */
    public void initialize() throws IOException {
        // 创建HTTP服务器
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // 加载电影数据
        try {
            DataLoader dataLoader = new DataLoader();
            String projectPath = System.getProperty("user.dir");
            // 如果当前目录是bin，则需要回到上一级目录
            if (projectPath.endsWith("/bin")) {
                projectPath = projectPath.substring(0, projectPath.length() - 4);
            }
            dataLoader.loadMoviesFromCsv(projectPath + "/src/movies.csv");
        } catch (IOException e) {
            System.err.println("加载电影数据时发生错误: " + e.getMessage());
        }
        
        // 注册控制器
        GameController gameController = new GameController();
        server.createContext("/api", gameController);
        

        
        // 设置线程池
        server.setExecutor(null); // 使用默认执行器
    }
    
    /**
     * 启动服务器
     */
    public void start() {
        if (server != null) {
            server.start();
            System.out.println("游戏服务器已启动，监听端口: " + port);
            System.out.println("服务器API地址: http://localhost:" + port + "/api");
        } else {
            System.err.println("服务器未初始化，请先调用initialize()方法");
        }
    }
    
    /**
     * 停止服务器
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("游戏服务器已停止");
        }
    }
    
    /**
     * 主方法，用于启动服务器
     */
    public static void main(String[] args) {
        try {
            int port = 8080;
            if (args.length > 0) {
                try {
                    port = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    System.err.println("无效的端口号，使用默认端口8080");
                }
            }
            
            GameServer gameServer = new GameServer(port);
            gameServer.initialize();
            gameServer.start();
            
            // 添加关闭钩子，确保服务器在JVM关闭时正常停止
            Runtime.getRuntime().addShutdownHook(new Thread(gameServer::stop));
            
        } catch (IOException e) {
            System.err.println("启动服务器时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
