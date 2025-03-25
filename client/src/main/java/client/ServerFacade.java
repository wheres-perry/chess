package client;

import com.google.gson.Gson;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Facade for the Chess server API. Handles all server communication.
 */
public class ServerFacade {
    private final String serverUrl;
    private static final Gson gson = new Gson();

    /**
     * Creates a new ServerFacade.
     *
     * @param url The URL of the server
     */
    public ServerFacade(String url) {
        serverUrl = url;
    }

    /**
     * Clears the database.
     *
     * @throws Exception if an error occurs during the operation
     */
    public void clear() throws Exception {
        HttpURLConnection http = sendRequest("DELETE", "/db", null, null);
        handleResponse(http);
    }

    /**
     * Registers a new user.
     *
     * @param username The username
     * @param password The password
     * @param email    The email address
     * @return Response data from the server (likely includes authToken)
     * @throws Exception if an error occurs during the operation
     */
    public Map<String, Object> register(String username, String password, String email)
            throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);
        requestBody.put("email", email);
        HttpURLConnection http = sendRequest("POST", "/user", null, requestBody);
        return handleResponse(http);
    }

    /**
     * Logs in a user.
     *
     * @param username The username
     * @param password The password
     * @return Response data from the server (likely includes authToken)
     * @throws Exception if an error occurs during the operation
     */
    public Map<String, Object> login(String username, String password) throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);
        HttpURLConnection http = sendRequest("POST", "/session", null, requestBody);
        return handleResponse(http);
    }

    /**
     * Logs out a user.
     *
     * @param authToken The authentication token
     * @throws Exception if an error occurs during the operation
     */
    public void logout(String authToken) throws Exception {
        HttpURLConnection http = sendRequest("DELETE", "/session", authToken, null);
        handleResponse(http);
    }

    /**
     * Lists all games.
     *
     * @param authToken The authentication token
     * @return Response data from the server (list of games)
     * @throws Exception if an error occurs during the operation
     */
    public Map<String, Object> listGames(String authToken) throws Exception {
        HttpURLConnection http = sendRequest("GET", "/game", authToken, null);
        return handleResponse(http);
    }

    /**
     * Creates a new game.
     *
     * @param authToken The authentication token
     * @param gameName  The name of the game
     * @return Response data from the server (likely includes gameID)
     * @throws Exception if an error occurs during the operation
     */
    public Map<String, Object> createGame(String authToken, String gameName) throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("gameName", gameName);
        HttpURLConnection http = sendRequest("POST", "/game", authToken, requestBody);
        return handleResponse(http);
    }

    /**
     * Joins an existing game.
     *
     * @param authToken   The authentication token
     * @param gameID      The ID of the game to join
     * @param playerColor The color the player wants to play as (WHITE/BLACK), or
     *                    null/empty to
     *                    observe
     * @return Response data from the server (empty on success for join/observe)
     * @throws Exception if an error occurs during the operation
     */
    public Map<String, Object> joinGame(String authToken, int gameID, String playerColor)
            throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        if (playerColor != null && !playerColor.isEmpty()) {
            requestBody.put("playerColor", playerColor.toUpperCase()); // Ensure consistent casing
        }
        HttpURLConnection http = sendRequest("PUT", "/game", authToken, requestBody);
        return handleResponse(http);
    }

    /**
     * Sends an HTTP request to the server.
     *
     * @param method    The HTTP method (GET, POST, PUT, DELETE)
     * @param path      The API endpoint path
     * @param authToken The authentication token, or null if not needed
     * @param request   The request body object, or null if no body is needed
     * @return The HttpURLConnection object representing the connection
     * @throws Exception if an error occurs during the operation
     */
    private HttpURLConnection sendRequest(String method, String path, String authToken,
            Object request) throws Exception {
        URL url = new URI(serverUrl + path).toURL();
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod(method);

        if (authToken != null) {
            http.setRequestProperty("Authorization", authToken);
        }

        if (request != null) {
            http.setDoOutput(true);
            http.addRequestProperty("Content-Type", "application/json");
            String reqBody = gson.toJson(request);
            try (OutputStream reqStream = http.getOutputStream()) {
                reqStream.write(reqBody.getBytes());
            }
        }

        http.connect();
        return http;
    }

    /**
     * Handles the HTTP response from the server. Checks status code and parses
     * body. Returns an
     * empty map for successful responses with no body.
     *
     * @param http The HttpURLConnection object representing the connection
     * @return Response data as a Map, if successful and body exists, or empty map
     *         otherwise.
     * @throws Exception containing error message if the request was unsuccessful
     */
    private Map<String, Object> handleResponse(HttpURLConnection http) throws Exception {
        int responseCode = http.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            if (http.getContentLength() == 0) {
                // Successful request, no content to return
                return new HashMap<>(); // Return empty map for success without body
            } else {
                try (InputStream respBody = http.getInputStream()) {
                    String respData = streamToString(respBody);
                    Map<String, Object> result = gson.fromJson(respData, type);

                    if (result != null) {
                        return result;
                    } else {
                        return new HashMap<>();
                    }
                }
            }
        } else { // Handle errors
            try (InputStream errorStream = http.getErrorStream()) {
                String errorData = "Error: " + responseCode + " " + http.getResponseMessage();
                if (errorStream != null) {
                    String errorBody = streamToString(errorStream);
                    // Attempt to parse error message from server if JSON
                    try {
                        Map<String, Object> errorJson = gson.fromJson(errorBody, );
                        if (errorJson != null && errorJson.containsKey("error")) {
                            errorData = (String) errorJson.get("error");
                        }
                    } catch (com.google.gson.JsonSyntaxException ex) {
                        errorData = errorBody;
                    }
                }
                throw new Exception(errorData); 
            }
        }
    }

    /**
     * Converts an InputStream to a String.
     *
     * @param inputStream The input stream to convert
     * @return The stream contents as a String
     * @throws IOException if an I/O error occurs
     */
    private String streamToString(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return ""; // Handle null stream gracefully
        }
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
