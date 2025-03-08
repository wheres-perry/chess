package handlers;

import requests.LoginRequest;
import results.LoginResult;
import service.ChessService;
import spark.Request;
import spark.Response;

/**
 * Dedicated handler for processing user authentication requests.
 * Manages credential verification and session establishment.
 */
public class LoginHandler extends AbstractHandler {

    private final ChessService chessService;

    /**
     * Establishes a login handler with service layer access.
     * 
     * @param chessService The service component for authentication operations
     */
    public LoginHandler(ChessService chessService) {
        this.chessService = chessService;
    }

    /**
     * Processes user login attempts and establishes authenticated sessions.
     * Validates credentials and generates secure tokens upon successful
     * verification.
     * 
     * @param req The request containing user authentication credentials
     * @param res The response object for returning results
     * @return Authentication token and user information or error details
     */
    @Override
    public Object handle(Request req, Response res) {
        try {
            LoginRequest serviceRequest = deserialize(req, LoginRequest.class);
            LoginResult serviceResult = chessService.login(serviceRequest);

            return success(res, 200, serviceResult);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null) {
                if (message.contains("unauthorized")) {
                    return error(res, 401, "Error: unauthorized");
                } else if (message.contains("bad request")) {
                    return error(res, 400, "Error: bad request");
                }
            }
            return error(res, 500, "Error: " + e.getMessage());
        }
    }
}