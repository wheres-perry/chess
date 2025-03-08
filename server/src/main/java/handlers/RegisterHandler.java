package handlers;

import requests.RegisterRequest;
import results.RegisterResult;
import service.ChessService;
import spark.Request;
import spark.Response;

/**
 * Specialized handler for processing new user registration requests.
 * Manages the creation of player accounts and initial authentication setup.
 */
public class RegisterHandler extends AbstractHandler {
    private final ChessService chessService;

    /**
     * Establishes a new registration handler with chess service access.
     * 
     * @param chessService The service layer component for user management
     */
    public RegisterHandler(ChessService chessService) {
        this.chessService = chessService;
    }

    /**
     * Processes account creation requests from new users.
     * Handles credential validation, account creation, and initial authentication.
     * 
     * @param req The registration request with username, password and email
     * @param res The response object for sending results
     * @return Authentication credentials for the newly created user or error
     *         details
     */
    @Override
    public Object handle(Request req, Response res) {
        try {
            RegisterRequest serviceRequest = deserialize(req, RegisterRequest.class);
            RegisterResult serviceResult = chessService.register(serviceRequest);

            return success(res, 200, serviceResult);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null) {
                if (message.contains("already taken")) {
                    return error(res, 403, "Error: already taken");
                } else if (message.contains("bad request")) {
                    return error(res, 400, "Error: bad request");
                }
            }
            return error(res, 500, "Error: " + e.getMessage());
        }
    }
}