package handlers;

import requests.NewGameRequest;
import results.NewGameResult;
import service.ChessService;
import spark.Request;
import spark.Response;
import com.google.gson.JsonObject;

/**
 * Dedicated handler for chess game creation requests.
 * Manages the initialization process for new chess matches in the system.
 */
public class NewGameHandler extends AbstractHandler {
    private final ChessService chessService;

    /**
     * Constructs a game creation handler with access to chess services.
     * 
     * @param chessService The service component for game operations
     */
    public NewGameHandler(ChessService chessService) {
        this.chessService = chessService;
    }

    /**
     * Processes requests to establish new chess matches.
     * Validates user authorization and game parameters before creation.
     * 
     * @param req The request containing game details and authentication
     * @param res The response object for returning results
     * @return Game identifier information or error details
     */
    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            if (authToken == null || authToken.isEmpty()) {
                return error(res, 401, "Error: unauthorized");
            }

            JsonObject requestBody = gson.fromJson(req.body(), JsonObject.class);
            if (!requestBody.has("gameName")) {
                return error(res, 400, "Error: bad request");
            }

            String gameName = requestBody.get("gameName").getAsString();
            NewGameRequest serviceRequest = new NewGameRequest(gameName, authToken);
            NewGameResult serviceResult = chessService.newGame(serviceRequest);

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