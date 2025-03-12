package dataaccess;

import dataaccess.implementations.MySQLAuthDAO;
import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class MySQLAuthDAOTest {
    private MySQLAuthDAO authDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        authDAO = new MySQLAuthDAO();
        authDAO.clear(); // Start with clean state
    }

    @AfterEach
    void tearDown() throws Exception {
        if (authDAO != null) {
            authDAO.close();
        }
    }

    @Test
    void testCreateAuthValid() throws DataAccessException {
        String username = "chessGod69";
        String authToken = authDAO.createAuth(username);

        assertNotNull(authToken);
        assertFalse(authToken.isEmpty());

        AuthData authData = authDAO.getAuth(authToken);
        assertEquals(username, authData.username());
    }

    @Test
    void testCreateAuthNull() {
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(null));
    }

    @Test
    void testCreateAuthEmpty() {
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(""));
    }

    @Test
    void testGetAuthValid() throws DataAccessException {
        String username = "chessGod69";
        String authToken = authDAO.createAuth(username);

        AuthData data = authDAO.getAuth(authToken);

        assertNotNull(data);
        assertEquals(authToken, data.authToken());
        assertEquals(username, data.username());
    }

    @Test
    void testGetAuthInvalid() throws DataAccessException {
        AuthData data = authDAO.getAuth("nonexistentToken");
        assertNull(data);
    }

    @Test
    void testDeleteAuth() throws DataAccessException {
        String username = "chessGod69";
        String authToken = authDAO.createAuth(username);

        assertNotNull(authDAO.getAuth(authToken));

        authDAO.deleteAuth(authToken);

        assertNull(authDAO.getAuth(authToken));
    }

    @Test
    void testDeleteAuthNull() {
        assertThrows(DataAccessException.class, () -> authDAO.deleteAuth(null));
    }

    @Test
    void testClear() throws DataAccessException {
        authDAO.createAuth("Trump");
        authDAO.createAuth("Biden");

        authDAO.clear();

        String authToken = authDAO.createAuth("Elon Musk");
        authDAO.deleteAuth(authToken);
    }

    @Test
    void testClearEmpty() throws DataAccessException {
        authDAO.clear(); // Already cleared in setup

        assertDoesNotThrow(() -> authDAO.clear());
    }

    @Test
    void testClose() throws Exception {
        assertDoesNotThrow(() -> authDAO.close());
    }

    @Test
    void testCloseMultiple() throws Exception {
        authDAO.close();
        assertDoesNotThrow(() -> authDAO.close());
    }
}