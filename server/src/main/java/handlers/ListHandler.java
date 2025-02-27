package handlers;

import spark.Request;
import spark.Response;
import java.util.ArrayList;
import java.util.List;
import model.GameData;

public class ListHandler extends AbstractHandler {

    @Override
    public Object handle(Request req, Response res) {
        try {
            ListGamesRequest serviceRequest = deserialize(req, ListGamesRequest.class);

            // TODO: Implement game listing logic
            List<GameData> gameList = new ArrayList<>();
            // Dummy function
            gameList.add(new GameData(1234, "white_user", "black_user", "Chess Match"));

            ListGamesResult serviceResult = new ListGamesResult(gameList);
            return success(res, 200, serviceResult);

        } catch (Exception e) {
            // TODO: Add correct error handling
            return error(res, 500, "Error");
        }
    }

    // No request class needed for GET
    private static class ListGamesRequest {
        private String authToken; // TODOL convert to auth token type
    }

    private static class ListGamesResult {
        private final List<GameData> games;

        public ListGamesResult(List<GameData> games) {
            this.games = games;
        }
    }
}