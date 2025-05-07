// Game state
const gameState = {
    currentPlayerIndex: 0,
    turnCount: 1,
    players: [],
    lastMovie: null,
    gameOver: false,
    winner: null
};

// API base URL
const API_BASE_URL = 'http://localhost:8080/api';

// DOM elements
const elements = {
    // Game setup
    setupForm: document.getElementById('setup-form'),
    player1Name: document.getElementById('player1-name'),
    player1Genre: document.getElementById('player1-genre'),
    player2Name: document.getElementById('player2-name'),
    player2Genre: document.getElementById('player2-genre'),
    winThreshold: document.getElementById('win-threshold'),
    startGameBtn: document.getElementById('start-game'),
    
    // Game panels
    gameSetup: document.getElementById('game-setup'),
    gameBoard: document.getElementById('game-board'),
    gameOver: document.getElementById('game-over'),
    
    // Game info
    turnCount: document.getElementById('turn-count'),
    currentPlayer: document.getElementById('current-player'),
    lastMovieSection: document.getElementById('last-movie'),
    lastMovieInfo: document.getElementById('last-movie-info'),
    
    // Player panels
    player1NameDisplay: document.getElementById('player1-name-display'),
    player1GenreDisplay: document.getElementById('player1-genre-display'),
    player1Count: document.getElementById('player1-count'),
    player1Threshold: document.getElementById('player1-threshold'),
    player1Movies: document.getElementById('player1-movies'),
    player1Board: document.getElementById('player1-board'),
    
    player2NameDisplay: document.getElementById('player2-name-display'),
    player2GenreDisplay: document.getElementById('player2-genre-display'),
    player2Count: document.getElementById('player2-count'),
    player2Threshold: document.getElementById('player2-threshold'),
    player2Movies: document.getElementById('player2-movies'),
    player2Board: document.getElementById('player2-board'),
    
    // Search
    movieSearch: document.getElementById('movie-search'),
    searchButton: document.getElementById('search-button'),
    searchResults: document.getElementById('search-results'),
    
    // Special abilities
    skipButton: document.getElementById('skip-button'),
    blockButton: document.getElementById('block-button'),
    nextPlayerButton: document.getElementById('next-player-button'),
    
    // Game over
    winnerName: document.getElementById('winner-name'),
    newGameBtn: document.getElementById('new-game'),
    
    // Message
    message: document.getElementById('message')
};

// Event listeners
function setupEventListeners() {
    // Start game button
    elements.startGameBtn.addEventListener('click', startGame);
    
    // Search button
    elements.searchButton.addEventListener('click', searchMovies);
    
    // Enter key search
    elements.movieSearch.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            searchMovies();
        }
    });
    
    // Special ability buttons
    elements.skipButton.addEventListener('click', useSkipAbility);
    elements.blockButton.addEventListener('click', useBlockAbility);
    elements.nextPlayerButton.addEventListener('click', nextPlayer);
    
    // New game button
    elements.newGameBtn.addEventListener('click', resetGame);
}

// API request function
async function apiRequest(endpoint, method = 'GET', data = null) {
    const url = `${API_BASE_URL}${endpoint}`;
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json'
        }
    };
    
    if (data && (method === 'POST' || method === 'PUT')) {
        options.body = JSON.stringify(data);
    }
    
    try {
        const response = await fetch(url, options);
        const responseData = await response.json();
        
        if (!response.ok) {
            throw new Error(responseData.message || 'Request failed');
        }
        
        return responseData;
    } catch (error) {
        console.error('API request error:', error);
        showMessage(error.message, true);
        throw error;
    }
}

// Perform game action and update state
async function performGameAction(action, endpoint, method = 'POST', data = null) {
    try {
        // Send API request
        await apiRequest(endpoint, method, data);
        
        // Get latest game state
        const gameStatus = await getGameStatus();
        
        // Log game state
        console.log(`${action} game state:`, gameStatus);
        
        return gameStatus;
    } catch (error) {
        console.error(`${action} failed:`, error);
        showMessage(`${action} failed: ${error.message || 'Unknown error'}`, true);
        throw error;
    }
}

// Start game
async function startGame() {
    try {
        // Get form data
        const player1Name = elements.player1Name.value;
        const player1Genre = elements.player1Genre.value;
        const player2Name = elements.player2Name.value;
        const player2Genre = elements.player2Genre.value;
        const winThreshold = elements.winThreshold.value;
        
        // Game data
        const gameData = {
            player1Name,
            player1Genre,
            player2Name,
            player2Genre,
            winThreshold
        };
        
        // Perform start game action
        const response = await apiRequest('/game/start', 'POST', gameData);
        
        // Update initial game state
        updateGameState(response.data);
        
        // Show game board
        elements.gameSetup.classList.add('hidden');
        elements.gameBoard.classList.remove('hidden');
        
        // Update UI
        updateUI();
        
        // Get latest game state
        await performGameAction('Game started', '/game/status', 'GET');
        
        showMessage('Game started!');
    } catch (error) {
        console.error('Start game failed:', error);
        showMessage('Start game failed: ' + (error.message || 'Unknown error'), true);
    }
}

// Search movies
async function searchMovies() {
    try {
        const query = elements.movieSearch.value.trim();
        
        if (!query) {
            showMessage('Please enter search keywords', true);
            return;
        }
        
        // Send search request
        const response = await apiRequest(`/movies/search?q=${encodeURIComponent(query)}`);
        
        // Display search results
        displaySearchResults(response.data.movies);
    } catch (error) {
        console.error('Search movies failed:', error);
    }
}

// Display search results
function displaySearchResults(movies) {
    elements.searchResults.innerHTML = '';
    
    if (!movies || movies.length === 0) {
        elements.searchResults.innerHTML = '<p>No matching movies found</p>';
        return;
    }
    
    movies.forEach(movie => {
        const movieElement = document.createElement('div');
        movieElement.className = 'movie-item';
        movieElement.innerHTML = `
            <h4>${movie.title} (${movie.releaseYear})</h4>
            <p>Genre: ${Array.isArray(movie.genre) ? movie.genre.join(', ') : movie.genre}</p>
        `;
        
        // Add click event
        movieElement.addEventListener('click', () => selectMovie(movie.id));
        
        elements.searchResults.appendChild(movieElement);
    });
}

// Select movie
async function selectMovie(movieId) {
    try {
        // Get current player
        const currentPlayer = gameState.players[gameState.currentPlayerIndex];
        
        // Check if player is blocked
        if (currentPlayer.isBlocked) {
            showMessage('You are blocked from taking action!', true);
            return;
        }
        
        // Perform select movie action
        await performGameAction('Select movie', `/movies/select?id=${movieId}`, 'POST');
        
        // Clear search box and results
        elements.movieSearch.value = '';
        elements.searchResults.innerHTML = '';
        
        // Show success message
        showMessage('Movie selected successfully!');
    } catch (error) {
        // Error handled in performGameAction
    }
}

// Use skip ability
async function useSkipAbility() {
    try {
        // Perform skip action
        await performGameAction('Skip opponent turn', '/actions/skip', 'POST');
        
        // Show success message
        showMessage('Successfully skipped opponent turn!');
    } catch (error) {
        // Error handled in performGameAction
    }
}

// Use block ability
async function useBlockAbility() {
    try {
        // Perform block action
        await performGameAction('Block opponent action', '/actions/block', 'POST');
        
        // Show success message
        showMessage('Successfully blocked opponent action!');
    } catch (error) {
        // Error handled in performGameAction
    }
}

// Use next player ability
async function nextPlayer() {
    try {
        // Perform next player action
        await performGameAction('Next player', '/actions/next', 'POST');
        
        // Show success message
        showMessage('Switched to next player!');
    } catch (error) {
        // Error handled in performGameAction
    }
}

// Get game status
async function getGameStatus() {
    try {
        const response = await apiRequest('/game/status');
        
        // Log game status response data
        console.log('Game status response data:', response.data);
        
        // Update game state
        updateGameState(response.data);
        
        // Update UI
        updateUI();
        
        // Check if game is over
        if (gameState.gameOver) {
            endGame();
        }
        
        return response.data;
    } catch (error) {
        console.error('Get game status failed:', error);
        showMessage('Get game status failed', true);
        throw error;
    }
}

// Update game state
function updateGameState(data) {
    // Update player info
    if (data.players) {
        gameState.players = data.players;
    }
    
    // Update current player
    if (data.currentPlayerIndex !== undefined) {
        gameState.currentPlayerIndex = data.currentPlayerIndex;
    } else if (data.currentPlayer) {
        // Find current player index by name
        const currentPlayerName = data.currentPlayer.name;
        gameState.currentPlayerIndex = gameState.players.findIndex(p => p.name === currentPlayerName);
    }
    
    // Update turn count
    if (data.turnCount !== undefined) {
        gameState.turnCount = data.turnCount;
    }
    
    // Update last movie
    if (data.lastMovie) {
        gameState.lastMovie = data.lastMovie;
    }
    
    // Update game over state
    if (data.gameOver !== undefined) {
        gameState.gameOver = data.gameOver;
    }
    
    // Update winner
    if (data.winner) {
        gameState.winner = data.winner;
    }
}

// Calculate target genre count
function calculateGenreCount(player) {
    if (!player || !player.movies || !player.movies.length) {
        return 0;
    }
    
    // Use targetGenre or winGenre, depending on backend response
    const targetGenre = player.targetGenre || player.winGenre;
    console.log('Calculating target genre count:', player.name, 'target genre:', targetGenre);
    console.log('Player movie list:', player.movies);
    
    // Check each movie's genre format
    player.movies.forEach((movie, index) => {
        console.log(`Movie ${index + 1}: ${movie.title}, genre:`, movie.genre, 'genre is array:', Array.isArray(movie.genre));
    });
    
    const matchingMovies = player.movies.filter(movie => {
        if (!movie.genre) {
            console.log(`Movie ${movie.title} has no genre information`);
            return false;
        }
        
        let isMatch = false;
        
        if (Array.isArray(movie.genre)) {
            isMatch = movie.genre.includes(targetGenre);
            console.log(`Movie ${movie.title} genre is array, includes target genre ${targetGenre}: ${isMatch}`);
        } else if (typeof movie.genre === 'string') {
            // Handle string genre format
            isMatch = movie.genre.toLowerCase() === targetGenre.toLowerCase() || 
                   movie.genre.split(',').map(g => g.trim().toLowerCase()).includes(targetGenre.toLowerCase());
            console.log(`Movie ${movie.title} genre is string "${movie.genre}", matches target genre ${targetGenre}: ${isMatch}`);
        } else {
            // Handle object genre format
            console.log(`Movie ${movie.title} genre is object:`, movie.genre);
            try {
                // Try to convert object to string array
                const genreArray = Array.from(movie.genre);
                isMatch = genreArray.some(g => g.toLowerCase() === targetGenre.toLowerCase());
                console.log(`Movie ${movie.title} genre converted to array, matches target genre ${targetGenre}: ${isMatch}`);
            } catch (error) {
                console.error(`Failed to handle movie ${movie.title} genre:`, error);
            }
        }
        
        return isMatch;
    });
    
    console.log(`Player ${player.name} target genre count:`, matchingMovies.length);
    return matchingMovies.length;
}

// Update UI
function updateUI() {
    console.log('Updating UI, current game state:', gameState);
    
    // Update turn count
    elements.turnCount.textContent = gameState.turnCount;
    
    // Update current player
    if (gameState.players.length > 0 && gameState.currentPlayerIndex >= 0) {
        const currentPlayer = gameState.players[gameState.currentPlayerIndex];
        console.log('Current player:', currentPlayer);
        elements.currentPlayer.textContent = currentPlayer.name;
        
        // Highlight current player panel
        if (gameState.currentPlayerIndex === 0) {
            elements.player1Board.classList.add('active-player');
            elements.player2Board.classList.remove('active-player');
        } else {
            elements.player1Board.classList.remove('active-player');
            elements.player2Board.classList.add('active-player');
        }
        
        // Update special ability buttons
        elements.skipButton.disabled = !currentPlayer.skipAvailable;
        elements.blockButton.disabled = !currentPlayer.blockAvailable;
        
        // Show player status information (skipped or blocked)
        let statusMessage = '';
        if (currentPlayer.isSkipped) {
            statusMessage = 'Current player turn skipped!';
            elements.skipButton.disabled = true;
            elements.blockButton.disabled = true;
            elements.searchButton.disabled = true;
            elements.movieSearch.disabled = true;
            
            // Show message but don't auto-proceed
            showMessage('Player ' + currentPlayer.name + ' turn skipped! Click "Next Player" to continue.');
        } else if (currentPlayer.isBlocked) {
            statusMessage = 'Current player blocked from taking action!';
            elements.skipButton.disabled = true;
            elements.blockButton.disabled = true;
            elements.searchButton.disabled = true;
            elements.movieSearch.disabled = true;
            
            // Show message but don't auto-proceed
            showMessage('Player ' + currentPlayer.name + ' blocked from taking action! Click "Next Player" to continue.');
        } else if (currentPlayer.hasSelectedMovie) {
            // Player has already selected a movie this turn, disable search functionality
            statusMessage = 'You have already selected a movie this turn!';
            elements.searchButton.disabled = true;
            elements.movieSearch.disabled = true;
            
            // Show message
            showMessage('You have already selected a movie this turn. Use special abilities or click "Next Player" to continue.');
        } else {
            // Normal turn, enable search functionality
            elements.searchButton.disabled = false;
            elements.movieSearch.disabled = false;
        }
        
        if (statusMessage) {
            showMessage(statusMessage);
        }
    }
    
    // Update last movie information
    if (gameState.lastMovie) {
        elements.lastMovieSection.classList.remove('hidden');
        elements.lastMovieInfo.innerHTML = `
            <h4>${gameState.lastMovie.title} (${gameState.lastMovie.releaseYear})</h4>
            <p>Genre: ${Array.isArray(gameState.lastMovie.genre) ? 
                gameState.lastMovie.genre.join(', ') : gameState.lastMovie.genre}</p>
        `;
    } else {
        elements.lastMovieSection.classList.add('hidden');
    }
    
    // Update player 1 information
    if (gameState.players.length > 0) {
        const player1 = gameState.players[0];
        console.log('Player 1 information:', player1);
        
        if (elements.player1NameDisplay) {
            elements.player1NameDisplay.textContent = player1.name;
        } else {
            console.error('Failed to find player1NameDisplay element');
        }
        
        if (elements.player1GenreDisplay) {
            elements.player1GenreDisplay.textContent = player1.targetGenre || player1.winGenre;
        } else {
            console.error('Failed to find player1GenreDisplay element');
        }
        
        if (elements.player1Threshold) {
            elements.player1Threshold.textContent = player1.winThreshold;
        } else {
            console.error('Failed to find player1Threshold element');
        }
        
        // Calculate target genre count
        if (elements.player1Count) {
            // Prioritize backend-provided targetGenreCount, otherwise use frontend-calculated value
            elements.player1Count.textContent = player1.targetGenreCount !== undefined ? 
                player1.targetGenreCount : calculateGenreCount(player1);
        } else {
            console.error('Failed to find player1Count element');
        }
        
        // Display player 1 movies
        if (elements.player1Movies) {
            displayPlayerMovies(player1, elements.player1Movies);
        } else {
            console.error('Failed to find player1Movies element');
        }
    }
    
    // Update player 2 information
    if (gameState.players.length > 1) {
        const player2 = gameState.players[1];
        console.log('Player 2 information:', player2);
        
        if (elements.player2NameDisplay) {
            elements.player2NameDisplay.textContent = player2.name;
        } else {
            console.error('Failed to find player2NameDisplay element');
        }
        
        if (elements.player2GenreDisplay) {
            elements.player2GenreDisplay.textContent = player2.targetGenre || player2.winGenre;
        } else {
            console.error('Failed to find player2GenreDisplay element');
        }
        
        if (elements.player2Threshold) {
            elements.player2Threshold.textContent = player2.winThreshold;
        } else {
            console.error('Failed to find player2Threshold element');
        }
        
        // Calculate target genre count
        if (elements.player2Count) {
            // Prioritize backend-provided targetGenreCount, otherwise use frontend-calculated value
            elements.player2Count.textContent = player2.targetGenreCount !== undefined ? 
                player2.targetGenreCount : calculateGenreCount(player2);
        } else {
            console.error('Failed to find player2Count element');
        }
        
        // Display player 2 movies
        if (elements.player2Movies) {
            displayPlayerMovies(player2, elements.player2Movies);
        } else {
            console.error('Failed to find player2Movies element');
        }
    }
    
    console.log('UI update complete');
}

// Display player movies
function displayPlayerMovies(player, container) {
    container.innerHTML = '';
    
    if (!player.movies || player.movies.length === 0) {
        container.innerHTML = '<p>No movies collected</p>';
        return;
    }
    
    player.movies.forEach(movie => {
        const movieElement = document.createElement('div');
        movieElement.className = 'player-movie';
        
        // Check if movie matches target genre
        const isTargetGenre = Array.isArray(movie.genre) ? 
            movie.genre.includes(player.targetGenre || player.winGenre) : movie.genre === player.targetGenre || movie.genre === player.winGenre;
        
        const genreDisplay = Array.isArray(movie.genre) ? movie.genre.join(', ') : movie.genre;
        
        movieElement.innerHTML = `
            <h4>${movie.title} (${movie.releaseYear})</h4>
            <p>Genre: ${isTargetGenre ? 
                `<span class="target-genre">${genreDisplay}</span>` : genreDisplay}</p>
        `;
        
        container.appendChild(movieElement);
    });
}

// End game
function endGame() {
    elements.gameBoard.classList.add('hidden');
    elements.gameOver.classList.remove('hidden');
    
    if (gameState.winner) {
        elements.winnerName.textContent = gameState.winner.name;
    }
}

// Reset game
function resetGame() {
    elements.gameOver.classList.add('hidden');
    elements.gameSetup.classList.remove('hidden');
    
    // Clear search and results
    elements.movieSearch.value = '';
    elements.searchResults.innerHTML = '';
}

// Show message
function showMessage(text, isError = false) {
    elements.message.textContent = text;
    elements.message.classList.remove('hidden');
    
    if (isError) {
        elements.message.classList.add('error');
    } else {
        elements.message.classList.remove('error');
    }
    
    // Hide message after 3 seconds
    setTimeout(() => {
        elements.message.classList.add('hidden');
    }, 3000);
}

// Initialize
function init() {
    // Check if DOM elements are correctly retrieved
    console.log('DOM element check:');
    console.log('player1NameDisplay:', elements.player1NameDisplay);
    console.log('player1GenreDisplay:', elements.player1GenreDisplay);
    console.log('player1Count:', elements.player1Count);
    console.log('player1Threshold:', elements.player1Threshold);
    console.log('player1Movies:', elements.player1Movies);
    
    setupEventListeners();
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', init);
