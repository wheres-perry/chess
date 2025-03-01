package handlers;

import spark.Request;
import spark.Response;
import chess.ChessGame.TeamColor;
import requests.JoinRequest;
import results.JoinResult;
import services.ChessService;

public class JoinHandler extends AbstractHandler {
    private final ChessService chessService;
    
    public JoinHandler(ChessService chessService) {
        this.chessService = chessService;
    }    

    @Override
    public Object handle(Request req, Response res) {
        try {
            JoinRequest serviceRequest = deserialize(req, JoinRequest.class);
            JoinResult serviceResult = chessService.joinGame(serviceRequest);
            
            
            return success(res, 200, serviceResult);
        } catch (Exception e) {
            // TODO: Implement correct error handling
            return error(res, 500, "Error: " + e.getMessage());
        }
    }
}