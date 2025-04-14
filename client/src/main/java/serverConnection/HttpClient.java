package serverconnection;

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
 */
public class HttpClient {

  private final String serverHttpUrl;
  private static final Gson JSON = new Gson();
  private static final Type MAPTYPE = new TypeToken<HashMap<String, Object>>() {
  }.getType();

  public HttpClient(String serverUrl) {
    this.serverHttpUrl = serverUrl;
  }

  /**
   * Sends an HTTP request to the server.
   *
   * @param method    HTTP method (e.g., "GET", "POST", "DELETE", "PUT").
   * @param path      Server endpoint path (e.g., "/user", "/game").
   * @param authToken Authorization token (can be null).
   * @param request   Request body object (can be null for GET/DELETE).
   * @return A HashMap representing the JSON response body on success.
   * @throws HttpException if the request fails or the server returns an error.
   */
  public HashMap<String, Object> sendRequest(String method, String path, String authToken, Object request)
      throws HttpException {

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
        String reqBody = JSON.toJson(request);
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
            HashMap<String, Object> result = JSON.fromJson(respData, MAPTYPE);
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
              Map<String, Object> errorJson = JSON.fromJson(errorBody, MAPTYPE);
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
}