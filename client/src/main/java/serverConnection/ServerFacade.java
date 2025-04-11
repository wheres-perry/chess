package serverConnection;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map; // Added for clarity

/**
 * Facade for the Chess server API. Handles all server communication.
 */
public class ServerFacade {
    private final String serverUrl;
    private static final Gson gson = new Gson();
    // Use Map<String, Object> for flexibility with JSON responses
    private static final Type mapType = new TypeToken<HashMap<String, Object>>() {
    }.getType();

    // A custom xception class for Server Errors
    public static class ServerException extends Exception {
        public ServerException(String message) {
            super(message);
        }

        public ServerException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public ServerFacade(String url) {
        serverUrl = url;
    }

    /**
     * Clears the server database via the DELETE /db endpoint.
     * Intended for debugging. Does not return anything on success.
     *
     * @throws ServerException if the server returns an error or communication
     *                         fails.
     */
    public void clearDatabase() throws ServerException {
        HttpURLConnection http = sendRequest("DELETE", "/db", null, null);
        handleResponse(http); // Throws ServerException on error
    }

    /**
     * Registers a new user via the POST /user endpoint.
     *
     * @param username Username for the new user.
     * @param password Password for the new user.
     * @param email    Email for the new user.
     * @return A HashMap containing the server's response (e.g., authToken,
     *         username).
     * @throws ServerException if registration fails or communication fails.
     */
    public HashMap<String, Object> register(String username, String password, String email) throws ServerException {
        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);
        requestBody.put("email", email);
        HttpURLConnection http = sendRequest("POST", "/user", null, requestBody);
        return handleResponse(http);
    }

    /**
     * Logs in an existing user via the POST /session endpoint.
     *
     * @param username User's username.
     * @param password User's password.
     * @return A HashMap containing the server's response (e.g., authToken,
     *         username).
     * @throws ServerException if login fails or communication fails.
     */
    public HashMap<String, Object> login(String username, String password) throws ServerException {
        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);
        HttpURLConnection http = sendRequest("POST", "/session", null, requestBody);
        return handleResponse(http);
    }

    /**
     * Logs out the user associated with the given authToken via DELETE /session.
     *
     * @param authToken The authentication token of the user to log out.
     * @throws ServerException if logout fails or communication fails.
     */
    public void logout(String authToken) throws ServerException {
        HttpURLConnection http = sendRequest("DELETE", "/session", authToken, null);
        handleResponse(http); // Throws ServerException on error
    }

    /**
     * Lists all available games via the GET /game endpoint. Requires
     * authentication.
     *
     * @param authToken The user's authentication token.
     * @return A HashMap containing the server's response (expected to have a
     *         "games" list).
     * @throws ServerException if listing fails or communication fails.
     */
    public HashMap<String, Object> listGames(String authToken) throws ServerException {
        HttpURLConnection http = sendRequest("GET", "/game", authToken, null);
        return handleResponse(http);
    }

    /**
     * Creates a new game via the POST /game endpoint. Requires authentication.
     *
     * @param authToken The user's authentication token.
     * @param gameName  The desired name for the new game.
     * @return A HashMap containing the server's response (expected to have
     *         "gameID").
     * @throws ServerException if creation fails or communication fails.
     */
    public HashMap<String, Object> createGame(String authToken, String gameName) throws ServerException {
        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("gameName", gameName);
        HttpURLConnection http = sendRequest("POST", "/game", authToken, requestBody);
        return handleResponse(http);
    }

    /**
     * Joins an existing game as a player (WHITE or BLACK) via PUT /game.
     * Requires authentication.
     *
     * @param authToken   The user's authentication token.
     * @param gameID      The ID of the game to join.
     * @param playerColor The desired color ("WHITE" or "BLACK").
     * @throws ServerException          if joining fails or communication fails.
     * @throws IllegalArgumentException if playerColor is invalid.
     */
    public void joinGame(String authToken, int gameID, String playerColor) throws ServerException {
        HashMap<String, Object> requestBody = new HashMap<>();
        requestBody.put("gameID", gameID);
        if (playerColor != null && (playerColor.equalsIgnoreCase("WHITE") || playerColor.equalsIgnoreCase("BLACK"))) {
            requestBody.put("playerColor", playerColor.toUpperCase());
        } else {
            throw new IllegalArgumentException("Player color (WHITE or BLACK) is required to join as player.");
        }
        HttpURLConnection http = sendRequest("PUT", "/game", authToken, requestBody);
        handleResponse(http); // Throws ServerException on error
    }

    /**
     * Joins an existing game as an observer via PUT /game.
     * Requires authentication. The request body only includes the gameID.
     *
     * @param authToken The user's authentication token.
     * @param gameID    The ID of the game to observe.
     * @throws ServerException if observing fails or communication fails.
     */
    public void observeGame(String authToken, int gameID) throws ServerException {
        HashMap<String, Object> requestBody = new HashMap<>();
        requestBody.put("gameID", gameID);
        // No "playerColor" key in the request body for observing
        HttpURLConnection http = sendRequest("PUT", "/game", authToken, requestBody);
        handleResponse(http); // Throws ServerException on error
    }

    /**
     * Sends an HTTP request to the server.
     *
     * @param method    HTTP method (e.g., "GET", "POST", "DELETE", "PUT").
     * @param path      Server endpoint path (e.g., "/user", "/game").
     * @param authToken Authorization token (can be null).
     * @param request   Request body object (can be null for GET/DELETE).
     * @return The HttpURLConnection object after connection attempt.
     * @throws ServerException if URL creation or connection setup fails.
     */
    private HttpURLConnection sendRequest(String method, String path, String authToken, Object request)
            throws ServerException {
        try {
            URL url = new URI(serverUrl + path).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setReadTimeout(5000); // Set a read timeout

            if (authToken != null && !authToken.isEmpty()) {
                http.setRequestProperty("Authorization", authToken);
            }

            // Only write body for methods that typically have one
            if (request != null && !method.equals("GET") && !method.equals("DELETE")) {
                http.setDoOutput(true);
                http.addRequestProperty("Content-Type", "application/json");
                String reqBody = gson.toJson(request);
                try (OutputStream reqStream = http.getOutputStream()) {
                    reqStream.write(reqBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
            }

            http.connect(); // Establish connection
            http.getResponseCode();
            return http;
        } catch (Exception e) {
            // Wrap IO/URISyntax exceptions into ServerException
            throw new ServerException("Failed to send request: " + e.getMessage(), e);
        }
    }

    /**
     * Handles the HTTP response, parsing JSON for success or error messages.
     *
     * @param http The connected HttpURLConnection.
     * @return HashMap representing the JSON response body on success.
     * @throws ServerException if the server returns an error status code or parsing
     *                         fails.
     */
    private HashMap<String, Object> handleResponse(HttpURLConnection http) throws ServerException {
        try {
            int responseCode = http.getResponseCode();

            if (responseCode >= 200 && responseCode < 300) { // Success range
                try (InputStream respBody = http.getInputStream()) {
                    // Handle cases with no content (e.g., 204 No Content, or empty 200 OK)
                    if (respBody == null || http.getContentLength() == 0) {
                        return new HashMap<>();
                    }

                    String respData = streamToString(respBody);
                    if (respData.isEmpty() || respData.equals("{}")) { // Check for empty JSON object
                        return new HashMap<>();
                    }

                    try {
                        HashMap<String, Object> result = gson.fromJson(respData, mapType);
                        return (result != null) ? result : new HashMap<>();
                    } catch (com.google.gson.JsonSyntaxException e) {
                        throw new ServerException("Failed to parse successful server response JSON: " + e.getMessage()
                                + "\nResponse Data: " + respData, e);
                    }
                } catch (IOException e) {
                    // Error reading the success response body is unusual
                    System.err.println(ui.EscapeSequences.SET_TEXT_COLOR_YELLOW
                            + "Warning: IOException reading response body for success status " + responseCode
                            + ": " + e.getMessage() + ui.EscapeSequences.RESET_TEXT_COLOR);
                    return new HashMap<>();
                }
            } else { // Error range
                String errorMessage;
                try (InputStream errorStream = http.getErrorStream()) {
                    String errorBody = "";
                    if (errorStream != null) {
                        errorBody = streamToString(errorStream);
                    }

                    // If we have an empty error body, use response message
                    if (errorBody.isEmpty()) {
                        errorMessage = http.getResponseMessage();
                    } else {
                        // Try to parse as JSON - expecting standard error format
                        Map<String, Object> errorJson = gson.fromJson(errorBody, mapType);

                        if (errorJson == null || !errorJson.containsKey("message")) {
                            System.err.println("Received unexpected error format from server. Terminating.");
                            System.err.println("Response code: " + responseCode);
                            System.err.println("Error body: " + errorBody);
                            System.exit(1);
                        }

                        errorMessage = (String) errorJson.get("message");
                    }
                } catch (IOException | com.google.gson.JsonSyntaxException e) {
                    System.err.println("Fatal error processing server response. Terminating.");
                    System.err.println("Response code: " + responseCode);
                    e.printStackTrace();
                    System.exit(1);
                    // The following line will never execute but is needed for compilation 🙄
                    errorMessage = "";
                }

                throw new ServerException("Server returned error: " + responseCode + " - " + errorMessage);
            }
        } catch (IOException e) {
            // Catch exceptions from getResponseCode() itself
            throw new ServerException("Failed to get response from server: " + e.getMessage(), e);
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