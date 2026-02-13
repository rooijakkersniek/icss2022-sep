package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {
	private Stylesheet stylesheet;
	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}
    public AST getAST() {
        return ast;
    }

	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		stylesheet = new Stylesheet();
		currentContainer.push(stylesheet);
	}

	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		stylesheet = (Stylesheet) currentContainer.pop();
		ast.setRoot(stylesheet);
	}

	@Override
	public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment variableAssignment = new VariableAssignment();
		variableAssignment.name = new VariableReference(ctx.CAPITAL_IDENT().getText());
        try {
            variableAssignment.expression = createExpression(ctx.value().expr());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        currentContainer.push(variableAssignment);
	}

	@Override
	public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment node = (VariableAssignment) currentContainer.pop();
		currentContainer.peek().addChild(node);
	}

	@Override
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		Stylerule node = new Stylerule();
		currentContainer.push(node);
	}

	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		Stylerule stylerule = (Stylerule) currentContainer.pop();
		currentContainer.peek().addChild(stylerule);
	}

	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration declaration = new Declaration();
		declaration.property = (new PropertyName(ctx.variable().getText()));
        try {
            declaration.expression = createExpression(ctx.value().expr());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        currentContainer.push(declaration);
	}

	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration decl = (Declaration) currentContainer.pop();
		currentContainer.peek().addChild(decl);
	}

	@Override
	public void enterTag(ICSSParser.TagContext ctx) {
		Stylerule rule = (Stylerule) currentContainer.peek();
		String text = ctx.getText();

		if (ctx.LOWER_IDENT() != null) {
			rule.selectors.add(new TagSelector(text));
		} else if (ctx.CLASS_IDENT() != null) {
			rule.selectors.add(new ClassSelector(text));
		} else if (ctx.ID_IDENT() != null) {
			rule.selectors.add(new IdSelector(text));
		} else if (ctx.CAPITAL_IDENT() != null) {
			rule.selectors.add(new TagSelector(text));
		}
	}

	@Override
	public void enterIfclause(ICSSParser.IfclauseContext ctx) {
		IfClause ifClause = new IfClause();
		try	{
			ifClause.conditionalExpression = createExpression(ctx.expr());
		} catch (Exception e) {
            throw new RuntimeException(e);
        }
		currentContainer.push(ifClause);
    }

	public void exitIfclause(ICSSParser.IfclauseContext ctx) {
		IfClause ifClause = (IfClause) currentContainer.pop();
		currentContainer.peek().addChild(ifClause);
	}

	public void enterElseclause(ICSSParser.ElseclauseContext ctx) {
		ElseClause elseClause = new ElseClause();
		currentContainer.push(elseClause);
	}

	public void exitElseclause(ICSSParser.ElseclauseContext ctx) {
		ElseClause elseClause = (ElseClause) currentContainer.pop();

		if(currentContainer.peek() instanceof IfClause) {
			((IfClause) currentContainer.peek()).elseClause = elseClause;
		}
	}

	// Recursive methode die de expressions opzet.
	private Expression createExpression(ICSSParser.ExprContext ctx) throws Exception {
		if(ctx.getChildCount() == 3) {
			Expression left = createExpression(ctx.expr(0));
			Expression right = createExpression(ctx.expr(1));
			String operator = ctx.getChild(1).getText();

			switch (operator) {
				case "+": return new AddOperation(left, right);
				case "-": return new SubtractOperation(left, right);
				case "*": return new MultiplyOperation(left, right);
			}
		}

		if(ctx.basic() != null) {
			return createBasicExpression(ctx.basic());
		}
		throw new Exception("Unexpected");
	}

	//Hulp methode om de juiste "basic" expression te krijgen.
	private Expression createBasicExpression(ICSSParser.BasicContext ctx) {
		if (ctx.COLOR() != null) {
			return new ColorLiteral(ctx.COLOR().getText());
		}
		if (ctx.PIXELSIZE() != null) {
			return new PixelLiteral(
					Integer.parseInt(ctx.PIXELSIZE().getText().replace("px", ""))
			);
		}
		if (ctx.PERCENTAGE() != null) {
			return new PercentageLiteral(
					Integer.parseInt(ctx.PERCENTAGE().getText().replace("%", ""))
			);
		}
		if (ctx.SCALAR() != null) {
			return new ScalarLiteral(Integer.parseInt(ctx.SCALAR().getText()));
		}
		if (ctx.TRUE() != null || ctx.FALSE() != null) {
			return new BoolLiteral(Boolean.parseBoolean(ctx.getText()));
		}
		if (ctx.variable() != null) {
			return new VariableReference(ctx.variable().getText());
		}

		throw new RuntimeException("Unknown value");
	}
}