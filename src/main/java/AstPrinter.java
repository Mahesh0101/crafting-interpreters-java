
public class AstPrinter implements Expr.Visitor<String>{
    String print(Expr expr)
    {
        return expr.accept(this);
    }
    // 2 + 2 * 3 - 3 + 5 -> (+ 2 (* 2 3)) // i dont think this is how it works. need to define precedence for out language.
        private String paranthesis(String operator, Expr ...exprs) {
            StringBuilder builder = new StringBuilder();
            builder.append("(").append(operator);
            builder.append(" ");
            for (Expr expr : exprs) {
                builder.append(expr.accept(this));
            }
            builder.append(")");
            return builder.toString();
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return paranthesis( expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return paranthesis("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return paranthesis( expr.operator.lexeme, expr.right);
    }
}
