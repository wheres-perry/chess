package dataaccess;

import model.GameData;
import chess.ChessGame;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of GameDAO
 */
public class GameDAOMemory implements GameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextID = 1;

    @Override
    public void clear() throws DataAccessException {
        games.clear();
        nextID = 1;
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        int gameID = nextID++;
        games.put(gameID, new GameData(gameID, null, null, gameName, new ChessGame()));
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return games.values();
    }

    @Override
    public void updateGame(int gameID, GameData data) throws DataAccessException {
        GameData game = games.get(gameID);
        String whiteUsername = game.whiteUsername();
        String blackUsername = game.blackUsername();
        String gameName = game.gameName();
        ChessGame chessGame = game.game();
        if (game != null) {
            games.put(gameID, new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame));
        }
    }
}