package dataaccess;

import model.UserData;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of UserDAO
 */
public class UserDAOMemory implements UserDAO {
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hashPassword'");
    }

    @Override
    public boolean checkPassword(String password, String hash) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkPassword'");
    }

    @Override
    public boolean verifyPassword(String username, String password) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'verifyPassword'");
    }
}