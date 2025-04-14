package server;

import spark.*;
import handlers.*;
import service.ChessService;
import dataaccess.DataAccessException;
import dataaccess.implementations.MySQLAuthDAO;
import dataaccess.implementations.MySQLGameDAO;
import dataaccess.implementations.MySQLUserDAO;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
import websocket.WebSocketHandler;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        try {
            AuthDAO authDAO = new MySQLAuthDAO();
            GameDAO gameDAO = new MySQLGameDAO();
            UserDAO userDAO = new MySQLUserDAO();

            ChessService chessService = new ChessService(userDAO, gameDAO, authDAO);

            WebSocketHandler webSocketHandler = new WebSocketHandler(authDAO, gameDAO);
            ClearHandler clearHandler = new ClearHandler(chessService);
            RegisterHandler registerHandler = new RegisterHandler(chessService);
            LoginHandler loginHandler = new LoginHandler(chessService);
            LogoutHandler logoutHandler = new LogoutHandler(chessService);
            ListHandler listGamesHandler = new ListHandler(chessService);
            NewGameHandler createGameHandler = new NewGameHandler(chessService);
            JoinHandler joinGameHandler = new JoinHandler(chessService);

            // Setup WebSocket endpoint
            Spark.webSocket("/ws", webSocketHandler);

            Spark.delete("/db", clearHandler::handle);
            Spark.post("/user", registerHandler::handle);
            Spark.post("/session", loginHandler::handle);
            Spark.delete("/session", logoutHandler::handle);
            Spark.get("/game", listGamesHandler::handle);
            Spark.post("/game", createGameHandler::handle);
            Spark.put("/game", joinGameHandler::handle);

            Spark.awaitInitialization();
            System.out.println("Server started on port " + Spark.port());
            return Spark.port();

            // I added stack traces to the catch blocks to help find issues
        } catch (DataAccessException e) {
            System.err.println("Failed to initialize DAOs or Chess Service: " + e.getMessage());
            e.printStackTrace();
            Spark.stop();
            System.exit(1);
            return -1;
        } catch (Throwable t) { // Catch all other exceptions
            System.err.println("Unexpected error during server startup: " + t.getMessage());
            t.printStackTrace();
            Spark.stop();
            System.exit(1);
            return -1;
        }
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
        System.out.println("Server stopped.");
    }
}