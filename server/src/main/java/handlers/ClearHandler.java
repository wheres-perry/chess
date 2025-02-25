package handlers;

import spark.Request;
import spark.Response;

public class ClearHandler implements Handler {
    @Override
    public Object handle(Request req, Response res) {
        // TODO: Implement clear database functionality
        return "{}";
    }
}