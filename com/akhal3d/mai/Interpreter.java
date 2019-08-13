package com.akhal3d.mai;

import java.util.List;

import org.omg.CosNaming.IstringHelper;

import com.akhal3d.mai.Expr.Assign;
import com.akhal3d.mai.Expr.Binary;
import com.akhal3d.mai.Expr.Grouping;
import com.akhal3d.mai.Expr.Literal;
import com.akhal3d.mai.Expr.Logical;
import com.akhal3d.mai.Expr.Unary;
import com.akhal3d.mai.Expr.Variable;
import com.akhal3d.mai.Stmt.Block;
import com.akhal3d.mai.Stmt.Do;
import com.akhal3d.mai.Stmt.Expression;
import com.akhal3d.mai.Stmt.If;
import com.akhal3d.mai.Stmt.Print;
import com.akhal3d.mai.Stmt.While;

public class Interpreter implements Stmt.Visitor<Object>, Expr.Visitor<Object> {

	private Environment environment = new Environment();

	public String interpret(List<Stmt> statements) {
		try {
			for (Stmt statement : statements) {
				execute(statement);
			}
		} catch (RuntimeError error) {
			Mai.runtimeError(error);
		}
		return null;
	}

	private void execute(Stmt statement) {
		statement.accept(this);
	}

	private String stringfy(Object value) {
		if (value == null)
			return "nil";

		if (value instanceof Double) {
			String text = value.toString();
			if (text.endsWith(".0")) {
				return Integer.toString(((Double) value).intValue());
			}
		}

		return value.toString();
	}

	@Override
	public Object visitBinaryExpr(Binary expr) {
		Object left = evaluate(expr.left);
		Object right = evaluate(expr.right);

		checkNumberOperands(expr.operator, left, right);

		switch (expr.operator.type) {
		case GREATER:
			return (double) left > (double) right;
		case GREATER_EQUAL:
			return (double) left >= (double) right;
		case LESS:
			return (double) left < (double) right;
		case LESS_EQUAL:
			return (double) left <= (double) right;
		case BANG_EQUAL:
			return !isEqual(left, right);
		case EQUAL_EQUAL:
			return isEqual(left, right);
		case MINUS:
			return (double) left - (double) right;
		case PLUS:
			return (double) left + (double) right;
		case SLASH:
			return (double) left / (double) right;
		case STAR:
			return (double) left * (double) right;
		}
		return null;
	}

	private void checkNumberOperands(Token operator, Object left, Object right) {
		if (left instanceof Double && right instanceof Double)
			return;
		throw new RuntimeError(operator, "Operand must be a number.");
	}

	private boolean isEqual(Object left, Object right) {
		if (left == null && right == null)
			return true;
		if (left == null)
			return false;

		return left.equals(right);
	}

	@Override
	public Object visitGroupingExpr(Grouping expr) {
		return evaluate(expr.expression);
	}

	@Override
	public Object visitLiteralExpr(Literal expr) {
		return expr.value;
	}

	@Override
	public Object visitUnaryExpr(Unary expr) {
		Object right = evaluate(expr.right);

		switch (expr.operator.type) {
		case MINUS:
			return -1 * (double) right;
		case BANG:
			return !isTruthy(right);
		}

		return null;
	}

	private Object evaluate(Expr expression) {
		return expression.accept(this);
	}

	private boolean isTruthy(Object object) {
		if (object == null)
			return false;
		if (object instanceof Boolean)
			return (boolean) object;
		return true;
	}

	@Override
	public Void visitExpressionStmt(Expression stmt) {
		evaluate(stmt.expression);
		return null;
	}

	@Override
	public Void visitPrintStmt(Print stmt) {
		Object value = evaluate(stmt.expression);
		System.out.println(stringfy(value));
		return null;
	}

	@Override
	public Object visitVariableExpr(Variable expr) {
		return environment.get(expr.name);
	}

	@Override
	public Object visitAssignExpr(Assign expr) {
		Object value = evaluate(expr.value);

		environment.define(expr.name, value);
		return value;
	}

	@Override
	public Object visitBlockStmt(Block stmt) {
		executeBlock(stmt.statements, new Environment(environment));
		return null;
	}

	private void executeBlock(List<Stmt> statements, Environment environment) {
		Environment previous = this.environment;
		try {
			this.environment = environment;

			for (Stmt statement : statements) {
				execute(statement);
			}
		} finally {
			this.environment = previous;
		}
	}

	@Override
	public Object visitIfStmt(If stmt) {
		if (isTruthy(evaluate(stmt.condition))) {
			execute(stmt.thenBranch);
		} else if (stmt.elseBranch != null) {
			execute(stmt.elseBranch);
		}

		return null;
	}

	@Override
	public Object visitLogicalExpr(Logical expr) {

		Object left = evaluate(expr.left);
		if (expr.operator.type == TokenType.OR) {
			if (isTruthy(left))
				return left;
		} else /* AND */ {
			if (!isTruthy(left))
				return left;
		}

		return evaluate(expr.right);
	}

	@Override
	public Object visitWhileStmt(While stmt) {
		while (isTruthy(evaluate(stmt.condition))) {
			execute(stmt.body);
		}
		return null;
	}

	@Override
	public Object visitDoStmt(Do stmt) {
		execute(stmt.body);
		while (isTruthy(evaluate(stmt.condition))) {
			execute(stmt.body);
		}
		
		return null;
	}

}
