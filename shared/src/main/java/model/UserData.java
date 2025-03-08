package model;

/**
 * Captures essential user profile information for system authentication.
 * Stores account credentials and contact details in an immutable structure.
 * 
 * @param username Unique identifier for user account access
 * @param password Secret verification string for account authentication
 * @param email    Electronic communication address for account notifications
 */
public record UserData(String username, String password, String email) {
}