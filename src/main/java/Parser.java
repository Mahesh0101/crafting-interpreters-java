import java.util.ArrayList;
import java.util.List;
// import static TokenType.*;
public class Parser {
    private static class ParseError extends RuntimeException{}
    private final List<Token> tokens;
    private int current = 0; // tracks the curent token in our statement (in parsing expressions challenge we only support one statement). 

    // it should prepare an expression. then based on the expression we should call the AstPrinter
    Parser(List<Token> tokens)
    {
        this.tokens = tokens;
    }

    // starting method of recursive parsing. -> calls the expression method.
    // the reason we are having an try catch here is becasue this is the starting point of expression building. when an exception we want to synchronize the token to start from next valid token.
    List<Stmt> Parse()
    {
        try {
            List<Stmt> statements = new ArrayList<>();
            while(!isAtEnd())
            {
                statements.add(statement());
            }
            return statements;
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        return equality();
    }

    //production -> PrintStmt | exprStmt
    private Stmt statement()
    {
        if(match(TokenType.PRINT)) return printStatement();

        return expressionStatement();
    }

    // production -> expression ";"
    private Stmt expressionStatement() {
        Expr value = expression();
        Consume(TokenType.SEMICOLON, "Expect ; after value.");
        return new Stmt.Expression(value);

    }

    // production -> "print" expression ";"
    private Stmt printStatement()
    {
        Expr value = expression();
        Consume(TokenType.SEMICOLON, "Expect ; after value.");
        return new Stmt.Print(value);
    }

    // production -> comparision (( "=" | "!=" ) comparision)*
    private Expr equality()
    {
        Expr expression = comparision();
        while(match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL))
        {
            Token operater = previous();
            Expr right = comparision();
            expression = new Expr.Binary(expression, operater, right);
        }
        return expression;
    }

    // production -> term (( ">" | "<" | ">=" | "<=" ) term)*
    private Expr comparision()
    {
        Expr expression = term();
        while(match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL))
        {
            Token operater = previous();
            Expr right = term();
            expression = new Expr.Binary(expression, operater, right);
        }
        return expression;
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
            Consume(TokenType.RIGHT_PAREN, "Expect ')' after expression");
            return new Expr.Grouping(expression);
        }

        throw error(peek(), "Expect Expression");
    }

    private Token Consume(TokenType rightParen, String message) {
        if(check(rightParen)) return advance();
            
        throw error(peek(), message);
        // Main.error(peek().line, "Unexpected character: " + peek().literal + " expected ) after expression");
    }

    private ParseError error(Token token, String message) {
        Main.error(token, message);
        return new ParseError();
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