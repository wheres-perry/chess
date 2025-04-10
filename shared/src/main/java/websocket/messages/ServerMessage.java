package websocket.messages;

import java.util.Objects;

/**
 * Represents a message that the server can send through a WebSocket.
 * <p>
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 * </p>
 */
public class ServerMessage {
    ServerMessageType serverMessageType;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    /**
     * Constructs a ServerMessage with the specified message type.
     *
     * @param type the type of the server message.
     */
    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    /**
     * Retrieves the type of the server message.
     *
     * @return the ServerMessageType of this message.
     */
    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage)) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    /**
     * Returns a hash code value for this server message.
     *
     * @return a hash code value for this server message.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}