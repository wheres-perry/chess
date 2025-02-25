package handlers;

import spark.Request;
import spark.Response;

public interface Handler {
    Object handle(Request req, Response res);
}