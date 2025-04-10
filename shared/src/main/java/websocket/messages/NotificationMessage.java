package websocket.messages;

/**
 * Represents a notification message sent from the server.
 */
public class NotificationMessage extends ServerMessage {
  private final String message;

  /**
   * Constructs a NotificationMessage instance.
   *
   * @param message the notification message text.
   */
  public NotificationMessage(String message) {
    super(ServerMessageType.NOTIFICATION);
    this.message = message;
  }

  /**
   * Retrieves the notification message.
   *
   * @return the notification message text.
   */
  public String getMessage() {
    return message;
  }
}