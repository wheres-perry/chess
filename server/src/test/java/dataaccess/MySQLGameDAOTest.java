package dataaccess;

import dataaccess.implementations.MySQLGameDAO;
import model.GameData;
import chess.ChessGame;
import org.junit.jupiter.api.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class MySQLGameDAOTest {
    private MySQLGameDAO gameDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        gameDAO = new MySQLGameDAO();
        gameDAO.clear(); // Start with clean state
    }

    @AfterEach
    void tearDown() throws Exception {
        if (gameDAO != null) {
            gameDAO.close();
        }
    }

    @Test
    void testCreateGame() throws DataAccessException {
        String gameName = "Blitz Tournament";
        int gameID = gameDAO.createGame(gameName);

        assertTrue(gameID > 0);

        GameData game = gameDAO.getGame(gameID);
        assertEquals(gameName, game.gameName());
    }

    @Test
    void testCreateGameNull() {
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(null));
    }

    @Test
    void testCreateGameEmpty() {
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(""));
    }

    @Test
    void testGetGame() throws DataAccessException {
        String gameName = "World Classic Championship";
        int gameID = gameDAO.createGame(gameName);

        GameData game = gameDAO.getGame(gameID);

        assertNotNull(game);
        assertEquals(gameID, game.gameID());
        assertEquals(gameName, game.gameName());
        assertNotNull(game.game());
    }

    @Test
    void testGetGameInvalid() throws DataAccessException {
        GameData game = gameDAO.getGame(696969);
        assertNull(game);
    }

    @Test
    void testListGames() throws DataAccessException {
        gameDAO.createGame("Game 1");
        gameDAO.createGame("Game 2");
        gameDAO.createGame("Game 3");

        Collection<GameData> games = gameDAO.listGames();

        assertEquals(3, games.size());
    }

    @Test
    void testListGamesEmpty() throws DataAccessException {
        Collection<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    void testUpdateGame() throws DataAccessException {
        int gameID = gameDAO.createGame("Original Name");
        GameData originalGame = gameDAO.getGame(gameID);

        GameData updatedGame = new GameData(
                gameID,
                "Gukesh",
                "Bobby Fischer",
                "Updated Name",
                originalGame.game());

        gameDAO.updateGame(gameID, updatedGame);

        GameData retrievedGame = gameDAO.getGame(gameID);
        assertEquals("Gukesh", retrievedGame.whiteUsername());
        assertEquals("Bobby Fischer", retrievedGame.blackUsername());
        assertEquals("Updated Name", retrievedGame.gameName());
    }

    @Test
    void testUpdateGameInvalid() {
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(696969, new GameData(
                696969, "Magnus", "Hikaru", "Invalid Game", new ChessGame())));
    }

    @Test
    void testUpdateGameNull() {
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(1, null));
    }

    @Test
    void testClear() throws DataAccessException {
        gameDAO.createGame("Game 1");
        gameDAO.createGame("Game 2");

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
    void testClose() throws Exception {
        assertDoesNotThrow(() -> gameDAO.close());
    }

    @Test
    void testCloseMultiple() throws Exception {
        gameDAO.close();
        assertDoesNotThrow(() -> gameDAO.close());
    }
}