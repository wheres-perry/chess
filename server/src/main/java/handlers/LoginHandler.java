package handlers;

import requests.LoginRequest;
import results.LoginResult;
import service.ChessService;
import spark.Request;
import spark.Response;

public class LoginHandler extends AbstractHandler {

    private final ChessService chessService;
    
    public LoginHandler(ChessService chessService) {
        this.chessService = chessService;
    }    

    @Override
    public Object handle(Request req, Response res) {
        try {
            LoginRequest serviceRequest = deserialize(req, LoginRequest.class);
            LoginResult serviceResult = chessService.login(serviceRequest);
                    


            return success(res, 200, serviceResult);
        } catch (Exception e) {
            // TODO: Imeplement correct error handling
            return error(res, 500, "Error: " + e.getMessage());
        }
    }
}