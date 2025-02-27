package handlers;

import spark.Request;
import spark.Response;

public class RegisterHandler extends AbstractHandler {

    @Override
    public Object handle(Request req, Response res) {
        try {
            RegisterRequest serviceRequest = deserialize(req, RegisterRequest.class);

            // TODO: Implement actual registration logic

            RegisterResult result = new RegisterResult(serviceRequest.username, "dummy-auth-token");
            return success(res, 200, result);

        } catch (Exception e) {
            return error(res, 500, "Error: " + e.getMessage());
        }
    }

    private static class RegisterRequest {
        private String username;
        private String password;
        private String email;
    }

    private static class RegisterResult {
        private final String username;
        private final String authToken;

        public RegisterResult(String username, String authToken) {
            this.username = username;
            this.authToken = authToken;
        }
    }
}