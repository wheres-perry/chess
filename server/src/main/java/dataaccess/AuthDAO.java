package dataaccess;

import model.AuthData;

// Auto generated comments

/**
 * Interface for Authentication data access operations
 */
public interface AuthDAO {
    /**
     * Clears all authentication data
     * 
     * @throws DataAccessException if there is an error clearing the auth data
     */
    void clear() throws DataAccessException;

    /**
     * Creates a new authentication token for a user
     * 
     * @param username the username to create a token for
     * @return the newly created authentication data
     * @throws DataAccessException if there is an error creating the auth token
     */
    String createAuth(String username) throws DataAccessException;

    /**
     * Deletes an authentication token (logout)
     * 
     * @param authToken the token to delete
     * @throws DataAccessException if there is an error deleting the auth token
     */
    void deleteAuth(String authToken) throws DataAccessException;

    /**
     * Gets authentication data by token
     * 
     * @param authToken the token to look up
     * @return the authentication data
     * @throws DataAccessException if there is an error retrieving the auth data
     */
    AuthData getAuth(String authToken) throws DataAccessException;
}