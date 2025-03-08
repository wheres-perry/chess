package dataaccess;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Properties;

public class MySQLAuthDAO implements AuthDAO {

    private static final String DB_URL = "placeholder";
    private static final String DB_USER = "placeholder";
    private static final String DB_PASSWORD = "placeholder";

    public MySQLAuthDAO() throws DataAccessException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("database.properties")) {
            props.load(fis);
            String DB_URL = props.getProperty("db.username");
            String DB_USER = props.getProperty("db.username");
            String DB_PASSWORD = props.getProperty("db.password");
        } catch (IOException e) {
            // Uh OH!
        }
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
