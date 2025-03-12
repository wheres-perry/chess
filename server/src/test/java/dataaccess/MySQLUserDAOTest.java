package dataaccess;

import dataaccess.implementations.MySQLUserDAO;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class MySQLUserDAOTest {
    private MySQLUserDAO userDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        userDAO = new MySQLUserDAO();
        userDAO.clear(); // Start with clean state
    }

    @AfterEach
    void tearDown() throws Exception {
        if (userDAO != null) {
            userDAO.close();
        }
    }

    @Test
    void testCreateUser() throws DataAccessException {
        String username = "BlunderMaster314";
        String password = "CS240IsEasy";
        String email = "bm@byu.edu";

        UserData user = new UserData(username, password, email);
        userDAO.createUser(user);

        UserData retrievedUser = userDAO.getUser(username);
        assertNotNull(retrievedUser);
        assertEquals(username, retrievedUser.username());
        assertEquals(email, retrievedUser.email());
    }

    @Test
    void testCreateUserNull() {
        assertThrows(DataAccessException.class, () -> userDAO.createUser(null));
    }

    @Test
    void testCreateUserIncomplete() {
        assertThrows(DataAccessException.class, () -> userDAO.createUser(new UserData(null, "password", "email")));
        assertThrows(DataAccessException.class, () -> userDAO.createUser(new UserData("username", null, "email")));
        assertThrows(DataAccessException.class, () -> userDAO.createUser(new UserData("username", "password", null)));
    }

    @Test
    void testGetUser() throws DataAccessException {
        String username = "RealMagnusCarlsen";
        String password = "Password is a bad password";
        String email = "master@chess.com";

        userDAO.createUser(new UserData(username, password, email));

        UserData user = userDAO.getUser(username);
        assertNotNull(user);
        assertEquals(username, user.username());
        assertEquals(email, user.email());
    }

    @Test
    void testGetUserInvalid() throws DataAccessException {
        UserData user = userDAO.getUser("nonexistentUser");
        assertNull(user);
    }

    @Test
    void testVerifyPassword() throws DataAccessException {
        String username = "Uh_im_running_out_of_ideas";
        String password = "password";
        String email = "email@example.com";

        userDAO.createUser(new UserData(username, password, email));

        assertTrue(userDAO.verifyPassword(username, password));
        assertFalse(userDAO.verifyPassword(username, "wrongPassword"));
    }

    @Test
    void testVerifyPasswordInvalidUser() throws DataAccessException {
        assertFalse(userDAO.verifyPassword("nonexistentUser", "anyPassword"));
    }

    @Test
    void testHashPassword() {
        String password = "plainTextPassword";
        String hash = userDAO.hashPassword(password);

        assertNotNull(hash);
        assertNotEquals(password, hash);
        assertTrue(userDAO.checkPassword(password, hash));
    }

    @Test
    void testCheckPassword() {
        String password = "myPassword123";
        String hash = userDAO.hashPassword(password);

        assertTrue(userDAO.checkPassword(password, hash));
        assertFalse(userDAO.checkPassword("wrongPassword", hash));
    }

    @Test
    void testClear() throws DataAccessException {
        userDAO.createUser(new UserData("user1", "pass1", "email1"));
        userDAO.createUser(new UserData("user2", "pass2", "email2"));

        userDAO.clear();

        assertNull(userDAO.getUser("user1"));
        assertNull(userDAO.getUser("user2"));
    }

    @Test
    void testClearEmpty() throws DataAccessException {
        userDAO.clear(); // Already cleared in setup
        assertDoesNotThrow(() -> userDAO.clear());
    }

    @Test
    void testClose() throws Exception {
        assertDoesNotThrow(() -> userDAO.close());
    }

    @Test
    void testCloseMultiple() throws Exception {
        userDAO.close();
        assertDoesNotThrow(() -> userDAO.close());
    }
}