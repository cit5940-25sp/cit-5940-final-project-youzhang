[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/nK589Lr0)
[![Open in Visual Studio Code](https://classroom.github.com/assets/open-in-vscode-2e0aaae1b6195c2367325f4f02e2d04e9abb55f0b24a779b69b11b9e10269abc.svg)](https://classroom.github.com/online_ide?assignment_repo_id=18841678&assignment_repo_type=AssignmentRepo)
# Movie Chain Game

A terminal-based game where players take turns selecting movies that are connected to each other. The game features movie search functionality and power-ups like skip and block.

> **Note:** This is currently a demo version with basic functionality. A more comprehensive TUI (Text User Interface) is planned for future development.

## Prerequisites

- Java 8 or higher
- JSON Simple library (included in the lib directory)

## Project Structure

```
.
├── src/                    # Java source files
│   ├── controllers/       # Game controllers
│   ├── models/           # Data models
│   ├── services/         # Business logic
│   ├── utils/            # Utility classes
│   ├── GameServer.java   # Server entry point
│   └── GameTUI.java      # Text User Interface
├── test/                  # Test files
├── lib/                   # External libraries
└── bin/                   # Compiled Java classes
```

## How to Run

### Compile the Project

```bash
# Compile the server and game logic
javac -d bin src/**/*.java src/*.java

# Compile the TUI (requires JSON Simple library)
javac -cp .:lib/json-simple-1.1.1.jar src/GameTUI.java -d bin
```

### Start the Game Server

```bash
java -cp bin GameServer
```

### Start the TUI Client

```bash
java -cp bin:lib/json-simple-1.1.1.jar GameTUI
```

## Game Rules

1. Each player is assigned a target genre
2. Players take turns selecting movies
3. Each movie must be connected to the previous movie (share cast or crew)
4. Players can use power-ups:
   - Skip: Force the opponent to skip their next turn
   - Block: Prevent the opponent from selecting a movie on their next turn
5. First player to collect enough movies of their target genre wins
6. If a player doesn't select a movie within 30 seconds, they lose the game

## Features

- Movie search functionality
- Connection-based movie selection
- Power-ups for strategic gameplay
- Text-based user interface
- Turn-based gameplay with time limits
- Detailed game status updates

## Development

The game is built using:
- Java for both the backend server and TUI client
- JSON Simple for API communication
- Trie data structure for efficient movie search
- Factory pattern for service management

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

## TUI Implementation

The Text User Interface (TUI) is built using Java and provides a terminal-based way to interact with the game:

1. **User Interface**
   - Menu-driven interface for game actions
   - Text-based display of game state
   - Search functionality for finding movies
   - Special ability options
   - Turn information and game status

2. **TUI Architecture**
   - Command-based interaction model
   - HTTP-based API communication using Java's HttpURLConnection
   - JSON parsing for handling API responses
   - State tracking through local variables

3. **Key Features**
   - Movie search with results display
   - Turn-based gameplay management
   - Special ability usage
   - Game status checking
   - Timeout monitoring

## Technologies Used

- **Backend**: Java, HttpServer
- **TUI Client**: Java, JSON Simple
- **Data**: CSV file with movie information (4802 movies)
- **Communication**: RESTful API over HTTP

## Future Development

This is currently a demo version with the following planned improvements:

1. Enhanced TUI with more visual elements
2. Better error handling and user feedback
3. Offline mode with cached movie data
4. Multiplayer over network
5. AI opponents for single-player mode
