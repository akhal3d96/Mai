package com.akhal3d.mai;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import com.akhal3d.mai.Expr.Literal;

public class Parser {

	private int current = 0;
	private final List<Token> tokens;

	public Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	List<Stmt> parse() {
		List<Stmt> statements = new ArrayList<>();
		while (!isAtEnd()) {
			statements.add(statement());
		}
		return statements;
	}

	private Expr assignment() {
		Expr expr = or();

		if (match(TokenType.EQUAL)) {
			Token equals = previous();
			Expr value = assignment();

			if (expr instanceof Expr.Variable) {
				Token name = ((Expr.Variable) expr).name;
				return new Expr.Assign(name, value);
			}

			error(equals, "Invalid assignment target.");
		}

		return expr;
	}

	private Expr or() {
		Expr expr = and();

		while (match(TokenType.OR)) {
			Token operator = previous();
			Expr right = and();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
	}

	private Expr and() {
		Expr expr = equality();

		while (match(TokenType.AND)) {
			Token operator = previous();
			Expr right = and();
			expr = new Expr.Logical(expr, operator, right);

		}

		return expr;
	}

	private Stmt statement() {
		try {
			if (match(TokenType.IF))
				return ifStatement();
			if (match(TokenType.PRINT))
				return printStatement();
			if (match(TokenType.WHILE))
				return whileStatement();
			if (match(TokenType.DO))
				return doStatement();
			if (match(TokenType.FOR))
				return forStatement();
			if(match(TokenType.BREAK))
				return new Stmt.Break();
			if(match(TokenType.PASS))
				return new Stmt.Pass();
			if (match(TokenType.LEFT_BRACE))
				return new Stmt.Block(block());

			return expressionStatement();
		} catch (ParseError e) {
			synchronize();
			return null;
		}
	}

	private Stmt forStatement() {
		consume(TokenType.LEFT_PAREN, "Expect `(` after 'for'.");

		Stmt initializer;
		if (match(TokenType.SEMICOLON))
			initializer = null;
		else
			initializer = expressionStatement();

		Expr condition = null;
		if (!check(TokenType.SEMICOLON))
			condition = expression();
		consume(TokenType.SEMICOLON, "Expect `;` after loop condition.");

		Expr increment = null;
		if (!check(TokenType.RIGHT_PAREN))
			increment = expression();

		consume(TokenType.RIGHT_PAREN, "Expect `)` after 'for'.");

		Stmt body = statement();

		if (increment != null)
			body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));

		if (condition == null)
			condition = new Expr.Literal(true);

		body = new Stmt.While(condition, body);

		if (initializer != null)
			body = new Stmt.Block(Arrays.asList(initializer, body));

		return body;
	}

	private Stmt doStatement() {
		Stmt body = statement();

		consume(TokenType.WHILE, "Expected `while` after 'do'.");

		consume(TokenType.LEFT_PAREN, "Expect `(` after 'while'.");
		Expr condition = expression();
		consume(TokenType.RIGHT_PAREN, "Expect `)` after condition.");

		return new Stmt.Do(body, condition);
	}

	private Stmt whileStatement() {
		consume(TokenType.LEFT_PAREN, "Expect `(` after 'while'.");
		Expr condition = expression();
		consume(TokenType.RIGHT_PAREN, "Expect `)` after condition.");
		Stmt body = statement();

		return new Stmt.While(condition, body);
	}

	private Stmt ifStatement() {
		consume(TokenType.LEFT_PAREN, "Expect `(` after `if`.");
		Expr condition = expression();
		consume(TokenType.RIGHT_PAREN, "Expect `)` after `if`.");

		Stmt thenBranch = statement();
		Stmt elseBranch = null;
		if (match(TokenType.ELSE)) {
			elseBranch = statement();
		}

		return new Stmt.If(condition, thenBranch, elseBranch);
	}

	private List<Stmt> block() {
		List<Stmt> statements = new ArrayList<>();

		while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
			statements.add(statement());
		}

		consume(TokenType.RIGHT_BRACE, "Expect `}` after block.");
		consume(TokenType.NEWLINE, "Expect a new line after block.");
		return statements;
	}

	private Stmt expressionStatement() {
		Expr value = expression();
		// EDITED
		if (peek().type == TokenType.NEWLINE)
			consume(TokenType.NEWLINE, "Expect a new line after value.");
		else if (peek().type == TokenType.SEMICOLON)
			consume(TokenType.SEMICOLON, "Expect a semicolon `;` after value.");

		return new Stmt.Expression(value);
	}

	private Stmt printStatement() {
		/* the PRINT keyword is already consumed in the match() */
		Expr value = expression();
		// EDITED
		if (peek().type == TokenType.NEWLINE)
			consume(TokenType.NEWLINE, "Expect a new line after value.");

		return new Stmt.Print(value);
	}

	private Expr expression() {
		return assignment();
	}

	private Expr equality() {
		Expr expr = comparison();

		while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
			Token operator = previous();
			Expr right = comparison();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr comparison() {
		Expr expr = addition();

		while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
			Token operator = previous();
			Expr right = addition();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr addition() {
		Expr expr = multiplication();

		while (match(TokenType.PLUS, TokenType.MINUS)) {
			Token operator = previous();
			Expr right = multiplication();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr multiplication() {
		Expr expr = unary();

		while (match(TokenType.STAR, TokenType.SLASH)) {
			Token operator = previous();
			Expr right = unary();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr unary() {
		if (match(TokenType.BANG, TokenType.MINUS)) {
			Token operator = previous();
			Expr right = unary();
			return new Expr.Unary(operator, right);
		}

		return primary();
	}

	private Expr primary() {
		if (match(TokenType.FALSE))
			return new Expr.Literal(false);
		if (match(TokenType.TRUE))
			return new Expr.Literal(true);
		if (match(TokenType.NIL))
			return new Expr.Literal(null);

		if (match(TokenType.NUMBER, TokenType.STRING)) {
			return new Expr.Literal(previous().literal);
		}

		if (match(TokenType.IDENTIFIER))
			return new Expr.Variable(previous());

		if (match(TokenType.LEFT_PAREN)) {
			Expr expr = expression();
			consume(TokenType.RIGHT_PAREN, "Expect `)` after expression.");
			return new Expr.Grouping(expr);
		}

		if (match(TokenType.NEWLINE)) {
			retreat();
			return new Expr.Empty();
		}

		throw error(peek(), "Expected expression");
	}

	private Token consume(TokenType type, String msg) {
		if (check(type))
			return advance();
		throw error(peek(), msg);
	}

	private ParseError error(Token token, String msg) {
		Mai.error(token, msg);
		return new ParseError();
	}

	private void synchronize() {
		advance();

		while (!isAtEnd()) {
			if (previous().type == TokenType.NEWLINE)
				return;

			switch (peek().type) {
			case CLASS:
			case FUNC:
			case FOR:
			case IF:
			case WHILE:
			case PRINT:
			case RETURN:
				return;
			}

			advance();
		}
	}

	private boolean match(TokenType... types) {
		for (TokenType type : types) {
			if (check(type)) {
				advance();
				return true;
			}
		}
		return false;
	}

	private Token advance() {
		if (!isAtEnd())
			current++;
		return previous();
	}

	private Token retreat() {
		if (!isAtEnd())
			current--;
		return previous();
	}

	private Token previous() {
		if (current > 0)
			return tokens.get(current - 1);
		return null;
	}

	private boolean check(TokenType type) {
		if (isAtEnd())
			return false;
		return peek().type == type;
	}

	private Token peek() {
		return tokens.get(current);
	}

	private boolean isAtEnd() {
		return peek().type == TokenType.EOF;
	}

}
