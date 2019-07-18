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

	private final Environment findEnv(String name, Environment env) {

		if (env.values.containsKey(name)) {
			return env;
		} else if (env.enclosing != null) {
			return findEnv(name, env.enclosing);
		}

		return null;
	}

	public void define(Token name, Object value) {

		Environment env = findEnv(name.lexeme, this);
		if (env == null) {
			values.put(name.lexeme, value);

		} else {
			env.values.put(name.lexeme, value);
			
		}

//		values.put(name, value);

	}

	public Object get(Token name) {
		if (values.containsKey(name.lexeme))
			return values.get(name.lexeme);

		if (enclosing != null)
			return enclosing.get(name);

		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
	}
}
