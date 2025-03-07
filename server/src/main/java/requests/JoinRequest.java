package requests;

import chess.ChessGame.TeamColor;

public record JoinRequest(int gameID, TeamColor playerColor, String authToken) {
}