import java.util.List;
import java.util.function.Consumer;

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

        return term();
    }

    // production -> factor (( "+" | "-" ) factor)*
    private Expr term()
    {
        Expr expression = factor();
        while(match(TokenType.PLUS, TokenType.MINUS))
        {
            Token operater = previous();
            Expr right = factor();
            expression = new Expr.Binary(expression, operater, right);
        }
        return expression;
    }
    
    // production -> unary (( "*" | "/" ) unary)*
    private Expr factor()
    {
        Expr expression = unary();
        while(match(TokenType.STAR, TokenType.SLASH))
        {
            Token operater = previous();
            Expr right = unary();
            expression = new Expr.Binary(expression, operater, right);
        }
        return expression;
    }

    // production -> ( "!" | "-") unary
    //               | primary
    private Expr unary()
    {
        if(match(TokenType.MINUS, TokenType.BANG))
        {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
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

        if(match(TokenType.LEFT_PAREN))
        {
            Expr expression = expression();
            Consume(TokenType.RIGHT_PAREN, "Expect closing Paranthesis ')'");
            return new Expr.Grouping(expression);
        }

        throw new RuntimeException("Expect Expression");
    }

    private void Consume(TokenType rightParen, String string) {
        if(check(rightParen))
        {
            advance();
            return;
        }
        Main.error(peek().line, "Unexpected character: " + peek().literal + " expected ) after expression");
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