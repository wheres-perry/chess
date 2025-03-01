package handlers;

import com.google.gson.Gson;  
import com.google.gson.JsonSyntaxException;
import spark.Request;
import spark.Response;

import java.util.Map;

public abstract class AbstractHandler {
    protected final Gson gson = new Gson();

    /**
     * Handle method 
     */
    public abstract Object handle(Request req, Response res);

    /**
     * Deserializes request body to specified class
     */
    protected <T> T deserialize(Request req, Class<T> myClass) throws JsonSyntaxException {
        return gson.fromJson(req.body(), myClass);
    }

    /**
     * Creates error response with given status code andgiven  message
     */
    protected String error(Response res, int statusCode, String message) {
        res.status(statusCode);
        return gson.toJson(Map.of("message", message));
    }

    /**
     * Creates a success response, with data attatched
     */
    protected String success(Response res, int statusCode, Object data) {
        res.status(statusCode);
        return gson.toJson(data);
    }

    /**
     * Creates a success response, with no data attatched
     */
    protected String success(Response res, int statusCode) {
        res.status(statusCode);
        return "{}";
    }
}