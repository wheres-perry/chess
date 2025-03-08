package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.*;
import results.*;

import static org.junit.jupiter.api.Assertions.*;
import chess.ChessGame.TeamColor;

@SuppressWarnings("unused")
public class ChessServiceTest {

    private ChessService services;
    static private final String SAMPLE_USERNAME = "Test_User";
    static private final String SAMPLE_PASSWORD = "passwordisabadpassword";
    static private final String SAMPLE_EMAIL = "user@myspace.com";
    static private final String SAMPLE_GAME_TITLE = "World Chess Championship";

    // Start each test with a clean state
    @BeforeEach
    public void initializeTestEnvironment() throws DataAccessException {
        services = new ChessService();
        services.clear();
    }

    @Test
    public void testClearRemovesAllData() throws DataAccessException {
        // Setup: register a user, create a game, and join it
        RegisterResult userRegistration = services
                .register(new RegisterRequest(SAMPLE_USERNAME, SAMPLE_PASSWORD, SAMPLE_EMAIL));
        NewGameResult createdGame = services
                .newGame(new NewGameRequest(SAMPLE_GAME_TITLE, userRegistration.authToken()));

        services.clear();

        assertThrows(RuntimeException.class, () -> services.listAll(new ListRequest(userRegistration.authToken())));
    }

    @Test
    public void testRegisterCreatesNewUser() throws DataAccessException {
        RegisterResult registrationResponse = services
                .register(new RegisterRequest(SAMPLE_USERNAME, SAMPLE_PASSWORD, SAMPLE_EMAIL));

        assertNotNull(registrationResponse);
        assertEquals(SAMPLE_USERNAME, registrationResponse.username());
        assertNotNull(registrationResponse.authToken());
        assertFalse(registrationResponse.authToken().isEmpty());
    }

    @Test
    public void testRegisterRejectsDuplicateUsername() throws DataAccessException {
        // Setup: register once successfully
        services.register(new RegisterRequest(SAMPLE_USERNAME, SAMPLE_PASSWORD, SAMPLE_EMAIL));

        assertThrows(RuntimeException.class,
                () -> services.register(new RegisterRequest(SAMPLE_USERNAME, SAMPLE_PASSWORD, SAMPLE_EMAIL)));
    }

    @Test
    public void testLoginAuthenticatesValidUser() throws DataAccessException {
        // Setup: register first
        services.register(new RegisterRequest(SAMPLE_USERNAME, SAMPLE_PASSWORD, SAMPLE_EMAIL));

        LoginResult loginResponse = services.login(new LoginRequest(SAMPLE_USERNAME, SAMPLE_PASSWORD));

        assertNotNull(loginResponse);
        assertEquals(SAMPLE_USERNAME, loginResponse.username());
        assertNotNull(loginResponse.authToken());
        assertFalse(loginResponse.authToken().isEmpty());
    }

    @Test
    public void testLoginRejectsInvalidPassword() throws DataAccessException {
        // Setup: register with correct password
        services.register(new RegisterRequest(SAMPLE_USERNAME, SAMPLE_PASSWORD, SAMPLE_EMAIL));

        assertThrows(RuntimeException.class,
                () -> services.login(new LoginRequest(SAMPLE_USERNAME, "incorrectPassword")));
    }

    @Test
    public void testLogoutInvalidatesAuthToken() throws DataAccessException {
        // Setup: register and login to get valid auth token
        RegisterResult userRegistration = services
                .register(new RegisterRequest(SAMPLE_USERNAME, SAMPLE_PASSWORD, SAMPLE_EMAIL));
        String sessionToken = userRegistration.authToken();

        LogoutResult logoutResponse = services.logout(new LogoutRequest(sessionToken));

        assertNotNull(logoutResponse);
        assertThrows(RuntimeException.class, () -> services.listAll(new ListRequest(sessionToken)));
    }

    @Test
    public void testLogoutFailsWithBadToken() {
        // Attempt logout with invalid token
        assertThrows(RuntimeException.class, () -> services.logout(new LogoutRequest("nonexistentToken")));
    }

    @Test
    public void testListAllReturnsCreatedGames() throws DataAccessException {
        // Setup: register, create a game
        RegisterResult userRegistration = services
                .register(new RegisterRequest(SAMPLE_USERNAME, SAMPLE_PASSWORD, SAMPLE_EMAIL));
        services.newGame(new NewGameRequest(SAMPLE_GAME_TITLE, userRegistration.authToken()));

        ListResult gameCollection = services.listAll(new ListRequest(userRegistration.authToken()));

        assertNotNull(gameCollection);
        assertNotNull(gameCollection.games());
        assertEquals(1, gameCollection.games().size());
        assertEquals(SAMPLE_GAME_TITLE, gameCollection.games().iterator().next().gameName());
    }

    @Test
    public void testListAllRejectsInvalidAuth() {
        assertThrows(RuntimeException.class, () -> services.listAll(new ListRequest("invalidSessionKey")));
    }

    @Test
    public void testNewGameCreatesGameEntry() throws DataAccessException {
        // Setup: register to get valid auth token
        RegisterResult userRegistration = services
                .register(new RegisterRequest(SAMPLE_USERNAME, SAMPLE_PASSWORD, SAMPLE_EMAIL));

        NewGameResult createdGameInfo = services
                .newGame(new NewGameRequest(SAMPLE_GAME_TITLE, userRegistration.authToken()));

        assertNotNull(createdGameInfo);
        assertNotNull(createdGameInfo.gameID());
        assertFalse(createdGameInfo.gameID().isEmpty());

        // Verify game is in database
        ListResult gameCollection = services.listAll(new ListRequest(userRegistration.authToken()));
        boolean matchFoundInDatabase = false;
        for (GameData gameRecord : gameCollection.games()) {
            if (gameRecord.gameName().equals(SAMPLE_GAME_TITLE)) {
                matchFoundInDatabase = true;
                break;
            }
        }
        assertTrue(matchFoundInDatabase);
    }

    @Test
    public void testNewGameRejectsEmptyTitle() throws DataAccessException {
        // Setup: register to get valid auth token
        RegisterResult userRegistration = services
                .register(new RegisterRequest(SAMPLE_USERNAME, SAMPLE_PASSWORD, SAMPLE_EMAIL));

        assertThrows(RuntimeException.class,
                () -> services.newGame(new NewGameRequest("", userRegistration.authToken())));
    }

    @Test
    public void testJoinGameAssignsPlayerToPosition() throws DataAccessException {
        // Setup: register and create a game
        RegisterResult userRegistration = services
                .register(new RegisterRequest(SAMPLE_USERNAME, SAMPLE_PASSWORD, SAMPLE_EMAIL));
        NewGameResult createdGameInfo = services
                .newGame(new NewGameRequest(SAMPLE_GAME_TITLE, userRegistration.authToken()));
        int matchIdentifier = Integer.parseInt(createdGameInfo.gameID());

        JoinResult joinResponse = services
                .joinGame(new JoinRequest(matchIdentifier, TeamColor.WHITE, userRegistration.authToken()));

        assertNotNull(joinResponse);
        assertEquals(TeamColor.WHITE, joinResponse.playerColor());
        assertEquals(SAMPLE_GAME_TITLE, joinResponse.gameName());

        // Verify game updated in database
        ListResult gameCollection = services.listAll(new ListRequest(userRegistration.authToken()));
        boolean playerPositionConfirmed = false;
        for (GameData gameRecord : gameCollection.games()) {
            if (gameRecord.gameID() == matchIdentifier && SAMPLE_USERNAME.equals(gameRecord.whiteUsername())) {
                playerPositionConfirmed = true;
                break;
            }
        }
        assertTrue(playerPositionConfirmed);
    }

    @Test
    public void testJoinGameRejectsOccupiedPosition() throws DataAccessException {
        // Setup: register, create a game, join as white
        RegisterResult firstPlayerRegistration = services
                .register(new RegisterRequest(SAMPLE_USERNAME, SAMPLE_PASSWORD, SAMPLE_EMAIL));
        NewGameResult createdGameInfo = services
                .newGame(new NewGameRequest(SAMPLE_GAME_TITLE, firstPlayerRegistration.authToken()));
        int matchIdentifier = Integer.parseInt(createdGameInfo.gameID());
        services.joinGame(new JoinRequest(matchIdentifier, TeamColor.WHITE, firstPlayerRegistration.authToken()));

        RegisterResult secondPlayerRegistration = services
                .register(new RegisterRequest("opponent", SAMPLE_PASSWORD, "opponent@example.com"));

        assertThrows(RuntimeException.class, () -> services
                .joinGame(new JoinRequest(matchIdentifier, TeamColor.WHITE, secondPlayerRegistration.authToken())));
    }
}