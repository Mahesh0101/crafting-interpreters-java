import java.util.ArrayList;
import java.util.HashMap;

public class Scanner {
    private final String source;
    ArrayList<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final HashMap<String,TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }

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
        char c = advance();
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
                incrementLineNumber();
                break;
            case '\t':
                break;
            // token to identify string literal

            case '"':
                scanStringLiteral();
                break;
            default:
                if (Character.isDigit(c)) {
                    scanNumberLiteral();
                }
                else if (isAlpha(c))
                {
                    scanIdentifier();
                }
                else {
                    Main.error(line, "Unexpected character: " + c);
                }
                break;
        }
    }

    private boolean matchNext(char expected){

        if(isAtEnd()) return false;
        if (peek() != expected) return false;

        current++;
        return true;
    }

    private void skipCurrentLine()
    {
        while (!isAtEnd() && peek() != '\n') advance();
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
    private char advance()
    {
        return source.charAt(current++);
    }

    private char peek()
    {
        if (current >= source.length()) return '\0';
        return source.charAt(current);
    }

    private char peekNext()
    {
        // \0 in Java is the null character, also known as the NUL character (ASCII code 0).
        if (current+1 >= source.length()) return '\0';
        return source.charAt(current+1);
    }

    private void incrementLineNumber()
    {
        line = line + 1;
    }

    private void scanStringLiteral(){
        while (!isAtEnd() && peek() != '"') 
        {
            if(peek() == '\n') incrementLineNumber();
            advance();
        }
        //reached EOF with out terminating the string. t
        if (isAtEnd()) 
        {
            Main.error(line, "Unterminated string.");
            return;
        }
        //skipping the current char -> terminated string ".
        advance();
        String literal = source.substring(start+1, current-1);
        addToken(TokenType.STRING, literal);
    }

    private void scanNumberLiteral()
    {
        // consume as many digits possible
        while (!isAtEnd() && Character.isDigit(peek())) advance();
        // if dot is present after digits consumed then check next char is a digit and consume as many as possible
        if(peek() == '.' && Character.isDigit(peekNext()))
        {
            //consume .
            advance();
            while (!isAtEnd() && Character.isDigit(peek())) advance();
        }
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start,current)));
    }
// no need to check isAtEnd in the whilelook as peek() already checks if it is at EOF
    private void scanIdentifier() {
        while(isAlphaNumeric(peek())) advance();
        String lexeme = source.substring(start, current); // this piece of code used many times maybe can make a method lexeme()
        TokenType type = keywords.get(lexeme);
        if(type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    // check if a character is an alphabet or _
    private boolean isAlpha(char c)
    {
        
        return (c >= 'a' && c <= 'z' || 
                c >= 'A' && c <= 'Z' || 
                c =='_');
    }

    // check if a character is alphabet or digit or _
    private boolean isAlphaNumeric(char c)
    {
        return isAlpha(c) || Character.isDigit(c);
    }
}


// do you now ? char in Java is an unsigned 16-bit type (ranging from 0 to 65535), it cannot represent -1.
// go one char at a time