package dataaccess;

import dataaccess.implementations.MemoryAuthDAO;
import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class MemoryAuthDAOTest {
    private MemoryAuthDAO authDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        authDAO = new MemoryAuthDAO();
        authDAO.clear();
    }

    @Test
    void testClear() throws DataAccessException {
        String authToken = authDAO.createAuth("testUser");
        assertNotNull(authDAO.getAuth(authToken));

        authDAO.clear();

        assertNull(authDAO.getAuth(authToken));
    }

    @Test
    void testClearEmpty() throws DataAccessException {
        authDAO.clear(); // Already cleared in setup
        assertDoesNotThrow(() -> authDAO.clear());
    }

    @Test
    void testCreateAuth() throws DataAccessException {
        String username = "testUser";
        String authToken = authDAO.createAuth(username);

        assertNotNull(authToken);
        AuthData authData = authDAO.getAuth(authToken);
        assertEquals(username, authData.username());
    }

    @Test
    void testCreateAuthNull() {
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(null));
    }

    @Test
    void testDeleteAuth() throws DataAccessException {
        String authToken = authDAO.createAuth("testUser");
        AuthData authData = authDAO.getAuth(authToken);
        assertNotNull(authData);

        authDAO.deleteAuth(authToken);

        assertNull(authDAO.getAuth(authToken));
    }

    @Test
    void testDeleteAuthInvalid() throws DataAccessException {
        assertDoesNotThrow(() -> authDAO.deleteAuth("invalidToken"));
    }

    @Test
    void testGetAuth() throws DataAccessException {
        String username = "testUser";
        String authToken = authDAO.createAuth(username);

        AuthData authData = authDAO.getAuth(authToken);

        assertNotNull(authData);
        assertEquals(username, authData.username());
        assertEquals(authToken, authData.authToken());
    }

    @Test
    void testGetAuthInvalid() throws DataAccessException {
        AuthData authData = authDAO.getAuth("invalidToken");
        assertNull(authData);
    }
}