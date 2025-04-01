package ui;

import client.ChessClient;
import java.util.Scanner;
import java.util.Arrays;

public class PreLoginRepl {
  private final ChessClient client;
  private final Scanner scanner;

  public PreLoginRepl(ChessClient client) {
    this.client = client;
    this.scanner = new Scanner(System.in);
  }

  /**
   * Runs the pre-login command loop.
   * 
   * @return true if the user wants to quit the application, false otherwise.
   */
  public boolean run() {
    System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Welcome! Type 'help' for options.");
    while (!client.isLoggedIn()) {
      System.out.print(EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR + "[LOGGED_OUT] >>> "
          + EscapeSequences.SET_TEXT_COLOR_GREEN);
      String line = scanner.nextLine().trim();
      System.out.print(EscapeSequences.RESET_TEXT_COLOR); // Reset color after input
      String[] args = line.split("\\s+");
      if (args.length == 0)
        continue;
      String command = args[0].toLowerCase();

      try {
        switch (command) {
          case "help":
            displayHelp();
            break;
          case "quit":
            return true; // Signal to exit the main loop
          case "login":
            handleLogin(Arrays.copyOfRange(args, 1, args.length));
            break;
          case "register":
            handleRegister(Arrays.copyOfRange(args, 1, args.length));
            break;
          default:
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Unknown command. Type 'help' for options."
                + EscapeSequences.RESET_TEXT_COLOR);
        }
      } catch (Exception e) {
        System.out.println(
            EscapeSequences.SET_TEXT_COLOR_RED + "Error: " + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
      }
    }
    return false;
  }

  private void displayHelp() {
    System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Available commands:");
    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  help" + EscapeSequences.SET_TEXT_COLOR_WHITE
        + "          - Show this help message");
    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  quit" + EscapeSequences.SET_TEXT_COLOR_WHITE
        + "          - Exit the program");
    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  login <USERNAME> <PASSWORD>"
        + EscapeSequences.SET_TEXT_COLOR_WHITE + " - Log in to an existing account");
    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  register <USERNAME> <PASSWORD> <EMAIL>"
        + EscapeSequences.SET_TEXT_COLOR_WHITE + " - Create a new account");
    System.out.print(EscapeSequences.RESET_TEXT_COLOR);
  }

  private void handleLogin(String[] args) throws Exception {
    if (args.length != 2) {
      throw new Exception("Usage: login <USERNAME> <PASSWORD>");
    }
    String username = args[0];
    String password = args[1];
    client.login(username, password);
    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Logged in successfully as " + username
        + EscapeSequences.RESET_TEXT_COLOR);
  }

  private void handleRegister(String[] args) throws Exception {
    if (args.length != 3) {
      throw new Exception("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
    }
    String username = args[0];
    String password = args[1];
    String email = args[2];
    client.register(username, password, email);
    System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Registered and logged in successfully as " + username
        + EscapeSequences.RESET_TEXT_COLOR);
  }
}