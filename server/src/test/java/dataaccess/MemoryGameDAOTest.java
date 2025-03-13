package dataaccess;

import chess.ChessGame;
import dataaccess.implementations.MemoryGameDAO;
import model.GameData;
import org.junit.jupiter.api.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class MemoryGameDAOTest {
    private MemoryGameDAO gameDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        gameDAO = new MemoryGameDAO();
        gameDAO.clear();
    }

    @Test
    void testClear() throws DataAccessException {
        gameDAO.createGame("Test Game");
        gameDAO.clear();

        Collection<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    void testClearEmpty() throws DataAccessException {
        gameDAO.clear(); // Already cleared in setup
        assertDoesNotThrow(() -> gameDAO.clear());
    }

    @Test
    void testCreateGame() throws DataAccessException {
        String gameName = "Test Game";
        int gameID = gameDAO.createGame(gameName);

        GameData game = gameDAO.getGame(gameID);
        assertNotNull(game);
        assertEquals(gameName, game.gameName());
    }

    @Test
    void testCreateGameNull() {
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(null));
    }

    @Test
    void testGetGame() throws DataAccessException {
        String gameName = "Chess Match";
        int gameID = gameDAO.createGame(gameName);

        GameData game = gameDAO.getGame(gameID);

        assertNotNull(game);
        assertEquals(gameID, game.gameID());
        assertEquals(gameName, game.gameName());
    }

    @Test
    void testGetGameInvalid() throws DataAccessException {
        GameData game = gameDAO.getGame(8008135);
        assertNull(game);
    }

    @Test
    void testListGames() throws DataAccessException {
        gameDAO.createGame("Code Quality Check Doesn't Like Duplicate Names 1");
        gameDAO.createGame("Code Quality Check Doesn't Like Duplicate Names 2");
        gameDAO.createGame("Code Quality Check Doesn't Like Duplicate Names 3");

        Collection<GameData> allGames = gameDAO.listGames();

        assertEquals(3, allGames.size());
    }

    @Test
    void testListGamesEmpty() throws DataAccessException {
        Collection<GameData> allGames = gameDAO.listGames();
        assertTrue(allGames.isEmpty());
    }

    @Test
    void testUpdateGame() throws DataAccessException {
        int gameID = gameDAO.createGame("Original Game Name");
        GameData originalGame = gameDAO.getGame(gameID);

        GameData updatedGame = new GameData(
                gameID,
                "whitePlayer",
                "blackPlayer",
                "Updated Name",
                originalGame.game());

        gameDAO.updateGame(gameID, updatedGame);
        GameData retrievedGame = gameDAO.getGame(gameID);

        assertEquals("Updated Name", retrievedGame.gameName());
        assertEquals("whitePlayer", retrievedGame.whiteUsername());
        assertEquals("blackPlayer", retrievedGame.blackUsername());
    }

    @Test
    void testUpdateGameInvalid() {
        ChessGame chessGame = new ChessGame();
        GameData game = new GameData(42069, null, null, "Gukesh vs Ding", chessGame);

        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(999, game));
    }
}