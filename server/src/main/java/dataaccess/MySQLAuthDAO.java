package dataaccess;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Properties;

public class MySQLAuthDAO implements AuthDAO {

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
