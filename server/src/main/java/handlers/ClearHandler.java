package handlers;

import service.ChessService;
import spark.Request;
import spark.Response;

/**
 * Specialized handler for database reset operations.
 * Manages the complete purging of all application data structures,
 * enabling system restoration to its initial state.
 */
public class ClearHandler extends AbstractHandler {
    private final ChessService chessService;

    /**
     * Establishes a new clearing mechanism with chess service connectivity.
     * 
     * @param chessService The service component responsible for data elimination
     */
    public ClearHandler(ChessService chessService) {
        this.chessService = chessService;
    }

    /**
     * Executes comprehensive data erasure across all application repositories.
     * Processes system-wide reset requests without requiring authentication,
     * restoring the environment to its default configuration.
     * 
     * @param req The incoming reset request (authentication not required)
     * @param res The outgoing response indicating operation outcome
     * @return Empty success confirmation or descriptive error details
     */
    @Override
    public Object handle(Request req, Response res) {
        try {
            chessService.clear();
            return success(res, 200);
        } catch (Exception e) {
            return error(res, 500, "Error: " + e.getMessage());
        }
    }
}