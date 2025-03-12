package dataaccess.implementations;

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
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public String hashPassword(String password) {
        throw new UnsupportedOperationException("Unimplemented method 'hashPassword'");
    }

    @Override
    public boolean checkPassword(String password, String hash) {
        throw new UnsupportedOperationException("Unimplemented method 'checkPassword'");
    }

    @Override
    public boolean verifyPassword(String username, String password) throws DataAccessException {
        throw new UnsupportedOperationException("Unimplemented method 'verifyPassword'");
    }
}