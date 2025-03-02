package handlers;

import requests.RegisterRequest;
import results.RegisterResult;
import service.ChessService;
import spark.Request;
import spark.Response;

public class RegisterHandler extends AbstractHandler {


    private final ChessService chessService;
    
    public RegisterHandler(ChessService chessService) {
        this.chessService = chessService;
    }    

    @Override
    public Object handle(Request req, Response res) {
        try {
            RegisterRequest serviceRequest = deserialize(req, RegisterRequest.class);
            RegisterResult serviceResult = chessService.register(serviceRequest);


            return success(res, 200, serviceResult);

        } catch (Exception e) {
            return error(res, 500, "Error: " + e.getMessage());
        }
    }
}