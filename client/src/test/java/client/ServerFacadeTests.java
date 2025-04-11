package client;

import org.junit.jupiter.api.*;
import server.Server;
import serverConnection.ServerFacade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

  private static Server server;
  static ServerFacade facade;
  private static int port;

  private static final String TEST_USER = "magnus carlesen";
  private static final String TEST_PASS = "password123";
  private static final String TEST_EMAIL = "magnus@example.com";
  private static final String TEST_GAME = "World Championship";

  @BeforeAll
  public static void init() {
    server = new Server();
    port = server.run(0);
    System.out.println("Started test HTTP server on " + port);
    facade = new ServerFacade("http://localhost:" + port);
  }

  @AfterAll
  static void stopServer() {
    server.stop();
  }

  @BeforeEach
  public void clearDatabase() {
    try {
      facade.clearDatabase();
    } catch (ServerFacade.ServerException e) {
      fail("Database clear failed before test: " + e.getMessage());
    }
  }

  /** Tests successful user registration. */
  @Test
  @DisplayName("Register User (+) ")
  void registerPositive() {
    try {
      HashMap<String, Object> authData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      assertNotNull(authData, "AuthData should not be null on successful registration.");
      assertTrue(authData.containsKey("username"), "Response should contain username.");
      assertEquals(TEST_USER, authData.get("username"), "Response username should match registered username.");
      assertTrue(authData.containsKey("authToken"), "Response should contain authToken.");
      assertNotNull(authData.get("authToken"), "AuthToken should not be null.");
      assertTrue(((String) authData.get("authToken")).length() > 10, "AuthToken seems too short.");
    } catch (ServerFacade.ServerException e) {
      fail("Positive register test failed with exception: " + e.getMessage());
    }
  }

  /** Tests registration failure when user already exists. */
  @Test
  @DisplayName("Register User Already Exists (-)")
  void registerNegativeExists() {
    try {
      facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      assertThrows(ServerFacade.ServerException.class, () -> {
        facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      }, "Registering an existing user should throw ServerException.");
      try {
        facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      } catch (ServerFacade.ServerException e) {
        assertTrue(e.getMessage().contains("403") || e.getMessage().toLowerCase().contains("already taken"),
            "Error message should indicate 'already taken' or HTTP 403.");
      }
    } catch (ServerFacade.ServerException e) {
      fail("Setup for negative register test failed: " + e.getMessage());
    }
  }

  /** Tests registration failure due to missing required fields (bad request). */
  @Test
  @DisplayName("Register Bad Request (-)")
  void registerNegativeBadRequest() {
    assertThrows(ServerFacade.ServerException.class, () -> {
      facade.register(TEST_USER, null, TEST_EMAIL);
    }, "Registering with null password should throw ServerException.");
    try {
      facade.register(TEST_USER, null, TEST_EMAIL);
    } catch (ServerFacade.ServerException e) {
      assertTrue(e.getMessage().contains("400") || e.getMessage().toLowerCase().contains("bad request"),
          "Error message should indicate 'bad request' or HTTP 400.");
    }
  }

  /** Tests successful login for an existing registered user. */
  @Test
  @DisplayName("Login User (+) ")
  void loginPositive() {
    try {
      facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      HashMap<String, Object> authData = facade.login(TEST_USER, TEST_PASS);
      assertNotNull(authData, "AuthData should not be null on successful login.");
      assertTrue(authData.containsKey("username"), "Response should contain username.");
      assertEquals(TEST_USER, authData.get("username"), "Response username should match logged in username.");
      assertTrue(authData.containsKey("authToken"), "Response should contain authToken.");
      assertNotNull(authData.get("authToken"), "AuthToken should not be null.");
      assertTrue(((String) authData.get("authToken")).length() > 10, "AuthToken seems too short.");
    } catch (ServerFacade.ServerException e) {
      fail("Positive login test failed with exception: " + e.getMessage());
    }
  }

  /** Tests login failure for a user that does not exist. */
  @Test
  @DisplayName("Login Non-Existent User (-)")
  void loginNegativeNoUser() {
    assertThrows(ServerFacade.ServerException.class, () -> {
      facade.login("non_existent_user", "any_password");
    }, "Login with non-existent user should throw ServerException.");
    try {
      facade.login("non_existent_user", "any_password");
    } catch (ServerFacade.ServerException e) {
      assertTrue(e.getMessage().contains("401") || e.getMessage().toLowerCase().contains("unauthorized"),
          "Error message should indicate 'unauthorized' or HTTP 401.");
    }
  }

  /** Tests login failure due to incorrect password for an existing user. */
  @Test
  @DisplayName("Login Wrong Password (-)")
  void loginNegativeWrongPassword() {
    try {
      facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      assertThrows(ServerFacade.ServerException.class, () -> {
        facade.login(TEST_USER, "wrong_password");
      }, "Login with wrong password should throw ServerException.");
      try {
        facade.login(TEST_USER, "wrong_password");
      } catch (ServerFacade.ServerException e) {
        assertTrue(e.getMessage().contains("401") || e.getMessage().toLowerCase().contains("unauthorized"),
            "Error message should indicate 'unauthorized' or HTTP 401.");
      }
    } catch (ServerFacade.ServerException e) {
      fail("Setup for negative login test failed: " + e.getMessage());
    }
  }

  /** Tests successful logout using a valid authToken. */
  @Test
  @DisplayName("Logout User (+) ")
  void logoutPositive() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");
      assertDoesNotThrow(() -> {
        facade.logout(authToken);
      }, "Valid logout should not throw exception.");
      assertThrows(ServerFacade.ServerException.class, () -> {
        facade.listGames(authToken);
      }, "Using logged-out token should fail.");
    } catch (ServerFacade.ServerException e) {
      fail("Positive logout test failed with exception: " + e.getMessage());
    }
  }

  /** Tests logout failure using an invalid or non-existent authToken. */
  @Test
  @DisplayName("Logout Invalid Token (-)")
  void logoutNegativeInvalidToken() {
    assertThrows(ServerFacade.ServerException.class, () -> {
      facade.logout("invalid_or_non_existent_token");
    }, "Logout with invalid token should throw ServerException.");
    try {
      facade.logout("invalid_or_non_existent_token");
    } catch (ServerFacade.ServerException e) {
      assertTrue(e.getMessage().contains("401") || e.getMessage().toLowerCase().contains("unauthorized"),
          "Error message should indicate 'unauthorized' or HTTP 401.");
    }
  }

  /** Tests successful game creation with a valid authToken. */
  @Test
  @DisplayName("Create Game (+) ")
  void createGamePositive() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");
      HashMap<String, Object> gameData = facade.createGame(authToken, TEST_GAME);
      assertNotNull(gameData, "Game data should not be null on success.");
      assertTrue(gameData.containsKey("gameID"), "Response should contain gameID.");
      assertNotNull(gameData.get("gameID"), "GameID should not be null.");
      Object gameIdObj = gameData.get("gameID");
      int gameId = -1;
      if (gameIdObj instanceof Number) {
        gameId = ((Number) gameIdObj).intValue();
      } else {
        try {
          gameId = Integer.parseInt(gameIdObj.toString());
        } catch (NumberFormatException nfe) {
          fail("GameID was not a recognizable number format: " + gameIdObj);
        }
      }
      assertTrue(gameId > 0, "GameID should be a positive integer.");
    } catch (ServerFacade.ServerException e) {
      fail("Positive create game test failed with exception: " + e.getMessage());
    }
  }

  /** Tests game creation failure using an invalid authToken. */
  @Test
  @DisplayName("Create Game Invalid Token (-)")
  void createGameNegativeInvalidToken() {
    assertThrows(ServerFacade.ServerException.class, () -> {
      facade.createGame("invalid_token", TEST_GAME);
    }, "Create game with invalid token should throw ServerException.");
    try {
      facade.createGame("invalid_token", TEST_GAME);
    } catch (ServerFacade.ServerException e) {
      assertTrue(e.getMessage().contains("401") || e.getMessage().toLowerCase().contains("unauthorized"),
          "Error message should indicate 'unauthorized' or HTTP 401.");
    }
  }

  /**
   * Tests game creation failure due to missing required fields (e.g., null name).
   */
  @Test
  @DisplayName("Create Game Bad Request (-)")
  void createGameNegativeBadRequest() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");
      assertThrows(ServerFacade.ServerException.class, () -> {
        facade.createGame(authToken, null);
      }, "Create game with null name should throw ServerException.");
      try {
        facade.createGame(authToken, null);
      } catch (ServerFacade.ServerException e) {
        assertTrue(e.getMessage().contains("400") || e.getMessage().toLowerCase().contains("bad request"),
            "Error message should indicate 'bad request' or HTTP 400.");
      }
    } catch (ServerFacade.ServerException e) {
      fail("Setup for negative create game test failed: " + e.getMessage());
    }
  }

  /** Tests successfully listing games when games exist. */
  @Test
  @DisplayName("List Games (+) ")
  @SuppressWarnings("unchecked")
  void listGamesPositive() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");
      HashMap<String, Object> createdGame = facade.createGame(authToken, TEST_GAME);
      Object createdGameIdObj = createdGame.get("gameID");

      HashMap<String, Object> listData = facade.listGames(authToken);
      assertNotNull(listData, "List data should not be null.");
      assertTrue(listData.containsKey("games"), "Response should contain 'games' list.");
      Object gamesObj = listData.get("games");
      assertTrue(gamesObj instanceof List, "'games' should be a List.");
      List<Map<String, Object>> gamesList = (List<Map<String, Object>>) gamesObj;
      assertFalse(gamesList.isEmpty(), "Games list should not be empty after creating a game.");
      assertEquals(1, gamesList.size(), "Should be exactly one game in the list.");

      Map<String, Object> listedGame = gamesList.get(0);
      assertEquals(TEST_GAME, listedGame.get("gameName"), "Listed game name doesn't match.");
      Object listedGameIdObj = listedGame.get("gameID");
      assertEquals(Double.parseDouble(createdGameIdObj.toString()), Double.parseDouble(listedGameIdObj.toString()),
          "Listed game ID doesn't match created game ID.");
      assertNull(listedGame.get("whiteUsername"), "White username should be null initially.");
      assertNull(listedGame.get("blackUsername"), "Black username should be null initially.");

    } catch (ServerFacade.ServerException e) {
      fail("Positive list games test failed with exception: " + e.getMessage());
    }
  }

  /** Tests successfully listing games when no games exist. */
  @Test
  @DisplayName("List Games Empty (+) ")
  @SuppressWarnings("unchecked")
  void listGamesPositiveEmpty() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");

      HashMap<String, Object> listData = facade.listGames(authToken);
      assertNotNull(listData, "List data should not be null.");
      assertTrue(listData.containsKey("games"), "Response should contain 'games' list.");
      Object gamesObj = listData.get("games");
      assertTrue(gamesObj instanceof List, "'games' should be a List.");
      List<Map<String, Object>> gamesList = (List<Map<String, Object>>) gamesObj;
      assertTrue(gamesList.isEmpty(), "Games list should be empty when no games created.");

    } catch (ServerFacade.ServerException e) {
      fail("Positive empty list games test failed with exception: " + e.getMessage());
    }
  }

  /** Tests listing games failure using an invalid authToken. */
  @Test
  @DisplayName("List Games Invalid Token (-)")
  void listGamesNegativeInvalidToken() {
    assertThrows(ServerFacade.ServerException.class, () -> {
      facade.listGames("invalid_token");
    }, "List games with invalid token should throw ServerException.");
    try {
      facade.listGames("invalid_token");
    } catch (ServerFacade.ServerException e) {
      assertTrue(e.getMessage().contains("401") || e.getMessage().toLowerCase().contains("unauthorized"),
          "Error message should indicate 'unauthorized' or HTTP 401.");
    }
  }

  /** Tests successfully joining a game as a player. */
  @Test
  @DisplayName("Join Game Player (+) ")
  @SuppressWarnings("unchecked")
  void joinGamePositivePlayer() {
    try {
      HashMap<String, Object> user1Reg = facade.register("user1", "pass1", "e1@mail.com");
      String user1Auth = (String) user1Reg.get("authToken");
      HashMap<String, Object> user2Reg = facade.register("user2", "pass2", "e2@mail.com");
      String user2Auth = (String) user2Reg.get("authToken");

      HashMap<String, Object> createdGame = facade.createGame(user1Auth, TEST_GAME);
      int gameID = Double.valueOf(createdGame.get("gameID").toString()).intValue();

      assertDoesNotThrow(() -> {
        facade.joinGame(user2Auth, gameID, "BLACK");
      }, "Valid join should not throw exception.");

      HashMap<String, Object> listData = facade.listGames(user1Auth);
      List<Map<String, Object>> gamesList = (List<Map<String, Object>>) listData.get("games");
      assertEquals(1, gamesList.size());
      Map<String, Object> listedGame = gamesList.get(0);
      assertEquals("user2", listedGame.get("blackUsername"), "Black username should be user2.");
      assertNull(listedGame.get("whiteUsername"), "White username should still be null.");

    } catch (ServerFacade.ServerException e) {
      fail("Positive join game test failed with exception: " + e.getMessage());
    }
  }

  /** Tests successfully joining a game as an observer. */
  @Test
  @DisplayName("Observe Game (+) ")
  @SuppressWarnings("unchecked")
  void joinGamePositiveObserver() {
    try {
      HashMap<String, Object> user1Reg = facade.register("observerUser", "pass1", "e1@mail.com");
      String observerAuth = (String) user1Reg.get("authToken");
      HashMap<String, Object> playerReg = facade.register("playerUser", "pass2", "e2@mail.com");
      String playerAuth = (String) playerReg.get("authToken");

      HashMap<String, Object> createdGame = facade.createGame(playerAuth, TEST_GAME);
      int gameID = Double.valueOf(createdGame.get("gameID").toString()).intValue();
      facade.joinGame(playerAuth, gameID, "WHITE");

      assertDoesNotThrow(() -> {
        facade.observeGame(observerAuth, gameID);
      }, "Valid observe should not throw exception.");

      HashMap<String, Object> listData = facade.listGames(playerAuth);
      List<Map<String, Object>> gamesList = (List<Map<String, Object>>) listData.get("games");
      assertEquals(1, gamesList.size());
      Map<String, Object> listedGame = gamesList.get(0);
      assertEquals("playerUser", listedGame.get("whiteUsername"), "White username should be playerUser.");
      assertNull(listedGame.get("blackUsername"), "Black username should still be null.");

    } catch (ServerFacade.ServerException e) {
      fail("Positive observe game test failed with exception: " + e.getMessage());
    }
  }

  /** Tests joining game failure when the requested color is already taken. */
  @Test
  @DisplayName("Join Game Color Taken (-)")
  void joinGameNegativeColorTaken() {
    try {
      HashMap<String, Object> user1Reg = facade.register("user1", "pass1", "e1@mail.com");
      String user1Auth = (String) user1Reg.get("authToken");
      HashMap<String, Object> user2Reg = facade.register("user2", "pass2", "e2@mail.com");
      String user2Auth = (String) user2Reg.get("authToken");

      HashMap<String, Object> createdGame = facade.createGame(user1Auth, TEST_GAME);
      int gameID = Double.valueOf(createdGame.get("gameID").toString()).intValue();
      facade.joinGame(user1Auth, gameID, "WHITE");

      assertThrows(ServerFacade.ServerException.class, () -> {
        facade.joinGame(user2Auth, gameID, "WHITE");
      }, "Joining a taken color should throw ServerException.");
      try {
        facade.joinGame(user2Auth, gameID, "WHITE");
      } catch (ServerFacade.ServerException e) {
        assertTrue(e.getMessage().contains("403") || e.getMessage().toLowerCase().contains("already taken"),
            "Error message should indicate 'already taken' or HTTP 403.");
      }

    } catch (ServerFacade.ServerException e) {
      fail("Setup for negative join game test failed: " + e.getMessage());
    }
  }

  /** Tests joining game failure with a non-existent game ID. */
  @Test
  @DisplayName("Join Game Bad Game ID (-)")
  void joinGameNegativeBadID() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");
      assertThrows(ServerFacade.ServerException.class, () -> {
        facade.joinGame(authToken, 99999, "WHITE");
      }, "Joining non-existent game should throw ServerException.");
      try {
        facade.joinGame(authToken, 99999, "WHITE");
      } catch (ServerFacade.ServerException e) {
        assertTrue(e.getMessage().contains("400") || e.getMessage().toLowerCase().contains("bad request"),
            "Error message should indicate 'bad request' or HTTP 400.");
      }
    } catch (ServerFacade.ServerException e) {
      fail("Setup for negative join game (bad ID) test failed: " + e.getMessage());
    }
  }

  /** Tests observing game failure with a non-existent game ID. */
  @Test
  @DisplayName("Observe Game Bad Game ID (-)")
  void observeGameNegativeBadID() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");
      assertThrows(ServerFacade.ServerException.class, () -> {
        facade.observeGame(authToken, 99999);
      }, "Observing non-existent game should throw ServerException.");
      try {
        facade.observeGame(authToken, 99999);
      } catch (ServerFacade.ServerException e) {
        assertTrue(e.getMessage().contains("400") || e.getMessage().toLowerCase().contains("bad request"),
            "Error message should indicate 'bad request' or HTTP 400.");
      }
    } catch (ServerFacade.ServerException e) {
      fail("Setup for negative observe game (bad ID) test failed: " + e.getMessage());
    }
  }

  /** Tests joining game failure with an invalid authToken. */
  @Test
  @DisplayName("Join Game Invalid Auth Token (-)")
  void joinGameNegativeInvalidAuth() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");
      HashMap<String, Object> createdGame = facade.createGame(authToken, TEST_GAME);
      int gameID = Double.valueOf(createdGame.get("gameID").toString()).intValue();

      assertThrows(ServerFacade.ServerException.class, () -> {
        facade.joinGame("invalid-token", gameID, "WHITE");
      }, "Joining game with invalid token should throw ServerException.");
      try {
        facade.joinGame("invalid-token", gameID, "WHITE");
      } catch (ServerFacade.ServerException e) {
        assertTrue(e.getMessage().contains("401") || e.getMessage().toLowerCase().contains("unauthorized"),
            "Error message should indicate 'unauthorized' or HTTP 401.");
      }
    } catch (ServerFacade.ServerException e) {
      fail("Setup for negative join game (invalid auth) test failed: " + e.getMessage());
    }
  }

  /** Tests observing game failure with an invalid authToken. */
  @Test
  @DisplayName("Observe Game Invalid Auth Token (-)")
  void observeGameNegativeInvalidAuth() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");
      HashMap<String, Object> createdGame = facade.createGame(authToken, TEST_GAME);
      int gameID = Double.valueOf(createdGame.get("gameID").toString()).intValue();

      assertThrows(ServerFacade.ServerException.class, () -> {
        facade.observeGame("invalid-token", gameID);
      }, "Observing game with invalid token should throw ServerException.");
      try {
        facade.observeGame("invalid-token", gameID);
      } catch (ServerFacade.ServerException e) {
        assertTrue(e.getMessage().contains("401") || e.getMessage().toLowerCase().contains("unauthorized"),
            "Error message should indicate 'unauthorized' or HTTP 401.");
      }
    } catch (ServerFacade.ServerException e) {
      fail("Setup for negative observe game (invalid auth) test failed: " + e.getMessage());
    }
  }

  /** Tests that clearing the database works and removes existing data. */
  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Clear Database Functional Test")
  void clearDatabaseFunctional() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");
      facade.createGame(authToken, TEST_GAME);

      assertDoesNotThrow(() -> {
        facade.clearDatabase();
      }, "Clear database should not throw an exception.");

      assertThrows(ServerFacade.ServerException.class, () -> {
        facade.login(TEST_USER, TEST_PASS);
      }, "Login should fail after clear.");

      HashMap<String, Object> newUserReg = facade.register("newUserAfterClear", "pass", "e@mail.com");
      String newUserAuth = (String) newUserReg.get("authToken");
      HashMap<String, Object> listData = facade.listGames(newUserAuth);
      List<Map<String, Object>> gamesList = (List<Map<String, Object>>) listData.get("games");
      assertTrue(gamesList.isEmpty(), "Games list should be empty after clear.");

    } catch (ServerFacade.ServerException e) {
      fail("Clear database functional test failed with exception: " + e.getMessage());
    }
  }
}