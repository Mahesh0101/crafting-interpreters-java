
import java.util.List;

abstract class Stmt{
	interface Visitor<R> {

		R visitExpressionStmt(Expression stmt);
		R visitPrintStmt(Print stmt);
	}
	abstract <R> R accept (Visitor<R> visitor);

	static class Expression extends Stmt{
		Expression(Expr expression){
			this.expression = expression;
		}

		final Expr expression;

		@Override
		<R> R accept (Visitor<R> visitor) {
			return visitor.visitExpressionStmt(this);
		}
	}
	static class Print extends Stmt{
		Print(List<Expr> expressions){
			this.expressions = expressions;
		}

		final List<Expr> expressions;

		@Override
		<R> R accept (Visitor<R> visitor) {
			return visitor.visitPrintStmt(this);
		}
	}
}
