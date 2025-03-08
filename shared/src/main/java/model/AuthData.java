package model;

/**
 * Encapsulates security credentials for user session management.
 * Maintains the binding between a unique authentication token
 * and its corresponding user identity.
 * 
 * @param authToken Unique session identifier for verifying authorized access
 * @param username  User identity associated with the authentication token
 */
public record AuthData(String authToken, String username) {

}