package server;

import spark.*;
import websocket.WebSocketHandler;
import dataaccess.implementations.MySQLAuthDAO;
import dataaccess.implementations.MySQLGameDAO;
import dataaccess.implementations.MySQLUserDAO;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
import handlers.*;
import service.*;
import dataaccess.DataAccessException;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // --- Initialize DAOs ---
        AuthDAO authDAO;
        GameDAO gameDAO;
        UserDAO userDAO;
        try {
            authDAO = new MySQLAuthDAO(); // Or MemoryAuthDAO
            gameDAO = new MySQLGameDAO(); // Or MemoryGameDAO
            userDAO = new MySQLUserDAO(); // Or MemoryUserDAO
        } catch (DataAccessException e) {
            System.err.println("Failed to initialize DAOs: " + e.getMessage());
            throw new RuntimeException("Server initialization failed due to data access error", e);
        }
        ChessService chessService = new ChessService(userDAO, gameDAO, authDAO);
        WebSocketService webSocketService = new WebSocketService(authDAO, gameDAO);

        WebSocketHandler webSocketHandler = new WebSocketHandler(webSocketService);

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
        Spark.webSocket("/ws", webSocketHandler);

        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}