import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Main {
  static boolean hasError;
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.err.println("Logs from your program will appear here!");

    if (args.length < 2) {
      System.err.println("Usage: ./your_program.sh tokenize <filename>");
      System.exit(1);
    }

    String command = args[0];
    String filename = args[1];

    if (!(command.equals("tokenize")|| command.equals("parse"))) {
      System.err.println("Unknown command: " + command);
      System.exit(1);
    }

    String fileContents = "";
    try {
      fileContents = Files.readString(Path.of(filename));
      Main.hasError = false;
    } catch (IOException e) {
      System.err.println("Error reading file: " + e.getMessage());
      System.exit(1);
    }
    
    // if (fileContents.length() == 0) {
    //   System.out.println("EOF  null");
    //   return; // Placeholder, remove this line when implementing the scanner
    // }
    
    ArrayList<Token> tokens = new ArrayList<>();
    Scanner scanner = new Scanner(fileContents);
    tokens = scanner.scanTokens();

    if(command.equals("tokenize"))
    {
      for (Token token : tokens) {
        System.out.println(token);
      }
    }
    else if(command.equals("parse"))
    {
      Parser parser = new Parser(tokens);
      Expr expression = parser.Parse();
      System.out.println(new AstPrinter().print(expression));
    }

    if (Main.hasError) System.exit(65);

    return;
  }

  static void error(int line, String message)
  {
    // later, implement another private method report to print this message
    System.err.println("[line " + line + "] Error: " + message);
    Main.hasError = true;
  }

}
