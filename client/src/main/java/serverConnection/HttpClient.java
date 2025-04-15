package serverconnection;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
      InputStream respStream;
      if (responseCode >= 200 && responseCode < 300) {
        respStream = http.getInputStream();
      } else {
        respStream = http.getErrorStream();
      }

      String respData = "";
      if (respStream != null) {
        respData = streamToString(respStream);
      }

      if (responseCode >= 200 && responseCode < 300) {
        if (respData.isEmpty() || respData.equals("{}")) {
          return new HashMap<>();
        }
        HashMap<String, Object> result = JSON.fromJson(respData, MAPTYPE);
        if (result == null) {
          return new HashMap<>();
        }
        return result;
      } else {
        throw new HttpException("Request failed");
      }
    } catch (Exception e) {
      throw new HttpException("Request failed");
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