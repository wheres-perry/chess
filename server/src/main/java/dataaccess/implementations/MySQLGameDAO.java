package dataaccess.implementations;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.DataAccessException;
import dataaccess.interfaces.GameDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import model.GameData;

public class MySQLGameDAO implements GameDAO, AutoCloseable {
  private final Connection connection;
  private final Gson gson = new GsonBuilder().serializeNulls().create();

  public MySQLGameDAO() throws DataAccessException {
    DatabaseManager.createDatabase();
    connection = DatabaseManager.getConnection();
    createGameTable();
  }

  private void createGameTable() throws DataAccessException {
    try {
      String createTableSQL = """
          CREATE TABLE IF NOT EXISTS games (
              game_id INT NOT NULL AUTO_INCREMENT,
              white_username VARCHAR(255),
              black_username VARCHAR(255),
              game_name VARCHAR(255) NOT NULL,
              game_state TEXT,
              PRIMARY KEY (game_id)
          )
          """;

      try (PreparedStatement stmt = connection.prepareStatement(createTableSQL)) {
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error creating game table: " + e.getMessage());
    }
  }

  @Override
  public int createGame(String gameName) throws DataAccessException {
    if (gameName == null || gameName.isEmpty()) {
      throw new DataAccessException("Game name cannot be empty");
    }

    try {
      String sql = "INSERT INTO games (game_name, game_state) VALUES (?, ?)";

      ChessGame game = new ChessGame();
      String gameState = gson.toJson(game);

      try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, gameName);
        stmt.setString(2, gameState);
        stmt.executeUpdate();

        try (ResultSet rs = stmt.getGeneratedKeys()) {
          if (rs.next()) {
            return rs.getInt(1);
          } else {
            throw new DataAccessException("Failed to get generated game ID");
          }
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error creating game: " + e.getMessage());
    }
  }

  @Override
  public GameData getGame(int gameID) throws DataAccessException {
    try {
      String sql = "SELECT game_id, white_username, black_username, game_name, game_state FROM games WHERE game_id = ?";

      try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, gameID);

        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            String gameStateJson = rs.getString("game_state");

            ChessGame game = gson.fromJson(gameStateJson, ChessGame.class);


            return new GameData(
                rs.getInt("game_id"),
                rs.getString("white_username"),
                rs.getString("black_username"),
                rs.getString("game_name"),
                game);
          }
          return null;
        }
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error retrieving game: " + e.getMessage());
    } catch (Exception e) {
      throw new DataAccessException("Unexpected error retrieving game: " + e.getMessage());
    }
  }

  @Override
  public Collection<GameData> listGames() throws DataAccessException {
    Collection<GameData> games = new ArrayList<>();

    try {
      String sql = "SELECT game_id, white_username, black_username, game_name, game_state FROM games";

      try (PreparedStatement stmt = connection.prepareStatement(sql);
          ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
          ChessGame game = gson.fromJson(rs.getString("game_state"), ChessGame.class);

          games.add(new GameData(
              rs.getInt("game_id"),
              rs.getString("white_username"),
              rs.getString("black_username"),
              rs.getString("game_name"),
              game));
        }
      }
      return games;
    } catch (SQLException e) {
      throw new DataAccessException("Error listing games: " + e.getMessage());
    }
  }

  @Override
  public void updateGame(int gameID, GameData gameDataInput) throws DataAccessException {
    if (gameDataInput == null) {
      throw new DataAccessException("Game data cannot be null");
    }
    ChessGame gameToSerialize = gameDataInput.game();
    if (gameToSerialize == null) {
      throw new DataAccessException("ChessGame object cannot be null");
    }


    try {
      String sql = "UPDATE games SET white_username = ?, black_username = ?, game_name = ?, game_state = ? WHERE game_id = ?";


      String gameState = gson.toJson(gameToSerialize);


      try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, gameDataInput.whiteUsername());
        stmt.setString(2, gameDataInput.blackUsername());
        stmt.setString(3, gameDataInput.gameName());
        stmt.setString(4, gameState);
        stmt.setInt(5, gameID);

        int rowsAffected = stmt.executeUpdate();
        if (rowsAffected == 0) {
          throw new DataAccessException("Game not found with ID: " + gameID);
        }

      }
    } catch (SQLException e) {
      throw new DataAccessException("Error updating game: " + e.getMessage());
    } catch (Exception e) {
      throw new DataAccessException("Unexpected error updating game: " + e.getMessage());
    }
  }

  @Override
  public void clear() throws DataAccessException {
    try {
      String sql = "DELETE FROM games";

      try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error clearing games: " + e.getMessage());
    }
  }

  @Override
  public void close() throws Exception {
    if (connection != null && !connection.isClosed()) {
      connection.close();
    }
  }
}