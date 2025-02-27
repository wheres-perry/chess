package handlers;

import spark.Request;
import spark.Response;
import chess.ChessGame;

public class JoinHandler extends AbstractHandler {

    @Override
    public Object handle(Request req, Response res) {
        try {
            JoinGameRequest serviceRequest = deserialize(req, JoinGameRequest.class);
            // TODO: Implement join game logic
            JoinGameResult serviceResult = new JoinGameResult(ChessGame.TeamColor.BLACK, serviceRequest.gameID);
            return success(res, 200, serviceResult);

        } catch (Exception e) {
            // TODO: Implement correct error handling
            return error(res, 500, "Error: " + e.getMessage());
        }
    }

    private static class JoinGameRequest {
        private String gameID; // 
        private String authToken; // TODO: convert to auth token type
    }

    private static class JoinGameResult {
        private final String gameID;
        private final ChessGame.TeamColor playerColor;
        
        public JoinGameResult(ChessGame.TeamColor col, String id) {
            this.gameID = id;
            this.playerColor = col;
        }
    }
}