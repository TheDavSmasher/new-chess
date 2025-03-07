package client;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static ui.EscapeSequences.UNSET_TEXT_COLOR;
public class Repl {

    private final ChessClient client;

    public Repl() {
        client = new ChessClient();
    }

    public void run() {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.println("Welcome to my Chess Server!");
        client.setOut(out);
        client.help(true);

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt(out);
            String line = scanner.nextLine();

            try {
                result = client.evaluate(line);
            } catch (Throwable e) {
                out.print(e);
            }
        }
        out.println();
    }

    private void printPrompt(PrintStream out) {
        out.print("\n" + UNSET_TEXT_COLOR + ">>> ");
    }
}
