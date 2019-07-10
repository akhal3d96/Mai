package com.akhal3d.mai;

import com.akhal3d.mai.Expr.Assign;
import com.akhal3d.mai.Expr.Binary;
import com.akhal3d.mai.Expr.Grouping;
import com.akhal3d.mai.Expr.Literal;
import com.akhal3d.mai.Expr.Unary;
import com.akhal3d.mai.Expr.Variable;

public class ASTPrinter implements Expr.Visitor<String> {
	
	private static ASTPrinter instance = new ASTPrinter();

	private ASTPrinter() {}
	public static ASTPrinter getInstance() {
		return instance;
	}
	
	public static void main(String[] args) {
		Expr expression = new Expr.Binary(
								new Expr.Literal(5),
								new Token(TokenType.PLUS, "+", null, 1),
				                new Expr.Grouping(
				                		new Expr.Binary(
				                				new Expr.Literal(8), 
				                        		new Token(TokenType.STAR, "*", null, 1),
				                        		new Expr.Literal(2)
				                        		  	   )
				                        		  )
				                          );
		System.out.println(ASTPrinter.getInstance().print(expression));
	}

	String print(Expr expr) {
		return expr.accept(this);
	}

	@Override
	public String visitBinaryExpr(Binary expr) {
		return paranthesize(expr.operator.lexeme, expr.left, expr.right);
	}

	@Override
	public String visitGroupingExpr(Grouping expr) {
		return paranthesize("group", expr.expression);
	}

	@Override
	public String visitLiteralExpr(Literal expr) {
		if (expr.value == null)
			return "nil";
		return expr.value.toString();
	}

	@Override
	public String visitUnaryExpr(Unary expr) {
		return paranthesize(expr.operator.lexeme, expr.right);
	}

	private String paranthesize(String name, Expr... exprs) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("(").append(name);
		for(Expr exp : exprs) {
			builder.append(" ");
			builder.append(exp.accept(this));
		}
		builder.append(")");
		
		return builder.toString();
	}
	@Override
	public String visitVariableExpr(Variable expr) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String visitAssignExpr(Assign assign) {
		// TODO Auto-generated method stub
		return null;
	}
}
