package com.akhal3d.tool;

import java.util.Arrays;
import java.util.List;


public class GenerateAST {

	public static void main(String[] args) {
		defineAST("Expr", Arrays.asList("Binary   : Expr left, Token operator, Expr right",
				  "Grouping : Expr expression",
				  "Literal  : Object value",
				  "Logical	: Expr left, Token operator, Expr right",
				  "Unary    : Token operator, Expr right",
				  "Variable : Token name",
				  "Assign   : Token name, Expr value"));
//		defineAST("Stmt", Arrays.asList("Expression : Expr expression",
//										"Print      : Expr expression",
//										"Block      :  List<Stmt> statements",
//										"If			: Expr condition, Stmt thenBranch, Stmt elseBranch"));
	}

	private static void defineAST(String baseName, List<String> types) {
		System.out.println("package com.akhal3d.mai;");
		System.out.println("");
		System.out.println("import java.util.List;");
		System.out.println("");

		System.out.println("abstract class " + baseName + " {");
		defineEmptyExpr();
		defineVisitor(baseName, types);

		for (String type : types) {
			String className = type.split(":")[0].trim();
			String fields = type.split(":")[1].trim();

			defineType(baseName, className, fields);
		}

		System.out.println("   abstract <R> R accept(Visitor<R> visitor);");

		System.out.println("");
		System.out.println("}");
	}

	private static void defineVisitor(String baseName, List<String> types) {
		System.out.println("  interface Visitor<R> {");

		for (String type : types) {
			String typeName = type.split(":")[0].trim();
			System.out
					.println("   R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");

		}

		System.out.println("}");

	}
	
	private static void defineEmptyExpr() {
		System.out.println("static class Empty extends Expr {");
		System.out.println("	<R> R accept(Visitor<R> visitor) {");
		System.out.println("		return null;");
		System.out.println("	}");

		System.out.println("}");
	}

	private static void defineType(String baseName, String className, String fieldList) {
		System.out.println("	static class " + className + " extends " + baseName + " {");

		System.out.println("	 " + className + "(" + fieldList + ") { ");

		String[] fields = fieldList.split(", ");
		for (String field : fields) {
			String name = field.split(" ")[1];
			System.out.println("	  this." + name + " = " + name + ";");
		}

		System.out.println("	 }");

		System.out.println("");
		for (String field : fields) {
			System.out.println("	final " + field + ";");

		}

		System.out.println("    <R> R accept(Visitor<R> visitor) {");
		System.out.println("      return visitor.visit" + className + baseName + "(this);");
		System.out.println("    }");

		System.out.println("	}");

	}

}
