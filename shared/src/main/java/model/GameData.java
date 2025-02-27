package model;

import chess.ChessGame;

/**
 * Represents all data associated with a chess game
 */
public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    // Additional constructor that doesn't require game
    public GameData(int gameID, String whiteUsername, String blackUsername, String gameName) {
        this(gameID, whiteUsername, blackUsername, gameName, null);
    }
}