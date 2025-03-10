package dataaccess;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.UserDAO;
import model.UserData;

/**
 * MySQL implementation of the UserDAO interface.
 */
public class MySQLUserDAO implements UserDAO {

    /**
     * Creates the users table if it does not already exist.
     *
     * @throws DataAccessException if there is an error creating the table
     */
    private void createTableIfNotExists() throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MySQLUserDAO() throws DataAccessException {
        DatabaseManager.createDatabase();
        createTableIfNotExists();
    }

    @Override
    public String hashPassword(String password) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean checkPassword(String password, String hash) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean verifyPassword(String username, String password) throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}