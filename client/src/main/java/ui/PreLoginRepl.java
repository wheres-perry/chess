// client/src/main/java/ui/PreLoginRepl.java
package ui;

import client.ChessClient;
import client.ServerFacade; // Import for exception handling

import java.util.Scanner;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Handles user interactions before login or registration.
 * Allows users to get help, quit, log in, or register.
 */
public class PreLoginRepl {
  private final ChessClient client;
  private final Scanner scanner;

  // Basic email pattern validation
  private static final Pattern EMAIL_PATTERN = Pattern.compile(
      "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

  public PreLoginRepl(ChessClient client) {
    this.client = client;
    this.scanner = new Scanner(System.in);
  }

  /**
   * Runs the pre-login command loop. This loop continues as long as the client is
   * not logged in.
   *
   * @return true if the user enters the 'quit' command, false otherwise (e.g., if
   *         they log in or register).
   */
  public boolean run() {
    System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Welcome! Type " + EscapeSequences.SET_TEXT_COLOR_YELLOW
        + "'help'" + EscapeSequences.SET_TEXT_COLOR_BLUE + " for options."
        + EscapeSequences.RESET_TEXT_COLOR);

    // Loop continues as long as the client is not logged in
    while (!client.isLoggedIn()) {
      System.out.print(EscapeSequences.RESET // Reset all formatting before the prompt
          + EscapeSequences.SET_TEXT_COLOR_WHITE + "[LOGGED_OUT] "
          + EscapeSequences.SET_TEXT_COLOR_DARK_GREY
          + EscapeSequences.SET_TEXT_BLINKING + ">> " // Prompt indicator
          + EscapeSequences.SET_TEXT_COLOR_GREEN); // Color for user input
      String line = scanner.nextLine().trim();
      System.out.print(EscapeSequences.RESET_TEXT_COLOR); // Reset color after input
      String[] args = line.split("\\s+");
      if (args.length == 0 || args[0].isEmpty()) // Handle empty input line
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
            return true; // Signal to ChessClient to terminate
          case "login":
            handleLogin(Arrays.copyOfRange(args, 1, args.length));
            // If login succeeds, client.isLoggedIn() becomes true, loop terminates
            break;
          case "register":
            handleRegister(Arrays.copyOfRange(args, 1, args.length));
            // If register succeeds, client.isLoggedIn() becomes true, loop terminates
            break;
          case "debug_clear": // Undocumented debug command
            handleClearDatabase();
            break;
          default:
            printError("Unknown command. Type 'help' for options.");
        }
      } catch (Exception e) {
        printError("Operation failed: " + e.getMessage());
        if (!(e instanceof ServerFacade.ServerException)) {
          e.printStackTrace();
        }
      }
    }
    // Loop terminated because user logged in or registered
    return false;
  }

  private void displayHelp() {
    System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Available commands:");
    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  help" + EscapeSequences.SET_TEXT_COLOR_WHITE
        + "                  - Show this help message");
    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  quit" + EscapeSequences.SET_TEXT_COLOR_WHITE
        + "                  - Exit the program");
    System.out.println(
        EscapeSequences.SET_TEXT_COLOR_YELLOW + "  login <USERNAME> <PASSWORD>" + EscapeSequences.SET_TEXT_COLOR_WHITE
            + " - Log in to an existing account");
    System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  register <USERNAME> <PASSWORD> <EMAIL>"
        + EscapeSequences.SET_TEXT_COLOR_WHITE
        + " - Create a new account");
    System.out.print(EscapeSequences.RESET_TEXT_COLOR);
  }

  /**
   * Handles the 'login' command, parsing arguments and calling the client method.
   */
  private void handleLogin(String[] args) {
    if (args.length != 2) {
      printError("Usage: login <USERNAME> <PASSWORD>");
      return;
    }
    String username = args[0];
    String password = args[1];
    try {
      client.login(username, password);
      // Print success message here upon successful login in this REPL
      System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Logged in successfully as " + username
          + EscapeSequences.RESET_TEXT_COLOR);
    } catch (Exception e) {
      // Provide user-friendly messages based on common error types
      String errorMessage = e.getMessage() != null ? e.getMessage() : "An unknown error occurred during login.";
      if (errorMessage.toLowerCase().contains("unauthorized") || errorMessage.contains("401")) {
        printError("Login failed: Invalid username or password.");
      } else if (errorMessage.toLowerCase().contains("bad request") || errorMessage.contains("400")) {
        printError("Login failed: Missing username or password.");
      } else {
        printError("Login failed: " + errorMessage); // General server/network error
      }
    }
  }

  /**
   * Handles the 'register' command, parsing arguments and calling the client
   * method.
   */
  private void handleRegister(String[] args) {
    if (args.length != 3) {
      printError("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
      return;
    }
    String username = args[0];
    String password = args[1];
    String email = args[2];

    // Basic client-side email format validation
    if (!EMAIL_PATTERN.matcher(email).matches()) {
      printError("Invalid email format provided.");
      return;
    }

    try {
      client.register(username, password, email);
      // Print success message here upon successful registration in this REPL
      System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Registered and logged in successfully as " + username
          + EscapeSequences.RESET_TEXT_COLOR);
      // State change (isLoggedIn) will cause the loop in run() to terminate
    } catch (Exception e) {
      // Provide user-friendly messages based on common error types
      String errorMessage = e.getMessage() != null ? e.getMessage() : "An unknown error occurred during registration.";
      if (errorMessage.toLowerCase().contains("already taken") || errorMessage.contains("403")) {
        printError("Registration failed: Username or email might already be taken.");
      } else if (errorMessage.toLowerCase().contains("bad request") || errorMessage.contains("400")) {
        printError("Registration failed: Missing username, password, or email.");
      } else {
        printError("Registration failed: " + errorMessage); // General server/network error
      }
    }
  }

  /** Handles the undocumented 'debug_clear' command. */
  private void handleClearDatabase() {
    try {
      System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Attempting server database clear..."
          + EscapeSequences.RESET_TEXT_COLOR);
      client.triggerServerClear(); // Calls the pass-through method in ChessClient
      System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN
          + "Debug: Server database clear request sent successfully." + EscapeSequences.RESET_TEXT_COLOR);
    } catch (Exception e) {
      printError("Database clear failed: " + e.getMessage());
    }
  }

  /** Prints an error message to the console in red. */
  private void printError(String message) {
    System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: " + message + EscapeSequences.RESET_TEXT_COLOR);
  }
}