package ui;

package client.repl;

import client.ChessClient;
import java.util.Scanner;

public class InGameRepl {
  private final ChessClient client;
  private final Scanner scanner;
  private boolean inGame;

  public InGameRepl(ChessClient client) {
    this.client = client;
    this.scanner = new Scanner(System.in);
    this.inGame = false;
  }

  public boolean run() {
    // TODO: Implement in-game REPL logic
    return false;
  }

  private String getInput() {
    System.out.print(client.getCurrentUser() + " (in game) >>> ");
    return scanner.nextLine().trim();
  }

  private void displayHelp() {
    // TODO: Implement help display for in-game state
  }

  public boolean isInGame() {
    return inGame;
  }

  public void setInGame(boolean inGame) {
    this.inGame = inGame;
  }

  // TODO: Add methods for handling move, resign, and leave game commands
}