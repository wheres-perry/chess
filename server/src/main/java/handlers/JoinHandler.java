package handlers;

import spark.Request;
import spark.Response;
import chess.ChessGame.TeamColor;
import requests.JoinRequest;
import service.ChessService;
import com.google.gson.JsonObject;

/**
 * Facilitates player entry into existing chess matches.
 * Orchestrates the verification, position assignment, and team allocation
 * processes required for integrating users into ongoing games.
 */
public class JoinHandler extends AbstractHandler {
    private final ChessService chessService;

    /**
     * Initializes a handler for chess match participation with service
     * connectivity.
     * 
     * @param chessService The functional backend for game membership operations
     */
    public JoinHandler(ChessService chessService) {
        this.chessService = chessService;
    }

    /**
     * Evaluates and processes player enrollment requests for chess matches.
     * Authenticates users, validates game existence, verifies position
     * availability,
     * and completes the integration of players into their requested team positions.
     * 
     * @param req The inbound player participation request with authentication and
     *            preferences
     * @param res The outbound system response for status delivery
     * @return Success confirmation or descriptive error information
     */
    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            if (authToken == null || authToken.isEmpty()) {
                return error(res, 401, "Error: unauthorized");
            }

            JsonObject requestBody = gson.fromJson(req.body(), JsonObject.class);

            if (!requestBody.has("gameID")) {
                return error(res, 400, "Error: bad request");
            }
            int gameID;
            try {
                gameID = requestBody.get("gameID").getAsInt();
            } catch (Exception e) {
                return error(res, 400, "Error: bad request");
            }

            if (!requestBody.has("playerColor") || requestBody.get("playerColor").isJsonNull()) {
                return error(res, 400, "Error: bad request");
            }
            String colorStr = requestBody.get("playerColor").getAsString();
            if (colorStr == null || colorStr.isEmpty()) {
                return error(res, 400, "Error: bad request");
            }

            TeamColor playerColor;
            if ("WHITE".equalsIgnoreCase(colorStr)) {
                playerColor = TeamColor.WHITE;
            } else if ("BLACK".equalsIgnoreCase(colorStr)) {
                playerColor = TeamColor.BLACK;
            } else {
                return error(res, 400, "Error: bad request");
            }

            JoinRequest serviceRequest = new JoinRequest(gameID, playerColor, authToken);
            chessService.joinGame(serviceRequest);

            return success(res, 200);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null) {
                if (message.contains("unauthorized")) {
                    return error(res, 401, "Error: unauthorized");
                } else if (message.contains("bad request")) {
                    return error(res, 400, "Error: bad request");
                } else if (message.contains("already taken")) {
                    return error(res, 403, "Error: already taken");
                }
            }
            return error(res, 500, "Error: " + e.getMessage());
        }
    }
}