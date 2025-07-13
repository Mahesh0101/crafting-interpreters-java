// why Stmt.Visitor<Void> is void ? for now, the statements are 2 types
// 1) print -> outputs something to user's console
// 2) Expression -> evaluating it produces side affects.  	(changes in application state.)

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>{
    public void interpret(List<Stmt> statements)
    {
        try {
            for (Stmt statement : statements) {
                evaluate(statement);
            }
        } catch (RuntimeError e) {
            Main.runtimeError(e);
        }
        
    }

    private void evaluate(Stmt statement) {
        statement.accept(this);
    }

    private String stringify(Object object) {
        if(object == null) return "nil";

        // Handle integers - display them as-is
        if(object instanceof Integer) {
            return object.toString();
        }

        // Handle doubles - display them without the decimal point if they're whole numbers
        if(object instanceof Double)
        {
            String text = object.toString();
            if(text.endsWith(".0"))
            {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        // expression operator expression. evaluate the left and right sub expression and apply the operator to the result.
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return subtractNumbers(left, right);

            case PLUS:
                if (isNumber(left) && isNumber(right)) return addNumbers(left, right);
                if (left instanceof String && right instanceof String) return (String) left + (String) right;
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");

            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return multiplyNumbers(left, right);

            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return divideNumbers(left, right);

            case EQUAL_EQUAL:
                return isEqual(left, right);

            case BANG_EQUAL:
                return !isEqual(left, right);

            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return compareNumbers(left, right, ">");
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return compareNumbers(left, right, "<");
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return compareNumbers(left, right, ">=");
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return compareNumbers(left, right, "<=");

            default:
                break;
        }

        return null;
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if(isNumber(left) && isNumber(right)) return;
        throw new RuntimeError(operator, "operands must be two numbers");
    }

    private boolean isNumber(Object obj) {
        return obj instanceof Double || obj instanceof Integer;
    }

    private double toDouble(Object obj) {
        if (obj instanceof Double) return (Double) obj;
        if (obj instanceof Integer) return ((Integer) obj).doubleValue();
        throw new RuntimeException("Object is not a number");
    }

    private Object addNumbers(Object left, Object right) {
        if (left instanceof Integer && right instanceof Integer) {
            return (Integer) left + (Integer) right;
        }
        return toDouble(left) + toDouble(right);
    }

    private Object subtractNumbers(Object left, Object right) {
        if (left instanceof Integer && right instanceof Integer) {
            return (Integer) left - (Integer) right;
        }
        return toDouble(left) - toDouble(right);
    }

    private Object multiplyNumbers(Object left, Object right) {
        if (left instanceof Integer && right instanceof Integer) {
            return (Integer) left * (Integer) right;
        }
        return toDouble(left) * toDouble(right);
    }

    private Object divideNumbers(Object left, Object right) {
        // Division always returns double to handle fractional results
        return toDouble(left) / toDouble(right);
    }

    private boolean compareNumbers(Object left, Object right, String operator) {
        double leftVal = toDouble(left);
        double rightVal = toDouble(right);

        switch (operator) {
            case ">": return leftVal > rightVal;
            case "<": return leftVal < rightVal;
            case ">=": return leftVal >= rightVal;
            case "<=": return leftVal <= rightVal;
            default: throw new RuntimeException("Unknown comparison operator");
        }
    }

    private Boolean isEqual(Object left, Object right) {
        // unlike comparision operator which require number or arthimatic operators which require both number or string (incase of addition), the equality operator supports operands of any type, even mixed ones
        // we cant ask lox if 3 > "three" but we can ask if it's equal to it
        if(left == null && right == null) return true;
        if(left == null) return null; // handling null so we dont call NullPointerException if we try to call equals() on null
        return left.equals(right);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        // a group contains an expression. we need to evaluate it. -> ( expression ). 
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expression) {
        // can be any Expr. just need to call the correct Expr visitor of interpreter.
        // this will call the accept() of corresponding Expr class with parameter as current visitor object. which will then call correct visitor of Interpreter.
        return expression.accept(this);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value; // literal value could be boolean, Number, String, null. need to understand on how to handle nulls on other expressions

    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        // ( "-" | "!" ) expression. need to first evaluate the right hand sub expression & then apply the unary operator to the result of sub expression itself.
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                if (right instanceof Integer) return -(Integer)right;
                return -toDouble(right);
            case BANG:
                return !isTruthy(right);
            default:
                break;
        }
        // unreachable
        return null;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (isNumber(operand)) return;
        throw new RuntimeError(operator, "operand must be a number.");
    }

    private boolean isTruthy(Object right) {
        // in lox, we follow ruby's rule. false and nil are false and everything is true
        if(right == null) return false;
        if(right instanceof Boolean) return (boolean) right;
        return true;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < stmt.expressions.size(); i++) {
            Object value = evaluate(stmt.expressions.get(i));
            output.append(stringify(value));
            if (i < stmt.expressions.size() - 1) {
                output.append(" ");
            }
        }
        System.out.println(output.toString());
        return null; // why return null when return type is void. in this case sort of Boxed void -> the class type Void.
    }
    
}

// notes or observations
// 1) When using enum constants in a switch statement that's already switching on an enum type, 
//you should use the unqualified constant names

// 2) in interpreted langage like python, we can do evaluation (1 == 2) & leave strings or nums on an new line 
// & like no expression or something just literals. it either evaluates or gets ignored, not sure but not throwing an error. same will happen on lox i guess.

// 3) public boolean equals(Object anotherObject). in java equals object can be call on Strings or on any object -> only tested
// on Boxed primitive types (i guess, it autoboxes them if passed a literal). cannot invoke on primitive types

// 4) according to IEEE which specify the standard for double precesion number dividing a zero by zero gives us the special NaN value. Nan is not wual to itself.
// jshell> 0.00/0.00 ==> NaN. (0.00/0.00) ==(0.00/0.00) ==> false. in java, the == operator on primitive doubles preserves the behaviour
// but the equals() method on double does not. lox uses the latter so it wont follow IEEE
// also, in java a number divided by floating point zero returns Infinity.

// 5) Java doesn't let us use void as generic type argument for obscure reasons having to do with type erasure (know more on it) and stack.
// instead there is a seperate "Void" type specially for this use. sort of like boxed void.




// java doesn't do implicit conversion in equality ?