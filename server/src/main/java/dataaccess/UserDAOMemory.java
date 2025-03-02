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
}