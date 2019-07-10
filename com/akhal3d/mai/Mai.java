package com.akhal3d.mai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Mai {

	private static boolean hadError;
	private static boolean hadRuntimeError;

	private static final Interpreter interpreter = new Interpreter();

	static void error(int line, String msg) {
		report(line, "", msg);
	}

	static void error(Token token, String msg) {
		if (token.type == TokenType.EOF) {
			report(token.line, " at end", msg);
		} else {
			report(token.line, " at '" + token.lexeme + "'", msg);
		}
	}

	private static void report(int line, String where, String msg) {
		System.err.println("[line " + line + "] Error" + where + ": " + msg);
		hadError = true;
	}

	public static void main(String[] args) {
		if (args.length == 1) {
			try {
				runFile(args[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (args.length == 0) {
			try {
				System.out.println("Mai 0.0.1-alpha\n");
				runPrompt();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Usage: mai [script]");
			System.exit(64);
		}
	}

	private static void runPrompt() throws IOException {
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);

		while (true) {
			System.out.print(">>> ");
			// EDITED
			run(reader.readLine() + "\n");

			hadError = false;
		}
	}

	private static void runFile(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String(bytes, Charset.defaultCharset()));

		if (hadError) {
			System.exit(64);
		}

		if (hadRuntimeError) {
			System.exit(70);
		}

	}

	private static void run(String source) {
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();
		Parser parser = new Parser(tokens);
		List<Stmt> statements = parser.parse();

		if (hadError)
			return;

		interpreter.interpret(statements);

//		System.out.println(ASTPrinter.getInstance().print(expression));

	}

	public static void runtimeError(RuntimeError error) {
		System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
		hadRuntimeError = true;
	}

}
