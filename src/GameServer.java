import com.sun.net.httpserver.HttpServer;
import controllers.GameController;
import utils.DataLoader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        
        // 注册静态文件处理器
        server.createContext("/", exchange -> {
            String requestPath = exchange.getRequestURI().getPath();
            if (requestPath.equals("/")) {
                requestPath = "/index.html";
            }
            
            String projectPath = System.getProperty("user.dir");
            if (projectPath.endsWith("/bin")) {
                projectPath = projectPath.substring(0, projectPath.length() - 4);
            }
            
            Path filePath = Paths.get(projectPath, "web", requestPath);
            File file = filePath.toFile();
            
            if (file.exists() && !file.isDirectory()) {
                String contentType = getContentType(filePath.toString());
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, file.length());
                
                try (OutputStream os = exchange.getResponseBody();
                     FileInputStream fs = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = fs.read(buffer)) != -1) {
                        os.write(buffer, 0, count);
                    }
                }
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        });
        
        // 设置线程池
        server.setExecutor(null); // 使用默认执行器
    }
    
    /**
     * 获取文件类型
     */
    private String getContentType(String filePath) {
        if (filePath.endsWith(".html")) return "text/html";
        if (filePath.endsWith(".css")) return "text/css";
        if (filePath.endsWith(".js")) return "application/javascript";
        if (filePath.endsWith(".json")) return "application/json";
        if (filePath.endsWith(".png")) return "image/png";
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) return "image/jpeg";
        return "text/plain";
    }
    
    /**
     * 启动服务器
     */
    public void start() {
        if (server != null) {
            server.start();
            System.out.println("游戏服务器已启动，监听端口: " + port);
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
