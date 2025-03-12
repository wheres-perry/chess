package dataaccess.implementations;

import dataaccess.DataAccessException;
import dataaccess.interfaces.GameDAO;

import java.util.Collection;
import model.GameData;

/**
 * MySQL implementation of the GameDAO interface.
 */
public class MySQLGameDAO implements GameDAO {

  /**
   * Creates the games table if it does not already exist.
   * 
   * @throws DataAccessException if there is an error creating the table
   */

  public MySQLGameDAO() throws DataAccessException {
  }

  /**
   * Creates a new game.
   * 
   * @param gameName the name of the game
   * @return the ID of the newly created game
   * @throws DataAccessException if there is an error creating the game
   */
  @Override
  public int createGame(String gameName) throws DataAccessException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Gets a game by ID.
   * 
   * @param gameID the game ID to look up
   * @return the game data
   * @throws DataAccessException if there is an error retrieving the game
   */
  @Override
  public GameData getGame(int gameID) throws DataAccessException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Lists all games.
   * 
   * @return a list of all games
   * @throws DataAccessException if there is an error listing the games
   */
  @Override
  public Collection<GameData> listGames() throws DataAccessException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Updates a game's information.
   * 
   * @param gameID the ID of the game to update
   * @param game   the updated game data
   * @throws DataAccessException if there is an error updating the game
   */
  @Override
  public void updateGame(int gameID, GameData game) throws DataAccessException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Clears all games.
   * 
   * @throws DataAccessException if there is an error clearing the games
   */
  @Override
  public void clear() throws DataAccessException {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}