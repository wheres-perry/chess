package ui;

import client.ChessClient;
import java.util.Scanner;

public class PreLoginRepl {
  private final ChessClient client;
  private final Scanner scanner;

  public PreLoginRepl(ChessClient client) {
    this.client = client;
    this.scanner = new Scanner(System.in);
  }

  public boolean run() {
    // TODO: Implement pre-login REPL logic
    return false;
  }

  private String getInput() {
    System.out.print(">>> ");
    return scanner.nextLine().trim();
  }

  private void displayHelp() {
    // TODO: Implement help display for pre-login state
  }

  // TODO: Add methods for handling register and login commands
}
