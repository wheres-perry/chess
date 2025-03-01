package handlers;

import services.ChessService;
import spark.Request;
import spark.Response;

public class ClearHandler extends AbstractHandler {
    private final ChessService chessService;
    
    public ClearHandler(ChessService chessService) {
        this.chessService = chessService;
    }    

    @Override
    public Object handle(Request req, Response res) {
        try {
            chessService.clear();
            return success(res, 200);
        } catch (Exception e) {
            return error(res, 500, "Error: " + e.getMessage());
        }
    }
}