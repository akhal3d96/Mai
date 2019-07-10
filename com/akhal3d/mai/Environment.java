package com.akhal3d.mai;

import java.util.HashMap;
import java.util.Map;

public class Environment {
	final Environment enclosing;
	private final Map<String, Object> values = new HashMap<>();

	/* Global Scope */
	public Environment() {
		this.enclosing = null;
	}

	/* Local Scopes */
	public Environment(Environment env) {
		this.enclosing = env;
	}

	private final Environment findEnv(String name, Object value) {
		Environment env = this;
		if (!this.values.containsKey(name) /* variable is not here */ &&
				this.enclosing != null /* there's a another environment up in the chain */) {
			
			env = this.enclosing.findEnv(name, value);
		}

		if (!this.values.containsKey(name)) {
			/*
			 * Assign to current environment return null
			 */
			return null;
		}
		return env;
	}

	public void define(String name, Object value) {
//		Environment env = findEnv(name, value);
//		if (env == null) {
//			values.put(name, value);
//
//		} else {
//			env.values.put(name, value);
//		}
		
		values.put(name, value);

	}

	public Object get(Token name) {
		if (values.containsKey(name.lexeme))
			return values.get(name.lexeme);

		if (enclosing != null)
			return enclosing.get(name);

		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
	}
}
