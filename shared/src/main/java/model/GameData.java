package model;

import chess.ChessGame;

/**
 * Consolidates all elements pertaining to a chess match instance.
 * Preserves game state, player assignments, and identification metadata
 * as an immutable snapshot of game conditions.
 * 
 * @param gameID        Unique numeric identifier for referencing the specific
 *                      match
 * @param whiteUsername Identity of player controlling white chess pieces
 * @param blackUsername Identity of player controlling black chess pieces
 * @param gameName      Descriptive title assigned to the chess match
 * @param game          Core chess engine instance containing board state and
 *                      game logic
 */
public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
}