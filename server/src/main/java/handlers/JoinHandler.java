package handlers;

import spark.Request;
import spark.Response;
import chess.ChessGame.TeamColor;
import requests.JoinRequest;
import results.JoinResult;

public class JoinHandler extends AbstractHandler {

    @Override
    public Object handle(Request req, Response res) {
        try {
            JoinRequest serviceRequest = deserialize(req, JoinRequest.class);
            // TODO: Implement join game logic
            JoinResult serviceResult = new JoinResult(TeamColor.BLACK, serviceRequest.getGameID());
            return success(res, 200, serviceResult);

        } catch (Exception e) {
            // TODO: Implement correct error handling
            return error(res, 500, "Error: " + e.getMessage());
        }
    }
}