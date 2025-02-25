package server;

import spark.*;
import handlers.*;


public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        ClearHandler clearHandler = new ClearHandler();
        RegisterHandler registerHandler = new RegisterHandler();
        LoginHandler loginHandler = new LoginHandler();
        LogoutHandler logoutHandler = new LogoutHandler();
        ListHandler listGamesHandler = new ListHandler();
        NewGameHandler createGameHandler = new NewGameHandler();
        JoinHandler joinGameHandler = new JoinHandler();

        Spark.delete("/db", (req, res) -> clearHandler.handle(req, res));
        Spark.post("/user", (req, res) -> registerHandler.handle(req, res));
        Spark.post("/session", (req, res) -> loginHandler.handle(req, res));
        Spark.delete("/session", (req, res) -> logoutHandler.handle(req, res));
        Spark.get("/game", (req, res) -> listGamesHandler.handle(req, res));
        Spark.post("/game", (req, res) -> createGameHandler.handle(req, res));
        Spark.put("/game", (req, res) -> joinGameHandler.handle(req, res));

        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}