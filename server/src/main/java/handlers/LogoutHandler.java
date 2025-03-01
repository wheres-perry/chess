package handlers;
import services.ChessService;
import requests.LogoutRequest;
import results.LogoutResult;
import spark.Request;
import spark.Response;

public class LogoutHandler extends AbstractHandler {

    private final ChessService chessService;
    
    public LogoutHandler(ChessService chessService) {
        this.chessService = chessService;
    }    

    @Override
    public Object handle(Request req, Response res) {
        try {
            LogoutRequest serviceRequest = deserialize(req, LogoutRequest.class);
            LogoutResult serviceResult = chessService.logout(serviceRequest);

            return success(res, 200);

        } catch (Exception e) {
            // TODO: Implement correct error handling
            return error(res, 500, "Error: " + e.getMessage());
        }
    }
}