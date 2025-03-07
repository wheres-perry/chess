package results;

import chess.ChessGame.TeamColor;

public record JoinResult(TeamColor playerColor, String gameName) {
}