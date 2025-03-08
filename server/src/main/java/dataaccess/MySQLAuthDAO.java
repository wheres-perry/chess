package dataaccess;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

public class MySQLAuthDAO implements AuthDAO {

    private static final String DB_URL = "placeholder";
    private static final String DB_USER = "placeholder";
    private static final String DB_PASSWORD = "placeholder";

    public MySQLAuthDAO() throws DataAccessException {
    }

    @Override
    public void clear() throws DataAccessException {
    }

    @Override
    public String createAuth(String username) throws DataAccessException {
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
    }
}
