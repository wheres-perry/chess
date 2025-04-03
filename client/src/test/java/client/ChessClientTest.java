package client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChessClientTest {

  private ChessClient client;
  private TestServerFacade testServer;

  private static final String TEST_SERVER_URL = "http://test.server";
  private static final String TEST_USERNAME = "testUser";
  private static final String TEST_PASSWORD = "testPassword";
  private static final String TEST_EMAIL = "test@example.com";
  private static final String TEST_AUTH_TOKEN = "test-auth-token";
  private static final String TEST_GAME_NAME = "Test Game";
  private static final int TEST_GAME_ID = 123;

  @BeforeEach
  public void setUp() throws Exception {
    // Create test server facade
    testServer = new TestServerFacade();

    // Create client with constructor that allows injecting the test server
    client = new ChessClient(TEST_SERVER_URL) {
      @Override
      protected ServerFacade createServerFacade(String serverUrl) {
        return testServer;
      }
    };
  }

  @Test
  public void testRegisterSuccess() throws Exception {
    // Setup test response
    HashMap<String, Object> response = new HashMap<>();
    response.put("username", TEST_USERNAME);
    response.put("authToken", TEST_AUTH_TOKEN);
    testServer.setRegisterResponse(response);

    // Call the method
    client.register(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL);

    // Verify interactions and state
    assertEquals(TEST_USERNAME, testServer.getLastRegisterUsername());
    assertEquals(TEST_PASSWORD, testServer.getLastRegisterPassword());
    assertEquals(TEST_EMAIL, testServer.getLastRegisterEmail());
    assertTrue(client.isLoggedIn());
    assertEquals(TEST_USERNAME, client.getCurrentUser());
  }

  @Test
  public void testLoginSuccess() throws Exception {
    // Setup test response
    HashMap<String, Object> response = new HashMap<>();
    response.put("username", TEST_USERNAME);
    response.put("authToken", TEST_AUTH_TOKEN);
    testServer.setLoginResponse(response);

    // Call the method
    client.login(TEST_USERNAME, TEST_PASSWORD);

    // Verify interactions and state
    assertEquals(TEST_USERNAME, testServer.getLastLoginUsername());
    assertEquals(TEST_PASSWORD, testServer.getLastLoginPassword());
    assertTrue(client.isLoggedIn());
    assertEquals(TEST_USERNAME, client.getCurrentUser());
  }

  @Test
  public void testLogoutSuccess() throws Exception {
    // Setup initial state - logged in
    HashMap<String, Object> loginResponse = new HashMap<>();
    loginResponse.put("username", TEST_USERNAME);
    loginResponse.put("authToken", TEST_AUTH_TOKEN);
    testServer.setLoginResponse(loginResponse);
    client.login(TEST_USERNAME, TEST_PASSWORD);

    // Now test logout
    client.logout();

    // Verify server was called with the auth token
    assertEquals(TEST_AUTH_TOKEN, testServer.getLastLogoutAuthToken());

    // Verify client state after logout
    assertFalse(client.isLoggedIn());
    assertNotEquals(TEST_USERNAME, client.getCurrentUser());
    assertFalse(client.isClientInGame());
  }

  @Test
  public void testCreateGameSuccess() throws Exception {
    // Setup initial state - logged in
    HashMap<String, Object> loginResponse = new HashMap<>();
    loginResponse.put("username", TEST_USERNAME);
    loginResponse.put("authToken", TEST_AUTH_TOKEN);
    testServer.setLoginResponse(loginResponse);
    client.login(TEST_USERNAME, TEST_PASSWORD);

    // Setup response for create game
    HashMap<String, Object> createResponse = new HashMap<>();
    createResponse.put("gameID", String.valueOf(TEST_GAME_ID));
    testServer.setCreateGameResponse(createResponse);

    // Call method
    client.createGame(TEST_GAME_NAME);

    // Verify server was called correctly
    assertEquals(TEST_AUTH_TOKEN, testServer.getLastCreateGameAuthToken());
    assertEquals(TEST_GAME_NAME, testServer.getLastCreateGameName());
  }

  @Test
  public void testListGamesSuccess() throws Exception {
    // Setup initial state - logged in
    HashMap<String, Object> loginResponse = new HashMap<>();
    loginResponse.put("username", TEST_USERNAME);
    loginResponse.put("authToken", TEST_AUTH_TOKEN);
    testServer.setLoginResponse(loginResponse);
    client.login(TEST_USERNAME, TEST_PASSWORD);

    // Setup response for list games
    HashMap<String, Object> listResponse = new HashMap<>();
    List<Map<String, Object>> gamesList = new ArrayList<>();

    Map<String, Object> game1 = new HashMap<>();
    game1.put("gameID", TEST_GAME_ID);
    game1.put("gameName", TEST_GAME_NAME);
    game1.put("whiteUsername", "player1");
    game1.put("blackUsername", "player2");
    gamesList.add(game1);

    listResponse.put("games", gamesList);
    testServer.setListGamesResponse(listResponse);

    // Call method
    List<HashMap<String, Object>> result = client.listGames();

    // Verify server was called correctly
    assertEquals(TEST_AUTH_TOKEN, testServer.getLastListGamesAuthToken());

    // Verify result
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(TEST_GAME_ID, result.get(0).get("gameID"));
    assertEquals(TEST_GAME_NAME, result.get(0).get("gameName"));
  }

  @Test
  public void testJoinGameSuccess() throws Exception {
    // Setup initial state - logged in
    HashMap<String, Object> loginResponse = new HashMap<>();
    loginResponse.put("username", TEST_USERNAME);
    loginResponse.put("authToken", TEST_AUTH_TOKEN);
    testServer.setLoginResponse(loginResponse);
    client.login(TEST_USERNAME, TEST_PASSWORD);

    // Setup response for join game
    HashMap<String, Object> joinResponse = new HashMap<>();
    joinResponse.put("status", "success");
    testServer.setJoinGameResponse(joinResponse);

    // Call method
    client.joinGame(TEST_GAME_ID, "WHITE");

    // Verify server was called correctly
    assertEquals(TEST_AUTH_TOKEN, testServer.getLastJoinGameAuthToken());
    assertEquals(TEST_GAME_ID, testServer.getLastJoinGameID());
    assertEquals("WHITE", testServer.getLastJoinGameColor());

    // Verify client state
    assertTrue(client.isClientInGame());
  }

  @Test
  public void testObserveGameSuccess() throws Exception {
    // Setup initial state - logged in
    HashMap<String, Object> loginResponse = new HashMap<>();
    loginResponse.put("username", TEST_USERNAME);
    loginResponse.put("authToken", TEST_AUTH_TOKEN);
    testServer.setLoginResponse(loginResponse);
    client.login(TEST_USERNAME, TEST_PASSWORD);

    // Call method
    client.observeGame(TEST_GAME_ID);

    // Verify server was called correctly
    assertEquals(TEST_AUTH_TOKEN, testServer.getLastJoinGameAuthToken());
    assertEquals(TEST_GAME_ID, testServer.getLastJoinGameID());
    assertNull(testServer.getLastJoinGameColor());

    // Verify client state
    assertTrue(client.isClientInGame());
  }

  @Test
  public void testNotLoggedInExceptions() {
    // Test all methods that require login throw exceptions when client not logged
    // in
    Exception exception;

    // createGame
    exception = assertThrows(Exception.class, () -> client.createGame(TEST_GAME_NAME));
    assertTrue(exception.getMessage().contains("logged in"));

    // listGames
    exception = assertThrows(Exception.class, () -> client.listGames());
    assertTrue(exception.getMessage().contains("logged in"));

    // joinGame
    exception = assertThrows(Exception.class, () -> client.joinGame(TEST_GAME_ID, "WHITE"));
    assertTrue(exception.getMessage().contains("logged in"));

    // observeGame
    exception = assertThrows(Exception.class, () -> client.observeGame(TEST_GAME_ID));
    assertTrue(exception.getMessage().contains("logged in"));
  }

  /**
   * Test double for ServerFacade that records method calls and returns predefined
   * responses
   */
  private static class TestServerFacade extends ServerFacade {
    // Stored responses
    private HashMap<String, Object> registerResponse;
    private HashMap<String, Object> loginResponse;
    private HashMap<String, Object> createGameResponse;
    private HashMap<String, Object> listGamesResponse;
    private HashMap<String, Object> joinGameResponse; // Add this field

    // Last method call arguments
    private String lastRegisterUsername;
    private String lastRegisterPassword;
    private String lastRegisterEmail;
    private String lastLoginUsername;
    private String lastLoginPassword;
    private String lastLogoutAuthToken;
    private String lastCreateGameAuthToken;
    private String lastCreateGameName;
    private String lastListGamesAuthToken;
    private String lastJoinGameAuthToken;
    private int lastJoinGameID;
    private String lastJoinGameColor;

    public TestServerFacade() {
      super("http://dummy.url");
      // Initialize response to avoid NPE
      this.joinGameResponse = new HashMap<>();
    }

    // Override server facade methods to return test responses and record arguments

    @Override
    public HashMap<String, Object> register(String username, String password, String email) {
      this.lastRegisterUsername = username;
      this.lastRegisterPassword = password;
      this.lastRegisterEmail = email;
      return registerResponse;
    }

    @Override
    public HashMap<String, Object> login(String username, String password) {
      this.lastLoginUsername = username;
      this.lastLoginPassword = password;
      return loginResponse;
    }

    @Override
    public void logout(String authToken) {
      this.lastLogoutAuthToken = authToken;
    }

    @Override
    public HashMap<String, Object> createGame(String authToken, String gameName) {
      this.lastCreateGameAuthToken = authToken;
      this.lastCreateGameName = gameName;
      return createGameResponse;
    }

    @Override
    public HashMap<String, Object> listGames(String authToken) {
      this.lastListGamesAuthToken = authToken;
      return listGamesResponse;
    }

    @Override
    public HashMap<String, Object> joinGame(String authToken, int gameID, String playerColor) {
      this.lastJoinGameAuthToken = authToken;
      this.lastJoinGameID = gameID;
      this.lastJoinGameColor = playerColor;
      return joinGameResponse;
    }

    // Setters for test responses
    public void setRegisterResponse(HashMap<String, Object> response) {
      this.registerResponse = response;
    }

    public void setLoginResponse(HashMap<String, Object> response) {
      this.loginResponse = response;
    }

    public void setCreateGameResponse(HashMap<String, Object> response) {
      this.createGameResponse = response;
    }

    public void setListGamesResponse(HashMap<String, Object> response) {
      this.listGamesResponse = response;
    }

    public void setJoinGameResponse(HashMap<String, Object> response) {
      this.joinGameResponse = response;
    }

    // Getters for last method call arguments
    public String getLastRegisterUsername() {
      return lastRegisterUsername;
    }

    public String getLastRegisterPassword() {
      return lastRegisterPassword;
    }

    public String getLastRegisterEmail() {
      return lastRegisterEmail;
    }

    public String getLastLoginUsername() {
      return lastLoginUsername;
    }

    public String getLastLoginPassword() {
      return lastLoginPassword;
    }

    public String getLastLogoutAuthToken() {
      return lastLogoutAuthToken;
    }

    public String getLastCreateGameAuthToken() {
      return lastCreateGameAuthToken;
    }

    public String getLastCreateGameName() {
      return lastCreateGameName;
    }

    public String getLastListGamesAuthToken() {
      return lastListGamesAuthToken;
    }

    public String getLastJoinGameAuthToken() {
      return lastJoinGameAuthToken;
    }

    public int getLastJoinGameID() {
      return lastJoinGameID;
    }

    public String getLastJoinGameColor() {
      return lastJoinGameColor;
    }
  }
}