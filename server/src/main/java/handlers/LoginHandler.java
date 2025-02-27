package handlers;

import spark.Request;
import spark.Response;

public class LoginHandler extends AbstractHandler {

    @Override
    public Object handle(Request req, Response res) {
        try {
            LoginRequest serviceRequest = deserialize(req, LoginRequest.class);

            // TODO: Implement actual login authentication

            LoginResult serviceResult = new LoginResult(serviceRequest.username, "dummy-auth-token");
            return success(res, 200, serviceResult);

        } catch (Exception e) {
            // TODO: Imeplement correct error handling
            return error(res, 500, "Error: " + e.getMessage());
        }
    }

    private static class LoginRequest {
        private String username;
        private String password;
    }

    private static class LoginResult {
        private final String username;
        private final String authToken;

        public LoginResult(String username, String authToken) {
            this.username = username;
            this.authToken = authToken;
        }
    }
}