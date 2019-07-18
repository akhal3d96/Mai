package com.akhal3d.mai;

import java.util.List;

abstract class Stmt {
	static class Empty extends Expr {
		<R> R accept(Visitor<R> visitor) {
			return null;
		}
	}

	interface Visitor<R> {
		R visitExpressionStmt(Expression stmt);

		R visitPrintStmt(Print stmt);

		R visitBlockStmt(Block stmt);

		R visitWhileStmt(While stmt);

		R visitIfStmt(If stmt);
	}

	static class Expression extends Stmt {
		Expression(Expr expression) {
			this.expression = expression;
		}

		final Expr expression;

		<R> R accept(Visitor<R> visitor) {
			return visitor.visitExpressionStmt(this);
		}
	}

	static class Print extends Stmt {
		Print(Expr expression) {
			this.expression = expression;
		}

		final Expr expression;

		<R> R accept(Visitor<R> visitor) {
			return visitor.visitPrintStmt(this);
		}
	}

	static class Block extends Stmt {
		Block(List<Stmt> statements) {
			this.statements = statements;
		}

		final List<Stmt> statements;

		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBlockStmt(this);
		}
	}

	static class While extends Stmt {
		While(Expr condition, Stmt body) {
			this.condition = condition;
			this.body = body;
		}

		final Expr condition;
		final Stmt body;

		<R> R accept(Visitor<R> visitor) {
			return visitor.visitWhileStmt(this);
		}
	}

	static class If extends Stmt {
		If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
			this.condition = condition;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}

		final Expr condition;
		final Stmt thenBranch;
		final Stmt elseBranch;

		<R> R accept(Visitor<R> visitor) {
			return visitor.visitIfStmt(this);
		}
	}

	abstract <R> R accept(Visitor<R> visitor);

}
