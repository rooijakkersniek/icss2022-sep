package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.Stylesheet;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;

public class Generator {

	public String generate(AST ast) {
        return generateStylesheet(ast.root);
	}

	private String generateStylesheet(Stylesheet stylesheet) {
		StringBuilder builder = new StringBuilder();
		for(ASTNode child : stylesheet.getChildren()) {
			if(child instanceof Stylerule) {
				builder.append(generateStylerule((Stylerule) child)).append(System.lineSeparator());
			}
		}
        return builder.toString();
    }

	private String generateStylerule(Stylerule stylerule){
		for(ASTNode child : stylerule.getChildren()) {
			if(child instanceof Declaration) {
				return stylerule.selectors.get(0).toString()
						+ " {\n"
						+ generateDeclaration((Declaration) child )
						+ "\n}";
			}
		}
		return "";
	}

	private String generateDeclaration(Declaration declaration) {
		return "  " + declaration.property.name + ": " + generateExpression(declaration.expression);
	}

	private String generateExpression(Expression expression) {
		if(expression instanceof PixelLiteral) {
			return ((PixelLiteral) expression).value + "px";
		}
		if(expression instanceof ScalarLiteral) {
			return ((ScalarLiteral) expression).value + "%";
		}
		if(expression instanceof BoolLiteral){
			return ((BoolLiteral) expression).value ? "TRUE" : "FALSE";
		}
		if(expression instanceof ColorLiteral){
			return ((ColorLiteral) expression).value;
		}
		return expression.toString();
	}

	
}
