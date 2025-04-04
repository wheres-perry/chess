import client.ChessClient; // Make sure ChessClient is imported

public class Main {
    public static void main(String[] args) {
        var serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        ChessClient client = new ChessClient(serverUrl);

        client.run();
    }
}