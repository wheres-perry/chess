package results;

import chess.ChessGame.TeamColor;;

public class JoinResult {
    private final String gameID;
    private final TeamColor playerColor;
    
    public JoinResult(TeamColor col, String id) {
        this.gameID = id;
        this.playerColor = col;
    }

    public String getGameID() {
        return gameID;
    }

    public TeamColor getPlayerColor() {
        return playerColor;
    }
}