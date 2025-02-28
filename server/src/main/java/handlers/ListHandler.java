package handlers;

import spark.Request;
import spark.Response;
import java.util.ArrayList;
import java.util.List;
import model.GameData;
import requests.ListRequest;
import results.ListResult;

public class ListHandler extends AbstractHandler {

    @Override
    public Object handle(Request req, Response res) {
        try {
            ListRequest serviceRequest = deserialize(req, ListRequest.class);

            // TODO: Implement game listing logic
            List<GameData> gameList = new ArrayList<>();
            // Dummy function
            gameList.add(new GameData(1234, "white_user", "black_user", "Chess Match"));

            ListResult serviceResult = new ListResult(gameList);
            return success(res, 200, serviceResult);

        } catch (Exception e) {
            // TODO: Add correct error handling
            return error(res, 500, "Error");
        }
    }
}