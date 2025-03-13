package dataaccess.implementations;

import org.mindrot.jbcrypt.BCrypt;
import model.UserData;
import java.util.HashMap;
import dataaccess.interfaces.UserDAO;
import java.util.Map;

import dataaccess.DataAccessException;

/**
 * In-memory implementation of UserDAO
 */
public class MemoryUserDAO implements UserDAO {
    private final Map<String, UserData> users = new HashMap<>();

    @Override
    public void clear() throws DataAccessException {
        users.clear();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user == null) {
            throw new DataAccessException("User cannot be null");
        }
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    @Override
    public boolean checkPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }

    @Override
    public boolean verifyPassword(String username, String password) throws DataAccessException {
        UserData user = getUser(username);
        if (user == null) {
            return false;
        }
        return checkPassword(password, user.password());
    }
}
