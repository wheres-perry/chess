package handlers;

import requests.RegisterRequest;
import results.RegisterResult;
import spark.Request;
import spark.Response;

public class RegisterHandler extends AbstractHandler {

    @Override
    public Object handle(Request req, Response res) {
        try {
            RegisterRequest serviceRequest = deserialize(req, RegisterRequest.class);

            // TODO: Implement actual registration logic

            RegisterResult result = new RegisterResult(serviceRequest.getUsername(), "dummy-auth-token");
            return success(res, 200, result);

        } catch (Exception e) {
            return error(res, 500, "Error: " + e.getMessage());
        }
    }
}