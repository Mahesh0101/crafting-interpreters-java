import java.util.ArrayList;

public class Scanner {
    private final String source;
    ArrayList<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Scanner(String source) {
        this.source = source;
    }
    
    public ArrayList<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        // System.out.println(start + "  " + current);
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken(){
        char c = getCurrentChar();
        switch (c) {
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            default:
                Main.error(line, "Unexpected character: " + c);
                break;
        }
    }

    private void addToken(TokenType token){
        addToken(token, null);
    }

    private void addToken(TokenType token, Object literal){
        String lexeme = source.substring(start, current);
        tokens.add(new Token(token, lexeme, literal, line));
    }

    private boolean isAtEnd()
    {
        return current >= source.length();
    }
    private char getCurrentChar()
    {
        return source.charAt(current++);
    }
}