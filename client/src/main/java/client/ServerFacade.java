package client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.lang.reflect.Type;

/**
 * Facade for the Chess server API. Handles all server communication.
 */
public class ServerFacade {
    private final String serverUrl;
    private static final Gson gson = new Gson();
    private static final Type mapType = new TypeToken<HashMap<String, Object>>() {
    }.getType();

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
    public HashMap<String, Object> register(String username, String password, String email)
            throws Exception {
        HashMap<String, String> requestBody = new HashMap<>();
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
    public HashMap<String, Object> login(String username, String password) throws Exception {
        HashMap<String, String> requestBody = new HashMap<>();
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
    public HashMap<String, Object> listGames(String authToken) throws Exception {
        HttpURLConnection http = sendRequest("GET", "/game", authToken, null);
        return handleResponse(http);
    }

    /**
     * Creates a new game.
     *
     * @param authToken The authentication token
     * @param gameName  The name of the game
     * @return Response data from the server 
     * @throws Exception if an error occurs during the operation
     */
    public HashMap<String, Object> createGame(String authToken, String gameName) throws Exception {
        HashMap<String, String> requestBody = new HashMap<>();
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
     *                    null/empty to observe
     * @return Response data from the server (empty on success for join/observe)
     * @throws Exception if an error occurs during the operation
     */
    public HashMap<String, Object> joinGame(String authToken, int gameID, String playerColor)
            throws Exception {
        HashMap<String, Object> requestBody = new HashMap<>();
        requestBody.put("gameID", gameID);
        if (playerColor != null && !playerColor.isEmpty()) {
            requestBody.put("playerColor", playerColor.toUpperCase());
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
    private HashMap<String, Object> handleResponse(HttpURLConnection http) throws Exception {
        int responseCode = http.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            if (http.getContentLength() == 0) {
                return new HashMap<>();
            } else {
                try (InputStream respBody = http.getInputStream()) {
                    String respData = streamToString(respBody);
                    HashMap<String, Object> result = gson.fromJson(respData, mapType);

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
                        HashMap<String, Object> errorJson = gson.fromJson(errorBody, mapType);
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
            return "";
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
