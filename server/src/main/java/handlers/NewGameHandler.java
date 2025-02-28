package handlers;

import requests.NewGameRequest;
import results.NewGameResult;
import spark.Request;
import spark.Response;

public class NewGameHandler extends AbstractHandler {

    @Override
    public Object handle(Request req, Response res) {
        try {
            NewGameRequest serviceRequest = deserialize(req, NewGameRequest.class);
            // TODO: Implement game creation logic
            

            NewGameResult serviceResult = new NewGameResult("1234");
            return success(res, 200, serviceResult);
            
        } catch (Exception e) {
            // TODO: Implement correct error handling
            return error(res, 500, "Error: " + e.getMessage());
        }
    }
}