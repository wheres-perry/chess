package handlers;

import spark.Request;
import spark.Response;

public class NewGameHandler extends AbstractHandler {

    @Override
    public Object handle(Request req, Response res) {
        try {
            CreateGameRequest serviceRequest = deserialize(req, CreateGameRequest.class);
            // TODO: Implement game creation logic
            

            CreateGameResult serviceResult = new CreateGameResult("1234");
            return success(res, 200, serviceResult);
            
        } catch (Exception e) {
            // TODO: Implement correct error handling
            return error(res, 500, "Error: " + e.getMessage());
        }
    }
    
    private static class CreateGameRequest {
        private String authToken;
        private String gameName;
    }
    
    private static class CreateGameResult {
        private final String gameID;
        public CreateGameResult(String gameID) {
            this.gameID = gameID;
        }
    }
}