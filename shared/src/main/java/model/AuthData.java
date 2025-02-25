package model;

/**
 * Represents authentication data with an auth token and associated username
 */
public record AuthData(String authToken, String username) {

}