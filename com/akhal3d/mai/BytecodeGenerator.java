package com.akhal3d.mai;

import com.akhal3d.mai.Expr.Binary;
import com.akhal3d.mai.Expr.Grouping;
import com.akhal3d.mai.Expr.Literal;
import com.akhal3d.mai.Expr.Unary;

import java.util.Queue;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class BytecodeGenerator implements Opcodes, Expr.Visitor<Object> {

	public byte[] generateBytecode(Queue<Instruction> instructionQueue, String name) throws Exception {

		ClassWriter cw = new ClassWriter(0);
		MethodVisitor mv;
		// version , access, name, signature, base class, interfaces
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, name, null, "java/lang/Object", null);
		{
			// declare static void main
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
//			final long localVariablesCount = instructionQueue.stream()
//					.filter(instruction -> instruction instanceof VariableDeclaration).count();
			final int maxStack = 100; // TODO - do that properly

			// apply instructions generated from traversing parse tree!
			for (Instruction instruction : instructionQueue) {
				instruction.apply(mv);
			}
			mv.visitInsn(RETURN); // add return instruction

			mv.visitMaxs(maxStack, (int) localVariablesCount); // set max stack and max local variables
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}

	public String interpret(Expr expression) {
		try {
			Object value = evaluate(expression);
			return stringfy(value);
		} catch (RuntimeError error) {
			Mai.runtimeError(error);
		}
		return null;
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

}
