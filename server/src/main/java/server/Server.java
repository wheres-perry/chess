package server;

import spark.*;
import handlers.*;
import service.ChessService;
import dataaccess.DataAccessException;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        ChessService chessService;
        try {
            chessService = new ChessService();
        } catch (DataAccessException e) {
            System.err.println("Failed to initialize Chess Service: " + e.getMessage());
            throw new RuntimeException("Server initialization failed due to data access error", e);
        }

        ClearHandler clearHandler = new ClearHandler(chessService);
        RegisterHandler registerHandler = new RegisterHandler(chessService);
        LoginHandler loginHandler = new LoginHandler(chessService);
        LogoutHandler logoutHandler = new LogoutHandler(chessService);
        ListHandler listGamesHandler = new ListHandler(chessService);
        NewGameHandler createGameHandler = new NewGameHandler(chessService);
        JoinHandler joinGameHandler = new JoinHandler(chessService);

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