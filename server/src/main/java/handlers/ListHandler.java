package handlers;

import spark.Request;
import spark.Response;
import requests.ListRequest;
import results.ListResult;
import service.ChessService;

/**
 * Specialized handler for retrieving game collections.
 * Provides functionality for users to discover available chess matches.
 */
public class ListHandler extends AbstractHandler {
    private final ChessService chessService;

    /**
     * Creates a game listing handler with service access.
     * 
     * @param chessService The service component for retrieving game data
     */
    public ListHandler(ChessService chessService) {
        this.chessService = chessService;
    }

    /**
     * Processes requests for available game listings.
     * Filters and returns games based on user permissions and state.
     * 
     * @param req The request containing authentication for filtering
     * @param res The response object for returning results
     * @return Collection of available chess games or error details
     */
    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            if (authToken == null || authToken.isEmpty()) {
                return error(res, 401, "Error: unauthorized");
            }

            ListRequest serviceRequest = new ListRequest(authToken);
            ListResult serviceResult = chessService.listAll(serviceRequest);

            return success(res, 200, serviceResult);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.contains("unauthorized")) {
                return error(res, 401, "Error: unauthorized");
            }
            return error(res, 500, "Error: " + e.getMessage());
        }
    }
}