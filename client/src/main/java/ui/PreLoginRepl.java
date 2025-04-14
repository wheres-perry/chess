package ui;

import client.ChessClient;

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

  private static final Pattern EMAIL_PATTERN = Pattern.compile(
      "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

  public PreLoginRepl(ChessClient client) {
    this.client = client;
    this.scanner = new Scanner(System.in);
  }

  /**
   * Runs the pre-login command loop.
   * 
   * @return true if the user enters the 'quit' command, false otherwise.
   */
  public boolean run() {
    System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Welcome! Type " + EscapeSequences.SET_TEXT_COLOR_YELLOW
        + "'help'" + EscapeSequences.SET_TEXT_COLOR_BLUE + " for options."
        + EscapeSequences.RESET_TEXT_COLOR);

    while (!client.isLoggedIn()) {
      System.out.print(EscapeSequences.RESET
          + EscapeSequences.SET_TEXT_COLOR_WHITE + "[LOGGED_OUT] "
          + EscapeSequences.SET_TEXT_COLOR_DARK_GREY
          + EscapeSequences.SET_TEXT_BLINKING + ">> "
          + EscapeSequences.SET_TEXT_COLOR_GREEN);
      String line = scanner.nextLine().trim();
      System.out.print(EscapeSequences.RESET_TEXT_COLOR);
      String[] args = line.split("\\s+");
      if (args.length == 0 || args[0].isEmpty())
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
            return true;
          case "login":
            handleLogin(Arrays.copyOfRange(args, 1, args.length));
            break;
          case "register":
            handleRegister(Arrays.copyOfRange(args, 1, args.length));
            break;
          case "debug_clear":
            handleClearDatabase();
            break;
          default:
            printError("Unknown command. Type 'help' for options.");
        }
      } catch (Exception e) {
        printError("Operation failed (Client): " + e.getMessage());
        e.printStackTrace();
      }
    }
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
   * Handles the 'login' command.
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
      System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Logged in successfully as " + username
          + EscapeSequences.RESET_TEXT_COLOR);
    } catch (Exception e) {
      String errorMessage = e.getMessage() != null ? e.getMessage() : "An unknown error occurred during login.";
      if (errorMessage.toLowerCase().contains("unauthorized") || errorMessage.contains("401")) {
        printError("Login failed: Invalid username or password.");
      } else if (errorMessage.toLowerCase().contains("bad request") || errorMessage.contains("400")) {
        printError("Login failed: Missing username or password.");
      } else {
        printError("Login failed: " + errorMessage);
      }
    }
  }

  /**
   * Handles the 'register' command.
   */
  private void handleRegister(String[] args) {
    if (args.length != 3) {
      printError("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
      return;
    }
    String username = args[0];
    String password = args[1];
    String email = args[2];

    if (!EMAIL_PATTERN.matcher(email).matches()) {
      printError("Invalid email format provided.");
      return;
    }

    try {
      client.register(username, password, email);
      System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Registered and logged in successfully as " + username
          + EscapeSequences.RESET_TEXT_COLOR);
    } catch (Exception e) {
      String errorMessage = e.getMessage() != null ? e.getMessage() : "An unknown error occurred during registration.";
      if (errorMessage.toLowerCase().contains("already taken") || errorMessage.contains("403")) {
        printError("Registration failed: Username or email might already be taken.");
      } else if (errorMessage.toLowerCase().contains("bad request") || errorMessage.contains("400")) {
        printError("Registration failed: Missing username, password, or email.");
      } else {
        printError("Registration failed: " + errorMessage);
      }
    }
  }

  /** Handles the undocumented 'debug_clear' command. */
  private void handleClearDatabase() {
    try {
      System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Attempting server database clear..."
          + EscapeSequences.RESET_TEXT_COLOR);
      client.triggerServerClear();
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