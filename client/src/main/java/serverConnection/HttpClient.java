package serverConnection;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ui.EscapeSequences;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * Handles sending HTTP requests and processing responses for the Chess client.
 * Includes a dummy implementation for testing without a live server.
 */
public class HttpClient {

  private final String serverHttpUrl;
  private static final Gson gson = new Gson();
  private static final Type mapType = new TypeToken<HashMap<String, Object>>() {
  }.getType();

  private boolean simulateHttpServer = true;
  private final Map<String, String> dummyUsers = new HashMap<>();
  private final Map<String, String> dummyAuthTokens = new HashMap<>();
  private final Map<Integer, Map<String, Object>> dummyGames = new HashMap<>();
  private int nextGameId = 1;

  /**
   * Custom exception class for HTTP communication errors.
   */
  public static class HttpException extends Exception {
    public HttpException(String message) {
      super(message);
    }

    public HttpException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public HttpClient(String serverUrl) {
    this.serverHttpUrl = serverUrl;
  }

  /**
   * Sends an HTTP request to the server or simulates the response if configured.
   *
   * @param method    HTTP method (e.g., "GET", "POST", "DELETE", "PUT").
   * @param path      Server endpoint path (e.g., "/user", "/game").
   * @param authToken Authorization token (can be null).
   * @param request   Request body object (can be null for GET/DELETE).
   * @return A HashMap representing the JSON response body on success.
   * @throws HttpException if the request fails or the server returns an error (or
   *                       simulation dictates).
   */
  public HashMap<String, Object> sendRequest(String method, String path, String authToken, Object request)
      throws HttpException {

    if (simulateHttpServer) {
      System.out.println("[HttpClient-Dummy] Simulating " + method + " " + path);
      return simulateHttpResponse(method, path, authToken, request);
    } else {
      HttpURLConnection http = null;
      try {
        URL url = new URI(serverHttpUrl + path).toURL();
        http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod(method);
        http.setReadTimeout(5000);

        if (authToken != null && !authToken.isEmpty()) {
          http.setRequestProperty("Authorization", authToken);
        }

        if (request != null && !method.equals("GET") && !method.equals("DELETE")) {
          http.setDoOutput(true);
          http.addRequestProperty("Content-Type", "application/json");
          String reqBody = gson.toJson(request);
          try (OutputStream reqStream = http.getOutputStream()) {
            reqStream.write(reqBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
          }
        }

        http.connect();
        return handleHttpResponse(http);

      } catch (Exception e) {
        throw new HttpException("Failed to send HTTP request: " + e.getMessage(), e);
      } finally {
        if (http != null) {
          http.disconnect();
        }
      }
    }
  }

  /**
   * Simulates the server's HTTP response based on the request details.
   * Uses in-memory maps to mimic database state.
   *
   * @param method    HTTP method.
   * @param path      Endpoint path.
   * @param authToken Provided auth token.
   * @param request   Request body (usually a Map for dummy).
   * @return Simulated response HashMap.
   * @throws HttpException For simulated errors (4xx, 5xx).
   */
  private HashMap<String, Object> simulateHttpResponse(String method, String path, String authToken, Object request)
      throws HttpException {
    @SuppressWarnings("unchecked")
    Map<String, String> requestBody = (request instanceof Map) ? (Map<String, String>) request : new HashMap<>();

    switch (path) {
      case "/db":
        if ("DELETE".equals(method)) {
          dummyUsers.clear();
          dummyAuthTokens.clear();
          dummyGames.clear();
          nextGameId = 1;
          System.out.println("[HttpClient-Dummy] Cleared dummy data.");
          return new HashMap<>(); // Success, empty body
        }
        break;

      case "/user":
        if ("POST".equals(method)) { // Register
          String username = requestBody.get("username");
          String password = requestBody.get("password");
          String email = requestBody.get("email");
          if (username == null || password == null || email == null || username.isEmpty() || password.isEmpty()
              || email.isEmpty()) {
            throw new HttpException("Server returned error: 400 - Error: bad request");
          }
          if (dummyUsers.containsKey(username)) {
            throw new HttpException("Server returned error: 403 - Error: already taken");
          }
          dummyUsers.put(username, password);
          String newAuthToken = UUID.randomUUID().toString();
          dummyAuthTokens.put(newAuthToken, username);
          System.out.println("[HttpClient-Dummy] Registered user: " + username);
          HashMap<String, Object> response = new HashMap<>();
          response.put("username", username);
          response.put("authToken", newAuthToken);
          return response;
        }
        break;

      case "/session":
        if ("POST".equals(method)) { // Login
          String username = requestBody.get("username");
          String password = requestBody.get("password");
          if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            throw new HttpException("Server returned error: 400 - Error: bad request");
          }
          if (!dummyUsers.containsKey(username) || !dummyUsers.get(username).equals(password)) {
            throw new HttpException("Server returned error: 401 - Error: unauthorized");
          }
          String newAuthToken = UUID.randomUUID().toString();
          dummyAuthTokens.put(newAuthToken, username);
          System.out.println("[HttpClient-Dummy] Logged in user: " + username);
          HashMap<String, Object> response = new HashMap<>();
          response.put("username", username);
          response.put("authToken", newAuthToken);
          return response;
        } else if ("DELETE".equals(method)) { // Logout
          if (authToken == null || !dummyAuthTokens.containsKey(authToken)) {
            throw new HttpException("Server returned error: 401 - Error: unauthorized");
          }
          String username = dummyAuthTokens.remove(authToken);
          System.out.println("[HttpClient-Dummy] Logged out user: " + username);
          return new HashMap<>(); // Success, empty body
        }
        break;

      case "/game":
        if ("GET".equals(method)) { // List Games
          if (authToken == null || !dummyAuthTokens.containsKey(authToken)) {
            throw new HttpException("Server returned error: 401 - Error: unauthorized");
          }
          System.out.println("[HttpClient-Dummy] Listing games for user: " + dummyAuthTokens.get(authToken));
          HashMap<String, Object> response = new HashMap<>();
          response.put("games", new ArrayList<>(dummyGames.values()));
          return response;
        } else if ("POST".equals(method)) { // Create Game
          if (authToken == null || !dummyAuthTokens.containsKey(authToken)) {
            throw new HttpException("Server returned error: 401 - Error: unauthorized");
          }
          @SuppressWarnings("unchecked")
          Map<String, String> createRequestBody = (request instanceof Map) ? (Map<String, String>) request : null;
          String gameName = (createRequestBody != null) ? createRequestBody.get("gameName") : null;

          if (gameName == null || gameName.isEmpty()) {
            throw new HttpException("Server returned error: 400 - Error: bad request");
          }

          int newId = nextGameId++;
          Map<String, Object> newGame = new HashMap<>();
          newGame.put("gameID", newId);
          newGame.put("gameName", gameName);
          newGame.put("whiteUsername", null);
          newGame.put("blackUsername", null);

          dummyGames.put(newId, newGame);
          System.out.println("[HttpClient-Dummy] Created game: " + gameName + " (ID: " + newId + ")");
          HashMap<String, Object> response = new HashMap<>();
          response.put("gameID", newId);
          return response;

        } else if ("PUT".equals(method)) { // Join / Observe Game
          if (authToken == null || !dummyAuthTokens.containsKey(authToken)) {
            throw new HttpException("Server returned error: 401 - Error: unauthorized");
          }

          @SuppressWarnings("unchecked")
          Map<String, Object> joinRequestBody = (request instanceof Map) ? (Map<String, Object>) request : null;
          Integer gameID = null;
          String playerColor = null; // Will be null if the command is to observe

          if (joinRequestBody != null && joinRequestBody.containsKey("gameID")) {

            Object idObj = joinRequestBody.get("gameID");
            if (idObj instanceof Number) {
              gameID = ((Number) idObj).intValue();
            } else {
              try {
                gameID = Integer.parseInt(idObj.toString());
              } catch (NumberFormatException | NullPointerException nfe) {
                throw new HttpException("Server returned error: 400 - Error: bad request (invalid gameID format)");
              }
            }
          } else {
            throw new HttpException("Server returned error: 400 - Error: bad request (missing gameID)");
          }

          if (joinRequestBody != null && joinRequestBody.containsKey("playerColor")) {
            playerColor = (String) joinRequestBody.get("playerColor"); // Should be "WHITE" or "BLACK"
          }

          if (!dummyGames.containsKey(gameID)) {
            throw new HttpException("Server returned error: 400 - Error: bad request (game not found)");
          }

          Map<String, Object> gameToJoin = dummyGames.get(gameID);
          String username = dummyAuthTokens.get(authToken);

          if (playerColor != null) { // Trying to JOIN
            if ("WHITE".equalsIgnoreCase(playerColor)) {
              if (gameToJoin.get("whiteUsername") != null) {
                throw new HttpException("Server returned error: 403 - Error: already taken");
              }
              gameToJoin.put("whiteUsername", username);
              System.out.println("[HttpClient-Dummy] User " + username + " joined game " + gameID + " as WHITE");
            } else if ("BLACK".equalsIgnoreCase(playerColor)) {
              if (gameToJoin.get("blackUsername") != null) {
                throw new HttpException("Server returned error: 403 - Error: already taken");
              }
              gameToJoin.put("blackUsername", username);
              System.out.println("[HttpClient-Dummy] User " + username + " joined game " + gameID + " as BLACK");
            } else {
              throw new HttpException("Server returned error: 400 - Error: bad request (invalid color)");
            }
          } else {
            System.out.println("[HttpClient-Dummy] User " + username + " observing game " + gameID);

          }
          return new HashMap<>();
        }
        break;
    }

    throw new HttpException(
        "Server returned error: 404 - Endpoint " + method + " " + path + " not found (or simulated)");
  }

  /**
   * Handles the HTTP response, parsing JSON for success or error messages.
   * (Used only when simulation is off)
   *
   * @param http The connected HttpURLConnection.
   * @return HashMap representing the JSON response body on success.
   * @throws HttpException if the server returns an error status code or parsing
   *                       fails.
   */
  private HashMap<String, Object> handleHttpResponse(HttpURLConnection http) throws HttpException {
    try {
      int responseCode = http.getResponseCode();

      if (responseCode >= 200 && responseCode < 300) {
        try (InputStream respBody = http.getInputStream()) {
          if (respBody == null || http.getContentLength() == 0) {
            return new HashMap<>();
          }
          String respData = streamToString(respBody);
          if (respData.isEmpty() || respData.equals("{}")) {
            return new HashMap<>();
          }
          try {
            HashMap<String, Object> result = gson.fromJson(respData, mapType);
            return (result != null) ? result : new HashMap<>();
          } catch (com.google.gson.JsonSyntaxException e) {
            throw new HttpException("Failed to parse successful HTTP server response JSON: " + e.getMessage()
                + "\nResponse Data: " + respData, e);
          }
        } catch (IOException e) {
          System.err.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
              + "Warning: IOException reading HTTP response body for success status " + responseCode
              + ": " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
          return new HashMap<>();
        }
      } else {
        String errorMessage;
        try (InputStream errorStream = http.getErrorStream()) {
          String errorBody = "";
          if (errorStream != null) {
            errorBody = streamToString(errorStream);
          }

          if (errorBody.isEmpty()) {
            errorMessage = http.getResponseMessage();
            if (errorMessage == null || errorMessage.trim().isEmpty()) {
              errorMessage = "Unknown server error (Code: " + responseCode + ")";
            }
          } else {
            try {
              Map<String, Object> errorJson = gson.fromJson(errorBody, mapType);
              if (errorJson != null && errorJson.containsKey("message")) {
                errorMessage = (String) errorJson.get("message");
              } else {
                System.err.println("Received unexpected error format from server. Using raw body.");
                errorMessage = errorBody;
              }
            } catch (com.google.gson.JsonSyntaxException jsonEx) {
              System.err.println("Received non-JSON error body from server. Using raw body.");
              errorMessage = errorBody;
            }
          }
        } catch (IOException ioEx) {
          System.err.println("Fatal error processing server HTTP error response. Using default message.");
          errorMessage = "Failed to read error stream (Code: " + responseCode + ")";
          ioEx.printStackTrace();
        }
        throw new HttpException("Server returned error: " + responseCode + " - " + errorMessage);
      }
    } catch (IOException e) {
      throw new HttpException("Failed to get HTTP response from server: " + e.getMessage(), e);
    }
  }

  /**
   * Reads an InputStream into a String using UTF-8 encoding.
   * 
   * @param inputStream The InputStream to read.
   * @return The content of the stream as a String.
   * @throws IOException If an I/O error occurs.
   */
  private String streamToString(InputStream inputStream) throws IOException {
    if (inputStream == null)
      return "";
    StringBuilder textBuilder = new StringBuilder();
    try (Reader reader = new BufferedReader(
        new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8))) {
      int c;
      while ((c = reader.read()) != -1) {
        textBuilder.append((char) c);
      }
    }
    return textBuilder.toString();
  }
}