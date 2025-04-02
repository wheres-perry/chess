package ui;

import client.ChessClient;
import java.util.Scanner;
import java.util.Arrays;
import java.util.regex.Pattern; // Added for email validation

public class PreLoginRepl {
  private final ChessClient client;
  private final Scanner scanner;

  // Basic email validation pattern
  private static final Pattern EMAIL_PATTERN = Pattern.compile(
      "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

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
            System.out.println(
                EscapeSequences.SET_TEXT_COLOR_YELLOW + "Exiting application." + EscapeSequences.RESET_TEXT_COLOR);
            return true; // Signal to exit the main loop
          case "login":
            handleLogin(Arrays.copyOfRange(args, 1, args.length));
            // Success message handled within handleLogin upon successful client.login()
            break;
          case "register":
            handleRegister(Arrays.copyOfRange(args, 1, args.length));
            // Success message handled within handleRegister upon successful
            // client.register()
            break;
          default:
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Unknown command. Type 'help' for options."
                + EscapeSequences.RESET_TEXT_COLOR);
        }
      } catch (Exception e) {
        // Provide a more user-friendly error message
        printError("Operation failed: " + e.getMessage());
      }
    }
    // Returns false if loop exited due to successful login/register
    return false;
  }

  private void displayHelp() {
    System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Available commands:");
    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  help" + EscapeSequences.SET_TEXT_COLOR_WHITE
        + "                 - Show this help message");
    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  quit" + EscapeSequences.SET_TEXT_COLOR_WHITE
        + "                 - Exit the program");
    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  login <USERNAME> <PASSWORD>"
        + EscapeSequences.SET_TEXT_COLOR_WHITE + " - Log in to an existing account");
    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  register <USERNAME> <PASSWORD> <EMAIL>"
        + EscapeSequences.SET_TEXT_COLOR_WHITE + " - Create a new account");
    System.out.print(EscapeSequences.RESET_TEXT_COLOR);
  }

  private void handleLogin(String[] args) {
    if (args.length != 2) {
      printError("Usage: login <USERNAME> <PASSWORD>");
      return;
    }
    String username = args[0];
    String password = args[1];
    try {
      client.login(username, password); // Assuming ChessClient handles auth token storage
      // Print success message only if login didn't throw an exception
      System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Logged in successfully as " + username
          + EscapeSequences.RESET_TEXT_COLOR);
    } catch (Exception e) {
      // Try to provide more specific feedback if possible, otherwise use general
      // error
      String errorMessage = e.getMessage() != null ? e.getMessage() : "An unknown error occurred during login.";
      if (errorMessage.toLowerCase().contains("unauthorized")) {
        printError("Login failed: Invalid username or password.");
      } else {
        printError("Login failed: " + errorMessage);
      }
    }
  }

  private void handleRegister(String[] args) {
    if (args.length != 3) {
      printError("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
      return;
    }
    String username = args[0];
    String password = args[1];
    String email = args[2];

    // Basic email validation
    if (!EMAIL_PATTERN.matcher(email).matches()) {
      printError("Invalid email format provided.");
      return;
    }

    try {
      client.register(username, password, email); // Assuming ChessClient handles auth token storage
      // Print success message only if register didn't throw an exception
      System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Registered and logged in successfully as " + username
          + EscapeSequences.RESET_TEXT_COLOR);
    } catch (Exception e) {
      // Try to provide more specific feedback if possible
      String errorMessage = e.getMessage() != null ? e.getMessage() : "An unknown error occurred during registration.";
      if (errorMessage.toLowerCase().contains("already taken") || errorMessage.toLowerCase().contains("conflict")) { // Common
                                                                                                                     // indicators
                                                                                                                     // for
                                                                                                                     // duplicate
                                                                                                                     // user
        printError("Registration failed: Username might already be taken.");
      } else {
        printError("Registration failed: " + errorMessage);
      }
    }
  }

  // Helper method for printing errors consistently
  private void printError(String message) {
    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: " + message + EscapeSequences.RESET_TEXT_COLOR);
  }
}