package model;

import chess.ChessGame;

/**
 * Represents all data associated with a chess game
 */
public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
}