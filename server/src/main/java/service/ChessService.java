package service;

import model.*;
import requests.*;
import results.*;
import dataaccess.*;
import chess.ChessGame.TeamColor;

import java.util.Collection;

// Auto generated comments

public class ChessService {
    private final UserDAO userDAO;
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    /**
     * Constructs a chess service with specific data access components.
     * Establishes connections to the persistence layer through provided DAOs.
     * 
     * @param userDAO Repository for player account information
     * @param gameDAO Repository for chess match data storage
     * @param authDAO Repository for authentication token management
     */
    public ChessService(UserDAO userDAO, GameDAO gameDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    /**
     * Initializes a self-contained chess service using in-memory storage
     * mechanisms.
     * Creates volatile repositories suitable for testing or ephemeral sessions.
     */
    public ChessService() {
        this.userDAO = new UserDAOMemory();
        this.gameDAO = new GameDAOMemory();
        this.authDAO = new AuthDAOMemory();
    }

    /**
     * Purges all persistent data from the system repositories.
     * Resets the entire application state to its factory configuration.
     * 
     * @throws DataAccessException When data elimination operations encounter errors
     */
    public void clear() throws DataAccessException {
        userDAO.clear();
        gameDAO.clear();
        authDAO.clear();
    }

    /**
     * Establishes a new player account in the system.
     * Validates registration details, ensures uniqueness, and generates
     * authentication credentials.
     * 
     * @param request Candidate user details including identity and verification
     *                information
     * @return Authentication package containing user recognition credentials
     * @throws DataAccessException When persistence operations encounter errors
     */
    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        if (request.username() == null || request.password() == null || request.email() == null ||
                request.username().isEmpty() || request.password().isEmpty()) {
            throw new RuntimeException("Error: bad request");
        }

        if (userDAO.getUser(request.username()) != null) {
            throw new RuntimeException("Error: already taken");
        }

        UserData user = new UserData(request.username(), request.password(), request.email());
        userDAO.createUser(user);

        String authToken = authDAO.createAuth(request.username());

        return new RegisterResult(request.username(), authToken);
    }

    /**
     * Authenticates an existing user and establishes a session.
     * Validates provided credentials against stored values and creates a secure
     * token.
     * 
     * @param request User identification and verification information
     * @return Session establishment package with authentication token
     * @throws DataAccessException When persistence operations encounter errors
     */
    public LoginResult login(LoginRequest request) throws DataAccessException {
        if (request.username() == null || request.password() == null) {
            throw new RuntimeException("Error: bad request");
        }

        UserData user = userDAO.getUser(request.username());
        if (user == null || !user.password().equals(request.password())) {
            throw new RuntimeException("Error: unauthorized");
        }

        String authToken = authDAO.createAuth(request.username());

        return new LoginResult(request.username(), authToken);
    }

    /**
     * Terminates an active user session.
     * Invalidates the authentication token to prevent further authorized access.
     * 
     * @param request Session identification information
     * @return Confirmation of successful session termination
     * @throws DataAccessException When persistence operations encounter errors
     */
    public LogoutResult logout(LogoutRequest request) throws DataAccessException {
        AuthData authData = authDAO.getAuth(request.authToken());
        if (authData == null) {
            throw new RuntimeException("Error: unauthorized");
        }

        authDAO.deleteAuth(request.authToken());
        return new LogoutResult();
    }

    /**
     * Retrieves the complete catalog of available chess matches.
     * Provides authenticated users with access to the game collection.
     * 
     * @param request Session verification information
     * @return Comprehensive inventory of current chess games
     * @throws DataAccessException When retrieval operations encounter errors
     */
    public ListResult listAll(ListRequest request) throws DataAccessException {
        AuthData authData = authDAO.getAuth(request.authToken());
        if (authData == null) {
            throw new RuntimeException("Error: unauthorized");
        }

        Collection<GameData> games = gameDAO.listGames();

        return new ListResult(games);
    }

    /**
     * Initiates a fresh chess competition instance.
     * Establishes a new game environment with the specified identification.
     * 
     * @param request Game creation parameters with session verification
     * @return Game establishment confirmation with unique identifier
     * @throws DataAccessException When creation operations encounter errors
     */
    public NewGameResult newGame(NewGameRequest request) throws DataAccessException {
        AuthData authData = authDAO.getAuth(request.authToken());
        if (authData == null) {
            throw new RuntimeException("Error: unauthorized");
        }

        if (request.gameName() == null || request.gameName().isEmpty()) {
            throw new RuntimeException("Error: bad request");
        }

        int gameID = gameDAO.createGame(request.gameName());

        return new NewGameResult(String.valueOf(gameID));
    }

    /**
     * Enrolls a player into an existing chess match.
     * Assigns users to their requested team position if available.
     * 
     * @param request Game participation details with position preference
     * @return Enrollment confirmation with position assignment
     * @throws DataAccessException When participation operations encounter errors
     */
    public JoinResult joinGame(JoinRequest request) throws DataAccessException {
        AuthData authData = authDAO.getAuth(request.authToken());
        if (authData == null) {
            throw new RuntimeException("Error: unauthorized");
        }

        try {
            int gameID = request.gameID();
            GameData gameData = gameDAO.getGame(gameID);

            if (gameData == null) {
                throw new RuntimeException("Error: bad request");
            }

            TeamColor playerColor = request.playerColor();
            String username = authData.username();

            if (playerColor != null) {
                GameData updatedGame;

                switch (playerColor) {
                    case WHITE:
                        if (gameData.whiteUsername() != null) {
                            throw new RuntimeException("Error: already taken");
                        }
                        updatedGame = new GameData(
                                gameID,
                                username,
                                gameData.blackUsername(),
                                gameData.gameName(),
                                gameData.game());
                        break;
                    case BLACK:
                        if (gameData.blackUsername() != null) {
                            throw new RuntimeException("Error: already taken");
                        }
                        updatedGame = new GameData(
                                gameID,
                                gameData.whiteUsername(),
                                username,
                                gameData.gameName(),
                                gameData.game());
                        break;
                    default:
                        throw new RuntimeException("Error: bad request");
                }

                gameDAO.updateGame(gameID, updatedGame);
            }

            return new JoinResult(playerColor, gameData.gameName());

        } catch (NumberFormatException e) {
            throw new RuntimeException("Error: bad request");
        }
    }
}