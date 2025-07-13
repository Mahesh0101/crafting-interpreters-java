import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {

  private static final Interpreter interpreter = new Interpreter();
  static boolean hasError;
  static boolean hadRuntimeError;
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.err.println("Logs from your program will appear here!");

    if (args.length < 2) {
      System.err.println("Usage: ./your_program.sh tokenize <filename>");
      System.exit(1);
    }

    String command = args[0];
    String filename = args[1];

    if (!(command.equals("tokenize")|| command.equals("parse") || command.equals("interpret"))) {
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
    Parser parser = new Parser(tokens);

    if(command.equals("tokenize"))
    {
      for (Token token : tokens) {
        System.out.println(token);
      }
    }
    // checking for scanning errors
    if (Main.hasError) System.exit(65);

    List<Stmt> expression = parser.Parse();

    //checking for parsing errors
    if (Main.hasError) System.exit(65);

    // if(command.equals("parse")){
    //   System.out.println(new AstPrinter().print(expression));
    // }
    
    if(command.equals("interpret")){
      // right now it only interprets the first first expression it encounters. so if it fails it throws an RuntimeError and exits.
      interpreter.interpret(expression);
      if (Main.hadRuntimeError) System.exit(70);
    }

    return;
  }

    private static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
    Main.hasError = true;
  }

  static void error(int line, String message)
  {
    report(line, "", message);
  }

  static void error(Token token, String message) {
    if (token.type == TokenType.EOF) report(token.line, " at end", message);
    else report(token.line, " at " + token.lexeme + " ", message);
  }

  public static void runtimeError(RuntimeError e) {
    System.err.println("Error:\n" + e.getMessage() + "\n [Line " + e.token.line + "]");
    hadRuntimeError = true;
  }

}
