package websocket.messages;

public class ErrorMessage extends ServerMessage {
  private final String errorMessage;

  public ErrorMessage(String errorMessage) {
    super(ServerMessageType.ERROR);
    // Ensure the message contains "Error" as per instructions [cite: 2342]
    this.errorMessage = errorMessage.toLowerCase().contains("error") ? errorMessage : "Error: " + errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}