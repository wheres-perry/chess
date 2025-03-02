package handlers;

import requests.NewGameRequest;
import results.NewGameResult;
import service.ChessService;
import spark.Request;
import spark.Response;

public class NewGameHandler extends AbstractHandler {

    private final ChessService chessService;
    
    public NewGameHandler(ChessService chessService) {
        this.chessService = chessService;
    }    

    @Override
    public Object handle(Request req, Response res) {
        try {
            NewGameRequest serviceRequest = deserialize(req, NewGameRequest.class);
            NewGameResult serviceResult = chessService.newGame(serviceRequest);

            return success(res, 200, serviceResult);
            
        } catch (Exception e) {
            // TODO: Implement correct error handling
            return error(res, 500, "Error: " + e.getMessage());
        }
    }
}