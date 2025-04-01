import ui.InGameRepl;
import ui.PreLoginRepl;
import ui.PostLoginRepl;

public class ClientMain {
    public static void main(String[] args) {
        var serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        // new PreLoginRepl(serverUrl).run();
    }

}