package service;

import org.junit.jupiter.api.*;
import connection.ServerFacade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

  static ServerFacade facade;
  private static int port;

  private static final String TEST_USER = "magnus carlesen";
  private static final String TEST_PASS = "password123";
  private static final String TEST_EMAIL = "magnus@example.com";
  private static final String TEST_GAME = "World Championship";

  @BeforeAll
  public static void init() {
    port = 8080;
    facade = new ServerFacade("http://localhost:" + port);
  }

  @BeforeEach
  public void clearDatabase() {
    try {
      facade.clearDatabase();
    } catch (ServerFacade.ServerFacadeException e) {
      fail("Database clear failed before test: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Register User (+)")
  void registerPositive() {
    try {
      HashMap<String, Object> authData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      assertNotNull(authData);
      assertTrue(authData.containsKey("username"));
      assertEquals(TEST_USER, authData.get("username"));
      assertTrue(authData.containsKey("authToken"));
      assertNotNull(authData.get("authToken"));
    } catch (ServerFacade.ServerFacadeException e) {
      fail("Positive register test failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Register User Already Exists (-)")
  void registerNegativeExists() {
    try {
      facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      assertThrows(ServerFacade.ServerFacadeException.class, () -> {
        facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      });
    } catch (ServerFacade.ServerFacadeException e) {
      fail("Setup for negative register test failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Register Bad Request (-)")
  void registerNegativeBadRequest() {
    assertThrows(ServerFacade.ServerFacadeException.class, () -> {
      facade.register(TEST_USER, null, TEST_EMAIL);
    });
  }

  @Test
  @DisplayName("Login User (+)")
  void loginPositive() {
    try {
      facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      HashMap<String, Object> authData = facade.login(TEST_USER, TEST_PASS);
      assertNotNull(authData);
      assertTrue(authData.containsKey("username"));
      assertEquals(TEST_USER, authData.get("username"));
      assertTrue(authData.containsKey("authToken"));
      assertNotNull(authData.get("authToken"));
    } catch (ServerFacade.ServerFacadeException e) {
      fail("Positive login test failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Login Non-Existent User (-)")
  void loginNegativeNoUser() {
    assertThrows(ServerFacade.ServerFacadeException.class, () -> {
      facade.login("non_existent_user", "any_password");
    });
  }

  @Test
  @DisplayName("Login Wrong Password (-)")
  void loginNegativeWrongPassword() {
    try {
      facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      assertThrows(ServerFacade.ServerFacadeException.class, () -> {
        facade.login(TEST_USER, "wrong_password");
      });
    } catch (ServerFacade.ServerFacadeException e) {
      fail("Setup for negative login test failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Logout User (+)")
  void logoutPositive() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");
      assertDoesNotThrow(() -> {
        facade.logout(authToken);
      });
      assertThrows(ServerFacade.ServerFacadeException.class, () -> {
        facade.listGames(authToken);
      });
    } catch (ServerFacade.ServerFacadeException e) {
      fail("Positive logout test failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Logout Invalid Token (-)")
  void logoutNegativeInvalidToken() {
    assertThrows(ServerFacade.ServerFacadeException.class, () -> {
      facade.logout("invalid_or_non_existent_token");
    });
  }

  @Test
  @DisplayName("Create Game (+)")
  void createGamePositive() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");
      HashMap<String, Object> gameData = facade.createGame(authToken, TEST_GAME);
      assertNotNull(gameData);
      assertTrue(gameData.containsKey("gameID"));
      assertNotNull(gameData.get("gameID"));
    } catch (ServerFacade.ServerFacadeException e) {
      fail("Positive create game test failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Create Game Invalid Token (-)")
  void createGameNegativeInvalidToken() {
    assertThrows(ServerFacade.ServerFacadeException.class, () -> {
      facade.createGame("invalid_token", TEST_GAME);
    });
  }

  @Test
  @DisplayName("Create Game Bad Request (-)")
  void createGameNegativeBadRequest() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");
      assertThrows(ServerFacade.ServerFacadeException.class, () -> {
        facade.createGame(authToken, null);
      });
    } catch (ServerFacade.ServerFacadeException e) {
      fail("Setup for negative create game test failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("List Games (+)")
  @SuppressWarnings("unchecked")
  void listGamesPositive() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");
      HashMap<String, Object> createdGame = facade.createGame(authToken, TEST_GAME);
      Object createdGameIdObj = createdGame.get("gameID");

      HashMap<String, Object> listData = facade.listGames(authToken);
      assertNotNull(listData);
      assertTrue(listData.containsKey("games"));
      Object gamesObj = listData.get("games");
      assertTrue(gamesObj instanceof List);
      List<Map<String, Object>> gamesList = (List<Map<String, Object>>) gamesObj;
      assertFalse(gamesList.isEmpty());
      assertEquals(1, gamesList.size());

      Map<String, Object> listedGame = gamesList.get(0);
      assertEquals(TEST_GAME, listedGame.get("gameName"));
      Object listedGameIdObj = listedGame.get("gameID");
      assertEquals(Double.parseDouble(createdGameIdObj.toString()), Double.parseDouble(listedGameIdObj.toString()));
      assertNull(listedGame.get("whiteUsername"));
      assertNull(listedGame.get("blackUsername"));

    } catch (ServerFacade.ServerFacadeException e) {
      fail("Positive list games test failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("List Games Empty (+)")
  @SuppressWarnings("unchecked")
  void listGamesPositiveEmpty() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");

      HashMap<String, Object> listData = facade.listGames(authToken);
      assertNotNull(listData);
      assertTrue(listData.containsKey("games"));
      Object gamesObj = listData.get("games");
      assertTrue(gamesObj instanceof List);
      List<Map<String, Object>> gamesList = (List<Map<String, Object>>) gamesObj;
      assertTrue(gamesList.isEmpty());

    } catch (ServerFacade.ServerFacadeException e) {
      fail("Positive empty list games test failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("List Games Invalid Token (-)")
  void listGamesNegativeInvalidToken() {
    assertThrows(ServerFacade.ServerFacadeException.class, () -> {
      facade.listGames("invalid_token");
    });
  }

  @Test
  @DisplayName("Join Game Player (+)")
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
        facade.joinGame(user2Auth, gameID, "BLACK", "user2");
      });

      HashMap<String, Object> listData = facade.listGames(user1Auth);
      List<Map<String, Object>> gamesList = (List<Map<String, Object>>) listData.get("games");
      assertEquals(1, gamesList.size());
      Map<String, Object> listedGame = gamesList.get(0);
      assertEquals("user2", listedGame.get("blackUsername"));
      assertNull(listedGame.get("whiteUsername"));

    } catch (ServerFacade.ServerFacadeException e) {
      fail("Positive join game test failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Observe Game (+)")
  @SuppressWarnings("unchecked")
  void joinGamePositiveObserver() {
    try {
      HashMap<String, Object> user1Reg = facade.register("observerUser", "pass1", "e1@mail.com");
      String observerAuth = (String) user1Reg.get("authToken");
      HashMap<String, Object> playerReg = facade.register("playerUser", "pass2", "e2@mail.com");
      String playerAuth = (String) playerReg.get("authToken");

      HashMap<String, Object> createdGame = facade.createGame(playerAuth, TEST_GAME);
      int gameID = Double.valueOf(createdGame.get("gameID").toString()).intValue();
      facade.joinGame(playerAuth, gameID, "WHITE", "playerUser");

      assertDoesNotThrow(() -> {
        facade.observeGame(observerAuth, gameID, "observerUser");
      });

      HashMap<String, Object> listData = facade.listGames(playerAuth);
      List<Map<String, Object>> gamesList = (List<Map<String, Object>>) listData.get("games");
      assertEquals(1, gamesList.size());
      Map<String, Object> listedGame = gamesList.get(0);
      assertEquals("playerUser", listedGame.get("whiteUsername"));
      assertNull(listedGame.get("blackUsername"));

    } catch (ServerFacade.ServerFacadeException e) {
      fail("Positive observe game test failed: " + e.getMessage());
    }
  }

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
      facade.joinGame(user1Auth, gameID, "WHITE", "user1");

      assertThrows(ServerFacade.ServerFacadeException.class, () -> {
        facade.joinGame(user2Auth, gameID, "WHITE", "user2");
      });

    } catch (ServerFacade.ServerFacadeException e) {
      fail("Setup for negative join game test failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Join Game Bad Game ID (-)")
  void joinGameNegativeBadID() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");
      assertThrows(ServerFacade.ServerFacadeException.class, () -> {
        facade.joinGame(authToken, 99999, "WHITE", TEST_USER);
      });
    } catch (ServerFacade.ServerFacadeException e) {
      fail("Setup for negative join game (bad ID) test failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Observe Game Bad Game ID (-)")
  void observeGameNegativeBadID() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");
      assertThrows(ServerFacade.ServerFacadeException.class, () -> {
        facade.observeGame(authToken, 99999, TEST_USER);
      });
    } catch (ServerFacade.ServerFacadeException e) {
      fail("Setup for negative observe game (bad ID) test failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Join Game Invalid Auth Token (-)")
  void joinGameNegativeInvalidAuth() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");
      HashMap<String, Object> createdGame = facade.createGame(authToken, TEST_GAME);
      int gameID = Double.valueOf(createdGame.get("gameID").toString()).intValue();

      assertThrows(ServerFacade.ServerFacadeException.class, () -> {
        facade.joinGame("invalid-token", gameID, "WHITE", "dummy");
      });
    } catch (ServerFacade.ServerFacadeException e) {
      fail("Setup for negative join game (invalid auth) test failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Observe Game Invalid Auth Token (-)")
  void observeGameNegativeInvalidAuth() {
    try {
      HashMap<String, Object> regData = facade.register(TEST_USER, TEST_PASS, TEST_EMAIL);
      String authToken = (String) regData.get("authToken");
      HashMap<String, Object> createdGame = facade.createGame(authToken, TEST_GAME);
      int gameID = Double.valueOf(createdGame.get("gameID").toString()).intValue();

      assertThrows(ServerFacade.ServerFacadeException.class, () -> {
        facade.observeGame("invalid-token", gameID, "dummy");
      });
    } catch (ServerFacade.ServerFacadeException e) {
      fail("Setup for negative observe game (invalid auth) test failed: " + e.getMessage());
    }
  }

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
      });

      assertThrows(ServerFacade.ServerFacadeException.class, () -> {
        facade.login(TEST_USER, TEST_PASS);
      });

      HashMap<String, Object> newUserReg = facade.register("newUserAfterClear", "pass", "e@mail.com");
      String newUserAuth = (String) newUserReg.get("authToken");
      HashMap<String, Object> listData = facade.listGames(newUserAuth);
      List<Map<String, Object>> gamesList = (List<Map<String, Object>>) listData.get("games");
      assertTrue(gamesList.isEmpty());

    } catch (ServerFacade.ServerFacadeException e) {
      fail("Clear database functional test failed: " + e.getMessage());
    }
  }
}