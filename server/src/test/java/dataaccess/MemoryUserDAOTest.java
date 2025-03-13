package dataaccess;

import dataaccess.implementations.MemoryUserDAO;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class MemoryUserDAOTest {
    private MemoryUserDAO userDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        userDAO = new MemoryUserDAO();
        userDAO.clear();
    }

    @Test
    void testClear() throws DataAccessException {
        UserData user = new UserData("testUser", "password", "test@example.com");
        userDAO.createUser(user);
        assertNotNull(userDAO.getUser("testUser"));

        userDAO.clear();

        assertNull(userDAO.getUser("testUser"));
    }

    @Test
    void testClearEmpty() throws DataAccessException {
        userDAO.clear(); // Already cleared in setup
        assertDoesNotThrow(() -> userDAO.clear());
    }

    @Test
    void testCreateUser() throws DataAccessException {
        UserData user = new UserData("testUser", "password", "test@example.com");
        userDAO.createUser(user);

        UserData retrievedUser = userDAO.getUser("testUser");
        assertNotNull(retrievedUser);
        assertEquals("testUser", retrievedUser.username());
        assertEquals("password", retrievedUser.password());
        assertEquals("test@example.com", retrievedUser.email());
    }

    @Test
    void testCreateUserNull() {
        assertThrows(DataAccessException.class, () -> userDAO.createUser(null));
    }

    @Test
    void testGetUser() throws DataAccessException {
        UserData user = new UserData("testUser", "password", "test@example.com");
        userDAO.createUser(user);

        UserData retrievedUser = userDAO.getUser("testUser");

        assertNotNull(retrievedUser);
        assertEquals("testUser", retrievedUser.username());
    }

    @Test
    void testGetUserInvalid() throws DataAccessException {
        UserData user = userDAO.getUser("nonExistentUser");
        assertNull(user);
    }

    @Test
    void testHashPassword() {
        String password = "mySecurePassword";
        String hash = userDAO.hashPassword(password);

        assertNotNull(hash);
        assertNotEquals(password, hash);
        assertTrue(userDAO.checkPassword(password, hash));
    }

    @Test
    void testHashPasswordEmpty() {
        String hash = userDAO.hashPassword("");
        assertNotNull(hash);
    }

    @Test
    void testCheckPassword() {
        String password = "correctPassword";
        String hash = userDAO.hashPassword(password);

        assertTrue(userDAO.checkPassword(password, hash));
    }

    @Test
    void testCheckPasswordIncorrect() {
        String password = "correctPassword";
        String hash = userDAO.hashPassword(password);

        assertFalse(userDAO.checkPassword("wrongPassword", hash));
    }

    @Test
    void testVerifyPassword() throws DataAccessException {
        String username = "testUser";
        String password = "correctPassword";
        String hash = userDAO.hashPassword(password);
        UserData user = new UserData(username, hash, "test@example.com");
        userDAO.createUser(user);

        assertTrue(userDAO.verifyPassword(username, password));
    }

    @Test
    void testVerifyPasswordIncorrect() throws DataAccessException {
        String username = "testUser";
        String password = "correctPassword";
        String hash = userDAO.hashPassword(password);
        UserData user = new UserData(username, hash, "test@example.com");
        userDAO.createUser(user);

        assertFalse(userDAO.verifyPassword(username, "wrongPassword"));
    }
}