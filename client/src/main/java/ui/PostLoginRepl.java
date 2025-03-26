package ui;

import client.ChessClient;
import java.util.Scanner;

public class PostLoginRepl {
    private final ChessClient client;
    private final Scanner scanner;

    public PostLoginRepl(ChessClient client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
    }

    public boolean run() {
        // TODO: Implement post-login REPL logic
        return false;
    }

    private String getInput() {
        System.out.print(client.getCurrentUser() + " >>> ");
        return scanner.nextLine().trim();
    }

    private void displayHelp() {
        // TODO: Implement help display for post-login state
    }

    // TODO: Add methods for handling logout, create game, list games, join game, and observe game commands
}