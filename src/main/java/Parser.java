import java.util.List;

import javax.management.RuntimeErrorException;

public class Parser {
    private final List<Token> tokens;
    private int current = 0; // tracks the curent token in our statement (in parsing expressions challenge we only support one statement). 

    // it should prepare an expression. then based on the expression we should call the AstPrinter
    Parser(List<Token> tokens)
    {
        this.tokens = tokens;
    }

    // starting method of recursive parsing. -> calls the expression method.
    Expr Parse()
    {
        return expression();
    }

    private Expr expression() {
        // TODO Auto-generated method stub
        return primary();
    }

    // primary handles our Literal and Grouping from our grammer.
    // production -> STRING | NUMBER | "nil" | "true" | "false"
    //              | "(" expression ")"
    private Expr primary() {
        if(match(TokenType.FALSE)) return new Expr.Literal(false);
        if(match(TokenType.TRUE)) return new Expr.Literal(true);
        if(match(TokenType.NIL)) return new Expr.Literal(null);
        
        while(match(TokenType.STRING, TokenType.NUMBER))
        {
            return new Expr.Literal(previous().literal);
        }

        throw new RuntimeException("Expect Expression");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if(check(type)) 
            {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token advance() {
        if(!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }


}
//parsing — transmogrifying a sequence of tokens into one of those syntax trees