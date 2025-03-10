package dataaccess.implementations;

import dataaccess.interfaces.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

public class MySQLAuthDAO implements AuthDAO {

    public MySQLAuthDAO() throws DataAccessException {
    }

    @Override
    public void clear() throws DataAccessException {
    }

    @Override
    public String createAuth(String username) throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
