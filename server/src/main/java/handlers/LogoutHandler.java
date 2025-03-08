package handlers;

import requests.LogoutRequest;
import results.LogoutResult;
import service.ChessService;
import spark.Request;
import spark.Response;

/**
 * Specialized handler for managing user session termination.
 * Handles the secure invalidation of authentication tokens.
 */
public class LogoutHandler extends AbstractHandler {

    private final ChessService chessService;

    /**
     * Creates a logout handler with access to required services.
     * 
     * @param chessService The service component for authentication operations
     */
    public LogoutHandler(ChessService chessService) {
        this.chessService = chessService;
    }

    /**
     * Processes session termination requests from authenticated users.
     * Invalidates active tokens to prevent further authorized access.
     * 
     * @param req The request containing authentication to invalidate
     * @param res The response object for returning results
     * @return Empty success response or appropriate error information
     */
    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            if (authToken == null || authToken.isEmpty()) {
                return error(res, 401, "Error: unauthorized");
            }

            LogoutRequest serviceRequest = new LogoutRequest(authToken);
            @SuppressWarnings("unused") // Stops warning because we don't need the result, I'm keeping it just for case
            LogoutResult serviceResult = chessService.logout(serviceRequest);

            return success(res, 200);

        } catch (Exception e) {
            String message = e.getMessage();
            if (message.contains("unauthorize") && message != null) {
                return error(res, 401, "Error: unauthorized");
            }
            return error(res, 500, "Error: " + e.getMessage());
        }
    }
}