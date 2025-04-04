package client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;

/**
 * Facade for the Chess server API. Handles all server communication.
 */
public class ServerFacade {
    private final String serverUrl;
    private static final Gson gson = new Gson();
    private static final Type mapType = new TypeToken<HashMap<String, Object>>() {
    }.getType();

    public ServerFacade(String url) {
        serverUrl = url;
    }

    /**
     * Clears the server database via the DELETE /db endpoint.
     * Intended for debugging. Does not return anything on success.
     */
    public void clearDatabase() throws Exception {
        HttpURLConnection http = sendRequest("DELETE", "/db", null, null);
        handleResponse(http);
    }

    public HashMap<String, Object> register(String username, String password, String email) throws Exception {
        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);
        requestBody.put("email", email);
        HttpURLConnection http = sendRequest("POST", "/user", null, requestBody);
        return handleResponse(http);
    }

    public HashMap<String, Object> login(String username, String password) throws Exception {
        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);
        HttpURLConnection http = sendRequest("POST", "/session", null, requestBody);
        return handleResponse(http);
    }

    public void logout(String authToken) throws Exception {
        HttpURLConnection http = sendRequest("DELETE", "/session", authToken, null);
        handleResponse(http);
    }

    public HashMap<String, Object> listGames(String authToken) throws Exception {
        HttpURLConnection http = sendRequest("GET", "/game", authToken, null);
        return handleResponse(http);
    }

    public HashMap<String, Object> createGame(String authToken, String gameName) throws Exception {
        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("gameName", gameName);
        HttpURLConnection http = sendRequest("POST", "/game", authToken, requestBody);
        return handleResponse(http);
    }

    /**
     * Joins an existing game as a player (WHITE or BLACK).
     * Sends a PUT request to /game with gameID and playerColor.
     */
    public void joinGame(String authToken, int gameID, String playerColor) throws Exception {
        HashMap<String, Object> requestBody = new HashMap<>();
        requestBody.put("gameID", gameID);
        if (playerColor != null && (playerColor.equalsIgnoreCase("WHITE") || playerColor.equalsIgnoreCase("BLACK"))) {
            requestBody.put("playerColor", playerColor.toUpperCase());
        } else {
            throw new IllegalArgumentException("Player color (WHITE or BLACK) is required to join as player.");
        }
        HttpURLConnection http = sendRequest("PUT", "/game", authToken, requestBody);
        handleResponse(http);
    }

    /**
     * Observes an existing game.
     * Sends a PUT request to /game with only gameID (playerColor is omitted).
     */
    public void observeGame(String authToken, int gameID) throws Exception {
        HashMap<String, Object> requestBody = new HashMap<>();
        requestBody.put("gameID", gameID);
        HttpURLConnection http = sendRequest("PUT", "/game", authToken, requestBody);
        handleResponse(http);
    }

    private HttpURLConnection sendRequest(String method, String path, String authToken, Object request)
            throws Exception {
        URL url = new URI(serverUrl + path).toURL();
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod(method);
        http.setReadTimeout(5000);

        if (authToken != null && !authToken.isEmpty()) {
            http.setRequestProperty("Authorization", authToken);
        }

        if (request != null) {
            if (!method.equals("GET") && !method.equals("DELETE")) {
                http.setDoOutput(true);
                http.addRequestProperty("Content-Type", "application/json");
                String reqBody = gson.toJson(request);
                try (OutputStream reqStream = http.getOutputStream()) {
                    reqStream.write(reqBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
            }
        }

        http.connect();
        http.getResponseCode();
        return http;
    }

    private HashMap<String, Object> handleResponse(HttpURLConnection http) throws Exception {
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
                    throw new Exception("Failed to parse successful server response JSON: " + e.getMessage()
                            + "\nResponse Data: " + respData);
                }
            } catch (IOException e) {
                System.err.println("Warning: IOException reading response body for success status " + responseCode
                        + ": " + e.getMessage());
                return new HashMap<>();
            }

        } else {
            String errorMessage;
            try (InputStream errorStream = http.getErrorStream()) {
                String errorBody = (errorStream != null) ? streamToString(errorStream) : "";
                try {
                    HashMap<String, Object> errorJson = gson.fromJson(errorBody, mapType);
                    if (errorJson != null && errorJson.containsKey("message")) {
                        errorMessage = (String) errorJson.get("message");
                    } else if (!errorBody.isEmpty()) {
                        errorMessage = errorBody;
                    } else {
                        errorMessage = "Error: " + responseCode + " " + http.getResponseMessage();
                    }
                } catch (com.google.gson.JsonSyntaxException ex) {
                    errorMessage = !errorBody.isEmpty() ? errorBody
                            : "Error: " + responseCode + " " + http.getResponseMessage();
                }
            } catch (IOException e) {
                errorMessage = "Error reading error stream: " + responseCode + " " + http.getResponseMessage();
            }

            if (errorMessage != null && errorMessage.startsWith("Error: ")) {
                errorMessage = errorMessage.substring("Error: ".length());
            }
            throw new Exception(errorMessage);
        }
    }

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