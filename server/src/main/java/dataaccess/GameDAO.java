package dataaccess;

import model.GameData;
import java.util.Collection;

// Auto generated comments

/**
 * Interface for Game data access operations
 */
public interface GameDAO {
    /**
     * Creates a new game
     * 
     * @param gameName the name of the game
     * @return the ID of the newly created game
     * @throws DataAccessException if there is an error creating the game
     */
    int createGame(String gameName) throws DataAccessException;

    /**
     * Gets a game by ID
     * 
     * @param gameID the game ID to look up
     * @return the game data
     * @throws DataAccessException if there is an error retrieving the game
     */
    GameData getGame(int gameID) throws DataAccessException;

    /**
     * Lists all games
     * 
     * @return a list of all games
     * @throws DataAccessException if there is an error listing the games
     */
    Collection<GameData> listGames() throws DataAccessException;

    /**
     * Updates a game's information
     * 
     * @param gameID the ID of the game to update
     * @param game   the updated game data
     * @throws DataAccessException if there is an error updating the game
     */
    void updateGame(int gameID, GameData game) throws DataAccessException;

    /**
     * Clears all games
     * 
     * @throws DataAccessException if there is an error clearing the games
     */
    void clear() throws DataAccessException;
}