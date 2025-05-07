[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/nK589Lr0)
[![Open in Visual Studio Code](https://classroom.github.com/assets/open-in-vscode-2e0aaae1b6195c2367325f4f02e2d04e9abb55f0b24a779b69b11b9e10269abc.svg)](https://classroom.github.com/online_ide?assignment_repo_id=18841678&assignment_repo_type=AssignmentRepo)
# Final Project Template
Chenghao
Erlang
This is an example of a project directory for you to start working from. Please use it!


```text
|
|--- .gitignore # lists all of the junk that might exist in your folder that should not be committed
|--- README.md # explanation for the purpose of your repo
|--- src
    |----- *.java (source code files)
|--- test
    |----- *Test.java (unit test files)
|--- web
    |----- index.html (main game interface)
    |----- css/style.css (styling)
    |----- js/game.js (frontend logic)
```

## Backend Architecture

### Design Patterns

The backend is built using several design patterns to ensure a clean, maintainable architecture:

1. **Factory Pattern**
   - `ServiceFactory`: Creates and provides service instances
   - `ClientFactory`: Creates client/player instances
   - This pattern centralizes object creation and ensures proper initialization

2. **Service Layer Pattern**
   - `GameService`: Manages game state and core game logic
   - `MovieService`: Handles movie data and operations
   - `ClientService`: Manages player-related operations
   - Services encapsulate business logic and provide a clean API

3. **Model-View-Controller (MVC)**
   - Models: `Movie`, `Client`, `Tuple`
   - Controller: `GameController` handles HTTP requests
   - View: Handled by the frontend

4. **Singleton Pattern**
   - Service instances are managed as singletons through the `ServiceFactory`
   - Ensures consistent state across the application

### Key Components

1. **GameServer**
   - Entry point for the application
   - Sets up HTTP server and routes
   - Loads movie data from CSV

2. **GameController**
   - Handles HTTP requests and routes them to appropriate methods
   - Implements RESTful API endpoints
   - Formats data for frontend consumption

3. **Game Services**
   - `GameService`: Manages game state, player turns, and win conditions
   - `MovieService`: Loads and searches movie data, checks connections
   - `ClientService`: Manages player state and special abilities

4. **API Endpoints**
   - `/api/game/start`: Initialize a new game
   - `/api/game/status`: Get current game state
   - `/api/movies/search`: Search for movies
   - `/api/movies/select`: Select a movie
   - `/api/actions/skip`: Use skip ability
   - `/api/actions/block`: Use block ability
   - `/api/actions/next`: Move to next player

## Game Mechanics

1. **Movie Connections**
   - Movies are connected if they share cast or crew members
   - Players must select movies that connect to the previous movie

2. **Special Abilities**
   - **Skip**: Force the opponent to skip their next turn
   - **Block**: Prevent the opponent from selecting a movie on their next turn
   - Each player can use only one special ability per game

3. **Turn-Based Flow**
   - Players can select one movie per turn
   - Players can optionally use a special ability after selecting a movie
   - Players must click "Next Player" to end their turn
   - Players who are skipped or blocked can only click "Next Player"

4. **Win Condition**
   - First player to collect the specified number of movies in their target genre wins

## Frontend Implementation

The frontend is built using HTML, CSS, and vanilla JavaScript:

1. **User Interface**
   - Clean, responsive design
   - Player boards showing collected movies
   - Search functionality for finding movies
   - Special ability buttons
   - Turn information and game status

2. **JavaScript Architecture**
   - Event-driven programming model
   - Asynchronous API calls using fetch
   - State management through the gameState object
   - Dynamic UI updates based on game state

3. **Key Features**
   - Real-time UI updates
   - Movie search with instant results
   - Visual feedback for game actions
   - Automatic genre matching and counting
   - Responsive design for different screen sizes

## How to Run the Project

1. **Compile the Java code**
   ```bash
   javac -d bin -cp src src/*.java src/*/*.java
   ```

2. **Start the server**
   ```bash
   java -cp bin GameServer
   ```
   The server will start on port 8080 by default.

3. **Access the game**
   Open a web browser and navigate to:
   ```
   http://localhost:8080
   ```

4. **Game Setup**
   - Enter player names
   - Select target genres for each player
   - Set the win condition (number of target genre movies to collect)
   - Click "Start Game"

5. **Troubleshooting**
   - If you encounter "Address already in use" error, find and kill the process using port 8080:
     ```bash
     lsof -i :8080 | grep LISTEN
     kill -9 [PID]
     ```

## Technologies Used

- **Backend**: Java, HttpServer
- **Frontend**: HTML5, CSS3, JavaScript
- **Data**: CSV file with movie information (4802 movies)
