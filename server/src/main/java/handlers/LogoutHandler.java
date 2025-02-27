package handlers;

import spark.Request;
import spark.Response;

public class LogoutHandler extends AbstractHandler {

    @Override
    public Object handle(Request req, Response res) {
        try {
            LogoutRequest serviceRequest = deserialize(req, LogoutRequest.class);
            // TODO: Implement actual logout logic


            LogoutResult serviceResult = new LogoutResult();
            return success(res, 200);

        } catch (Exception e) {
            // TODO: Implement correct error handling
            return error(res, 500, "Error: " + e.getMessage());
        }
    }

    private static class LogoutRequest {
        static String authToken;
    }

    private static class LogoutResult {
        // Empty result
    }
}