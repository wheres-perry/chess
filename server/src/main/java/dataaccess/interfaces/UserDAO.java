package dataaccess.interfaces;

import dataaccess.DataAccessException;

import model.UserData;

/**
 * Interface for User data access operations
 */
public interface UserDAO {
    /**
     * Creates a new user in the database
     * 
     * @param user the user to create
     * @throws DataAccessException if there is an error creating the user
     */
    void createUser(UserData user) throws DataAccessException;

    /**
     * Gets a user from the database by username
     * 
     * @param username the username to look up
     * @return the user data
     * @throws DataAccessException if there is an error retrieving the user
     */
    UserData getUser(String username) throws DataAccessException;

    /**
     * Clears all users from the database
     * 
     * @throws DataAccessException if there is an error clearing users
     */
    void clear() throws DataAccessException;

    /**
     * Hashes a password with a generated salt
     * 
     * @param password the password to hash
     * @return the hash string
     */
    String hashPassword(String password);

    /**
     * Verifies a password against a hash string
     * 
     * @param password  the password to verify
     * @param hash the hashstring to verify against
     * @return true if the password matches, false otherwise
     */
    boolean checkPassword(String password, String hash);

    /**
     * Verifies a password against a user's stored credentials
     * 
     * @param username the username to look up
     * @param password the password to verify
     * @return true if the password matches, false otherwise
     * @throws DataAccessException if there is an error retrieving the user
     */
    boolean verifyPassword(String username, String password) throws DataAccessException;
}