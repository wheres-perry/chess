package service;

import model.*;
import requests.*;
import results.*;
import dataaccess.*;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.UserDAO;
import dataaccess.implementations.MemoryGameDAO;
import dataaccess.implementations.MemoryAuthDAO;
import dataaccess.implementations.MemoryUserDAO;
import dataaccess.implementations.MySQLGameDAO;
import dataaccess.implementations.MySQLAuthDAO;
import dataaccess.implementations.MySQLUserDAO;
import chess.ChessGame.TeamColor;

import java.util.Collection;

@SuppressWarnings("unused")
public class ChessService {
    private final UserDAO userDAO;
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    /**
     * Constructor with DAO dependencies.
     *
     * @param userDAO User data access.
     * @param gameDAO Game data access.
     * @param authDAO Auth data access.
     */
    public ChessService(UserDAO userDAO, GameDAO gameDAO, AuthDAO authDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
    }

    public ChessService() throws DataAccessException {
        UserDAO userStorage = new MySQLUserDAO();
        GameDAO gameStorage = new MySQLGameDAO();
        AuthDAO authStorage = new MySQLAuthDAO();

        this.userDAO = userStorage;
        this.gameDAO = gameStorage;
        this.authDAO = authStorage;
    }

    /**
     * Clear all data.
     *
     * @throws DataAccessException Data access error.
     */
    public void clear() throws DataAccessException {
        authDAO.clear();
        userDAO.clear();
        gameDAO.clear();
    }

    /**
     * Register new user.
     *
     * @param request Register request.
     * @return Register result.
     * @throws DataAccessException Data access error.
     */
    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        String playerName = request.username();
        String playerPass = request.password();
        String playerEmail = request.email();

        boolean invalidInput = playerName == null || playerPass == null || playerEmail == null ||
                playerName.isEmpty() || playerPass.isEmpty();

        if (invalidInput) {
            throw new RuntimeException("Error: bad request");
        }

        if (userDAO.getUser(playerName) != null) {
            throw new RuntimeException("Error: already taken");
        }

        UserData playerData = new UserData(playerName, playerPass, playerEmail);
        userDAO.createUser(playerData);

        String sessionToken = authDAO.createAuth(playerName);
        return new RegisterResult(playerName, sessionToken);
    }

    /**
     * Login user.
     *
     * @param request Login request.
     * @return Login result.
     * @throws DataAccessException Data access error.
     */
    public LoginResult login(LoginRequest request) throws DataAccessException {
        if (request.username() == null || request.password() == null) {
            throw new RuntimeException("Error: bad request");
        }

        UserData user = userDAO.getUser(request.username());
        if (user == null) {
            throw new RuntimeException("Error: unauthorized");
        }

        if (!userDAO.verifyPassword(request.username(), request.password())) {
            throw new RuntimeException("Error: unauthorized");
        }

        String authToken = authDAO.createAuth(request.username());
        return new LoginResult(request.username(), authToken);
    }

    /**
     * Logout user.
     *
     * @param request Logout request.
     * @return Logout result.
     * @throws DataAccessException Data access error.
     */
    public LogoutResult logout(LogoutRequest request) throws DataAccessException {
        String accessToken = request.authToken();
        AuthData authInfo = authDAO.getAuth(accessToken);

        if (authInfo == null) {
            throw new RuntimeException("Error: unauthorized");
        }

        authDAO.deleteAuth(accessToken);
        return new LogoutResult();
    }

    /**
     * List all games.
     *
     * @param request List request.
     * @return List result.
     * @throws DataAccessException Data access error.
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
     * Create new game.
     *
     * @param request New game request.
     * @return New game result.
     * @throws DataAccessException Data access error.
     */
    public NewGameResult newGame(NewGameRequest request) throws DataAccessException {
        String accessKey = request.authToken();
        String matchName = request.gameName();

        AuthData sessionData = authDAO.getAuth(accessKey);
        if (sessionData == null) {
            throw new RuntimeException("Error: unauthorized");
        }

        boolean emptyTitle = matchName == null || matchName.isEmpty();
        if (emptyTitle) {
            throw new RuntimeException("Error: bad request");
        }

        int gameID = gameDAO.createGame(matchName);
        return new NewGameResult(String.valueOf(gameID));
    }

    /**
     * Join existing game.
     *
     * @param request Join game request.
     * @return Join game result.
     * @throws DataAccessException Data access error.
     */
    public JoinResult joinGame(JoinRequest request) throws DataAccessException {
        try {
            AuthData authData = authDAO.getAuth(request.authToken());
            if (authData == null) {
                throw new RuntimeException("Error: unauthorized");
            }

            int gameID = request.gameID();
            GameData gameData = gameDAO.getGame(gameID);

            if (gameData == null) {
                throw new RuntimeException("Error: bad request");
            }

            TeamColor playerColor = request.playerColor();
            String username = authData.username();

            if (playerColor == null) {
                return new JoinResult(playerColor, gameData.gameName());
            }

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
            return new JoinResult(playerColor, gameData.gameName());

        } catch (NumberFormatException e) {
            throw new RuntimeException("Error: bad request");
        }
    }
}