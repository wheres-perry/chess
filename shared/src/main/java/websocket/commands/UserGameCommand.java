package websocket.commands;

import java.util.Objects;

/**
 * Represents a command a user can send to the server over a WebSocket.
 * <p>
 * This is a base class for different types of game commands.
 * </p>
 */
public class UserGameCommand {

    private final CommandType commandType;
    private final String authToken;
    private final Integer gameID;

    /**
     * Constructs a UserGameCommand instance.
     *
     * @param commandType the type of command.
     * @param authToken   the user's authentication token.
     * @param gameID      the game identifier.
     */
    public UserGameCommand(CommandType commandType, String authToken, Integer gameID) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
    }

    /**
     * Enumeration of user game command types.
     */
    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    /**
     * Retrieves the command type.
     *
     * @return the CommandType.
     */
    public CommandType getCommandType() {
        return commandType;
    }

    /**
     * Retrieves the authentication token.
     *
     * @return the authentication token string.
     */
    public String getAuthToken() {
        return authToken;
    }

    /**
     * Retrieves the game identifier.
     *
     * @return the game identifier.
     */
    public Integer getGameID() {
        return gameID;
    }

    /**
     * Determines whether this object is equal to another.
     *
     * @param o the other object to compare.
     * @return true if equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserGameCommand))
            return false;
        UserGameCommand that = (UserGameCommand) o;
        return commandType == that.commandType &&
                Objects.equals(authToken, that.authToken) &&
                Objects.equals(gameID, that.gameID);
    }

    /**
     * Returns the hash code for this command.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(commandType, authToken, gameID);
    }
}