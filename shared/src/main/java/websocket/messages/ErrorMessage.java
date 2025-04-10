package websocket.messages;

/**
 * Represents an error message that is sent from the server.
 * <p>
 * Ensures that the error message is prefixed with "Error:" if it doesn't
 * already contain the word "error".
 * </p>
 */
public class ErrorMessage extends ServerMessage {
  private final String errorMessage;

  /**
   * Constructs an ErrorMessage instance.
   *
   * @param errorMessage the error message text; if it does not contain "error",
   *                     "Error:" is prefixed.
   */
  public ErrorMessage(String errorMessage) {
    super(ServerMessageType.ERROR);
    this.errorMessage = errorMessage.toLowerCase().contains("error")
        ? errorMessage
        : "Error: " + errorMessage;
  }

  /**
   * Retrieves the error message.
   *
   * @return the formatted error message.
   */
  public String getErrorMessage() {
    return errorMessage;
  }
}