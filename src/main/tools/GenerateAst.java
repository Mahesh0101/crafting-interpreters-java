import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    // we give output directory as the input to the file.
    public static void main(String[] args) throws IOException {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.err.println("Logs from your program will appear here!");

        if (args.length != 1) {
          System.err.println("Usage: GenerateAst <output directory>"); // hint on how to use this.
          System.exit(64);
        }

        String outputDir = args[0];

        defineAst(outputDir, "Expr",  Arrays.asList( // can alos use List.of
          "Binary : Expr left, Token operator, Expr right",
          "Grouping : Expr expression",
          "Literal : Object value",
          "Unary : Token operator, Expr right"
        ));

        defineAst(outputDir, "Stmt",  Arrays.asList( // can alos use List.of
          "Expression : Expr expression",
          "Print : Expr expression"
        ));
      }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {

      String path = outputDir + "/" + baseName + ".java";
      PrintWriter printWriter = new PrintWriter(path, "UTF-8");
      printWriter.println("");
      printWriter.println("abstract class " + baseName + "{");
			defineVisitor(printWriter, baseName, types);
			printWriter.println("\tabstract <R> R accept (Visitor<R> visitor);");
      printWriter.println("");
      for (String type : types)
      {
        String className = type.split(":")[0].trim();
        String fieldList = type.split(":")[1].trim();
        defineType(printWriter, baseName, className, fieldList);
      }

      printWriter.println("}");
      printWriter.close();

    }
    
    private static void defineVisitor(PrintWriter printWriter, String baseName, List<String> types) {
			printWriter.println("\tinterface Visitor<R> {");
			printWriter.println("");
			for (String type : types)
			{
				String typeName  = type.split(":")[0].trim();
				printWriter.println("\t\tR visit"+typeName+baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
			}
			printWriter.println("\t}");
		}

		private static void defineType(PrintWriter printWriter, String baseName, String className, String fieldList) {
			// constructer
      printWriter.println("\tstatic class " + className + " extends " + baseName + "{");
      printWriter.println("\t\t" + className + "(" + fieldList + ")" + "{"); //2
			String[] fields = fieldList.split(", ");
			for (String field : fields)
			{
				String name = field.split(" ")[1].trim();
				printWriter.println("\t\t\t" + "this." + name + " = " + name + ";");
			}
			printWriter.println("\t\t}");

			printWriter.println();
			// fields
			for (String field : fields)
			{
				printWriter.println("\t\tfinal " + field + ";");	
			}
			printWriter.println("");
			printWriter.println("\t\t@Override");
			printWriter.println("\t\t<R> R accept (Visitor<R> visitor) {");
			printWriter.println("\t\t\treturn visitor.visit" + className + baseName + "(this);");
			printWriter.println("\t\t}");

      printWriter.println("\t}");
    }    
}

// to run the script
//javac -d bin src/main/tools/GenerateAst.java
// java -cp bin GenerateAst src/main/java

// if we have a file with same name as output file and run this program, it'll overwrite the file and save it. 
// so make sure to review the changes for any overwrites (comments etc)