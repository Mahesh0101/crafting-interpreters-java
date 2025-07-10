
public class Interpreter implements Expr.Visitor<Object>{
    public void interpret(Expr expression)
    {
        try {
            Object result = evaluate(expression);
            System.out.println(stringify(result));
        } catch (RuntimeError e) {
            Main.runtimeError(e);
        }
        
    }

    private String stringify(Object object) {
        if(object == null) return "nil";

        //TODO understand lox representation of number from chapter 3.
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
                return (double) left - (double) right;

            case PLUS:

                if (left instanceof Double && right instanceof Double) return (double) left + (double) right;
                if (left instanceof String && right instanceof String) return (String) left + (String) right;
                throw new RuntimeError(expr.operator, "Operands must be two number or two strings.");

            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
                
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;

            case EQUAL_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return isEqual(left, right);
                
            case BANG_EQUAL:
                return !isEqual(left, right); // no need to handle for below operator as they will always return a boolean.

            case GREATER:
                
                return (double) left > (double) right;
            case LESS:
                
                return (double) left >= (double) right;
            case GREATER_EQUAL:
                
                return (double) left < (double) right;
            case LESS_EQUAL:
                
                return (double) left <= (double) right;

            default:
                break;
        }

        return null;
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {

        if(left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "operands must be two numbers");
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
                return -(Double)right;
            case BANG:
                return !isTruthy(right);
            default:
                break;
        }
        // unreachable
        return null;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "operand must be a number.");
    }

    private boolean isTruthy(Object right) {
        // in lox, we follow ruby's rule. false and nil are false and everything is true
        if(right == null) return false;
        if(right instanceof Boolean) return (boolean) right;
        return true;
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

// java doesn't do implicit conversion in equality ?