package handlers;

import spark.Request;
import spark.Response;

public class ClearHandler extends AbstractHandler {

    @Override
    public Object handle(Request req, Response res) {
        try {
            // TODO: put service code here
            return success(res, 200);
        } catch (Exception e) {
            return error(res, 500, "Error: " + e.getMessage());
        }
    }
    // No request class needed, it just needs to call the function from the service
}