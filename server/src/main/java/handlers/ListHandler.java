package handlers;

import spark.Request;
import spark.Response;
import java.util.ArrayList;
import java.util.List;
import model.GameData;
import requests.ListRequest;
import results.ListResult;
import services.ChessService;

public class ListHandler extends AbstractHandler {
    private final ChessService chessService;
    
    public ListHandler(ChessService chessService) {
        this.chessService = chessService;
    }
    
    @Override
    public Object handle(Request req, Response res) {
        try {
            ListRequest serviceRequest = deserialize(req, ListRequest.class);
            ListResult serviceResult = chessService.listAll(serviceRequest);
            
            
            return success(res, 200, serviceResult);
        } catch (Exception e) {
            // TODO: Add correct error handling
            return error(res, 500, "Error");
        }
    }
}