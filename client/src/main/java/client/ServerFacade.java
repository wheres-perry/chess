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
     * @param email The email address
     * @return Response data from the server
     * @throws Exception if an error occurs during the operation
     */
    public Map<String, Object> register(String username, String password, String email)
            throws Exception {
        // Stub implementation
        return null;
    }

    /**
     * Logs in a user.
     * 
     * @param username The username
     * @param password The password
     * @return Response data from the server
     * @throws Exception if an error occurs during the operation
     */
    public Map<String, Object> login(String username, String password) throws Exception {
        // Stub implementation
        return null;
    }

    /**
     * Logs out a user.
     * 
     * @param authToken The authentication token
     * @throws Exception if an error occurs during the operation
     */
    public void logout(String authToken) throws Exception {
        // Stub implementation
    }

    /**
     * Lists all games.
     * 
     * @param authToken The authentication token
     * @return Response data from the server
     * @throws Exception if an error occurs during the operation
     */
    public Map<String, Object> listGames(String authToken) throws Exception {
        // Stub implementation
        return null;
    }

    /**
     * Creates a new game.
     * 
     * @param authToken The authentication token
     * @param gameName The name of the game
     * @return Response data from the server
     * @throws Exception if an error occurs during the operation
     */
    public Map<String, Object> createGame(String authToken, String gameName) throws Exception {
        // Stub implementation
        return null;
    }

    /**
     * Joins an existing game.
     * 
     * @param authToken The authentication token
     * @param gameID The ID of the game to join
     * @param playerColor The color the player wants to play as (WHITE/BLACK), or null to observe
     * @return Response data from the server
     * @throws Exception if an error occurs during the operation
     */
    public Map<String, Object> joinGame(String authToken, int gameID, String playerColor)
            throws Exception {
        // Stub implementation
        return null;
    }

    /**
     * Sends an HTTP request to the server.
     * 
     * @param method The HTTP method (GET, POST, PUT, DELETE)
     * @param path The API endpoint path
     * @param authToken The authentication token, or null if not needed
     * @param request The request body object, or null if no body is needed
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
     * Handles the HTTP response from the server.
     * 
     * @param http The HttpURLConnection object representing the connection
     * @return Response data as a Map, if successful
     * @throws Exception if the request was unsuccessful or an error occurs
     */
    private Map<String, Object> handleResponse(HttpURLConnection http) throws Exception {
        if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (InputStream respBody = http.getInputStream()) {
                String respData = streamToString(respBody);
                return gson.fromJson(respData, Map.class);
            }
        } else {
            try (InputStream errorStream = http.getErrorStream()) {
                String errorData = errorStream != null ? streamToString(errorStream)
                        : "Error: " + http.getResponseCode() + " " + http.getResponseMessage();
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
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            int c;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return textBuilder.toString();
    }
}
