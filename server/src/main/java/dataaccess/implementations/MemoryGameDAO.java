package dataaccess.implementations;

import model.GameData;
import dataaccess.DataAccessException;
import dataaccess.interfaces.GameDAO;
import chess.ChessGame;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of GameDAO
 */
public class MemoryGameDAO implements GameDAO {
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
    public void updateGame(int gameID, GameData game) throws DataAccessException {
        games.put(gameID, game);
    }
}