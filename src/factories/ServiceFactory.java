package factories;

import services.ClientService;
import services.GameService;
import services.MovieService;

/**
 * factory class, responsible for creating and managing service instances
 */
public class ServiceFactory {
    private static ClientService clientService;
    private static MovieService movieService;
    private static GameService gameService;
    
    /**
     * get ClientService instance
     */
    public static ClientService getClientService() {
        if (clientService == null) {
            clientService = new ClientService();
        }
        return clientService;
    }
    
    /**
     * get MovieService instance
     */
    public static MovieService getMovieService() {
        if (movieService == null) {
            movieService = new MovieService();
        }
        return movieService;
    }
    
    /**
     * get GameService instance
     */
    public static GameService getGameService() {
        if (gameService == null) {
            ClientService clientSvc = getClientService();
            MovieService movieSvc = getMovieService();
            gameService = new GameService(clientSvc, movieSvc);
        }
        return gameService;
    }
}
