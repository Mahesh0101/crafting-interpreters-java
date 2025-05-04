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
        // System.err.println(c);
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
            case '=':
            // learned new this, the ternery operator (condition ? expr1 : expr2; ) requires both expr1 and expr2 to be expressions that return a value (even if that value is ignored). But if addToken(...) returns void, you can't use it in a ternary like that.
            // so this wont work -> matchNext('=')? addToken(TokenType.EQUAL_EQUAL) : addToken(TokenType.EQUAL);
                addToken(matchNext('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '!':
                addToken(matchNext('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '<':
                addToken(matchNext('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;    
            case '>':
                addToken(matchNext('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '/':
                // if next matches /-> it's a comment and needs to ignore the current line
                if(matchNext('/')) 
                {
                    skipCurrentLine();
                    break;
                }
                addToken(TokenType.SLASH);
                break;
            case ' ':
                break;
            case '\n':
                break;
            case '\t':
                break;
            default:
                Main.error(line, "Unexpected character: " + c);
                break;
        }
    }

    private boolean matchNext(char expected){

        if(isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private void skipCurrentLine()
    {
        // this.current = source.length();
        // this.start = current;
        while (!isAtEnd() && getCurrentChar() != '\n');
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


// do you now ? char in Java is an unsigned 16-bit type (ranging from 0 to 65535), it cannot represent -1.