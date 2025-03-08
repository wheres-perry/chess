package handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import spark.Request;
import spark.Response;

import java.util.Map;

/**
 * Base class for all API endpoint handlers in the chess system.
 * Provides shared utilities for request processing, authentication
 * verification,
 * and standardized response formatting across the application.
 */
public abstract class AbstractHandler {
    protected final Gson gson = new Gson();

    /**
     * Core processing method that all handlers must implement.
     * Orchestrates the handling of HTTP requests based on endpoint-specific
     * business logic.
     * 
     * @param req The incoming Spark HTTP request
     * @param res The outgoing Spark HTTP response
     * @return The formatted response content
     */
    public abstract Object handle(Request req, Response res);

    /**
     * Confirms the validity of authentication credentials in request headers.
     * Extracts and validates authorization tokens to ensure proper access rights.
     * 
     * @param req     The request containing authentication credentials
     * @param authDAO The authentication data interface for token verification
     * @return The authenticated username if successful
     * @throws DataAccessException If database operations encounter issues
     * @throws RuntimeException    If authentication is invalid or missing
     */
    protected String validateAuthToken(Request req, AuthDAO authDAO) throws DataAccessException {
        String authToken = req.headers("authorization");
        if (authToken == null || authToken.isEmpty()) {
            throw new RuntimeException("Error: unauthorized");
        }

        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            throw new RuntimeException("Error: unauthorized");
        }

        return authData.username();
    }

    /**
     * Transforms JSON request content into corresponding Java objects.
     * Simplifies the extraction and conversion of client-provided data.
     * 
     * @param <T>     The target object type for conversion
     * @param req     The request containing JSON data
     * @param myClass The class to instantiate from JSON
     * @return A populated object of the specified type
     * @throws JsonSyntaxException If JSON parsing fails
     */
    protected <T> T deserialize(Request req, Class<T> myClass) throws JsonSyntaxException {
        return gson.fromJson(req.body(), myClass);
    }

    /**
     * Generates standardized error responses with consistent formatting.
     * Ensures uniform error reporting across all handler implementations.
     * 
     * @param res        The response to configure
     * @param statusCode The HTTP status code to apply
     * @param message    The explanatory error message
     * @return JSON-formatted error response
     */
    protected String error(Response res, int statusCode, String message) {
        res.status(statusCode);
        return gson.toJson(Map.of("message", message));
    }

    /**
     * Creates success responses with payload data.
     * Maintains consistency in successful response structures throughout the API.
     * 
     * @param res        The response to configure
     * @param statusCode The HTTP status code to apply
     * @param data       The content to include in the response
     * @return JSON-formatted success response with data
     */
    protected String success(Response res, int statusCode, Object data) {
        res.status(statusCode);
        return gson.toJson(data);
    }

    /**
     * Produces empty success responses for operations without return data.
     * Simplifies handlers that only need to indicate successful completion.
     * 
     * @param res        The response to configure
     * @param statusCode The HTTP status code to apply
     * @return Empty JSON object string
     */
    protected String success(Response res, int statusCode) {
        res.status(statusCode);
        return "{}";
    }
}