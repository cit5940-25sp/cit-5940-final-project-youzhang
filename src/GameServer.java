import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import controllers.GameController;
import utils.DataLoader;
import java.io.IOException;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.net.InetSocketAddress;

/**
 * Game server, responsible for starting the HTTP server and registering controllers
 */
public class GameServer {
    private final int port;
    private HttpServer server;
    
    /**
     * Constructor
     */
    public GameServer(int port) {
        this.port = port;
    }
    
    /**
     * Initialize the server
     */
    public void initialize() throws IOException {
        // Create HTTP server
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Load movie data
        try {
            DataLoader dataLoader = new DataLoader();
            String projectPath = System.getProperty("user.dir");
            // If current directory is bin, go back to parent directory
            if (projectPath.endsWith("/bin")) {
                projectPath = projectPath.substring(0, projectPath.length() - 4);
            }
            dataLoader.loadMoviesFromCsv(projectPath + "/src/movies.csv");
        } catch (IOException e) {
            System.err.println("Error loading movie data: " + e.getMessage());
        }
        
        // Register controller
        GameController gameController = new GameController();
        server.createContext("/api", gameController);
        
        // Register static file handler for web files
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            
            // Default to index.html
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            // Get the file from the web directory
            File file = new File("web" + path);
            
            if (!file.exists()) {
                String response = "404 (Not Found)";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }
            
            // Set content type
            String contentType = "text/html";
            if (path.endsWith(".css")) {
                contentType = "text/css";
            } else if (path.endsWith(".js")) {
                contentType = "text/javascript";
            }
            exchange.getResponseHeaders().set("Content-Type", contentType);
            
            // Send file content
            exchange.sendResponseHeaders(200, file.length());
            try (OutputStream os = exchange.getResponseBody()) {
                Files.copy(file.toPath(), os);
            }
        });
        
        // Set thread pool
        server.setExecutor(null); // Use default executor
    }
    
    /**
     * Start the server
     */
    public void start() {
        if (server != null) {
            server.start();
            System.out.println("Game server started, listening on port: " + port);
            System.out.println("Open http://localhost:" + port + " to play the game");
        } else {
            System.err.println("Server not initialized, please call initialize() method first");
        }
    }
    
    /**
     * Stop the server
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Game server stopped");
        }
    }
    
    /**
     * Main method for starting the server
     */
    public static void main(String[] args) {
        try {
            int port = 8080;
            if (args.length > 0) {
                try {
                    port = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port number, using default port 8080");
                }
            }
            
            GameServer gameServer = new GameServer(port);
            gameServer.initialize();
            gameServer.start();
            
            // Add shutdown hook to ensure server stops properly when JVM shuts down
            Runtime.getRuntime().addShutdownHook(new Thread(gameServer::stop));
            
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
