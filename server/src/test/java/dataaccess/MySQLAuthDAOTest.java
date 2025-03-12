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

    @AfterEach // Cleans up after each test
    void tearDown() throws Exception {
        if (authDAO != null) {
            authDAO.close();
        }
    }

    @Test
    void createAuth_validUsername_returnsAuthToken() throws DataAccessException {
        String username = "chessGod69";
        String authToken = authDAO.createAuth(username);

        assertNotNull(authToken);
        assertFalse(authToken.isEmpty());

        AuthData authData = authDAO.getAuth(authToken);
        assertEquals(username, authData.username());
    }

    @Test
    void createAuth_nullUsername_throwsException() {
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(null));
    }

    @Test
    void createAuth_emptyUsername_throwsException() {
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(""));
    }

    @Test
    void getAuth_validToken_returnsCorrectData() throws DataAccessException {
        String username = "chessGod69";
        String authToken = authDAO.createAuth(username);

        AuthData data = authDAO.getAuth(authToken);

        assertNotNull(data);
        assertEquals(authToken, data.authToken());
        assertEquals(username, data.username());
    }

    @Test
    void getAuth_invalidToken_returnsNull() throws DataAccessException {
        AuthData data = authDAO.getAuth("nonexistentToken");
        assertNull(data);
    }

    @Test
    void deleteAuth_validToken_removesToken() throws DataAccessException {
        String username = "chessGod69";
        String authToken = authDAO.createAuth(username);

        assertNotNull(authDAO.getAuth(authToken));

        authDAO.deleteAuth(authToken);

        assertNull(authDAO.getAuth(authToken));
    }

    @Test
    void deleteAuth_nullToken_throwsException() {
        assertThrows(DataAccessException.class, () -> authDAO.deleteAuth(null));
    }

    @Test
    void clear_withExistingData_removesAllData() throws DataAccessException {
        authDAO.createAuth("Trump");
        authDAO.createAuth("Biden");

        authDAO.clear();

        String authToken = authDAO.createAuth("Elon Musk");
        authDAO.deleteAuth(authToken);
    }

    @Test
    void clear_emptyDatabase_completesSuccessfully() throws DataAccessException {
        authDAO.clear(); // Already cleared in setup

        assertDoesNotThrow(() -> authDAO.clear());
    }

    @Test
    void close_validConnection_closesSuccessfully() throws Exception {
        assertDoesNotThrow(() -> authDAO.close());
    }

    @Test
    void close_multipleCalls_doesNotThrowException() throws Exception {
        authDAO.close();
        assertDoesNotThrow(() -> authDAO.close());
    }
}