package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.VariableAssignment;
import nl.han.ica.icss.ast.VariableReference;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.types.ExpressionType;
import java.util.HashMap;
import java.util.LinkedList;

public class Checker {

    private LinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new LinkedList<>();

        addScope();
        checkNode(ast.root);
        closeScope();
    }

    private void checkNode(ASTNode node) {
        if(node instanceof VariableAssignment) {
            addVariableAssignment((VariableAssignment) node);
        }

        if(node instanceof VariableReference) {
            checkForExistenceVariableReference((VariableReference) node);
        }
    }

    public void addVariableAssignment(VariableAssignment variableAssignment) {
        String name = variableAssignment.name.name;
        ExpressionType type = getExpressionType(variableAssignment.expression);
        variableTypes.get(0).put(name, type);
    }

    public void checkForExistenceVariableReference(VariableReference variableReference) {
        boolean found = false;

        for (HashMap<String, ExpressionType> variableType : variableTypes) {
            if (variableType.containsKey(variableReference.name)) {
                found = true;
                break;
            }
        }

        if(!found){
            variableReference.setError("Variable " + variableReference.name + " not found");
        }
    }

    private ExpressionType getExpressionType(Expression expression) {
        if(expression instanceof BoolLiteral) return ExpressionType.BOOL;
        if(expression instanceof PixelLiteral) return ExpressionType.PIXEL;
        if(expression instanceof PercentageLiteral) return ExpressionType.PERCENTAGE;
        if(expression instanceof ScalarLiteral) return ExpressionType.SCALAR;
        if(expression instanceof ColorLiteral) return ExpressionType.COLOR;
        if(expression instanceof VariableReference){
            for (HashMap<String, ExpressionType> variableType : variableTypes) {
                if (variableType.containsKey(((VariableReference) expression).name)) {
                    return variableType.get(((VariableReference) expression).name);
                }
            }
        }
        return ExpressionType.UNDEFINED;
    }

    private void addScope() {
        variableTypes.addFirst(new HashMap<>());
    }

    private void closeScope() {
        variableTypes.removeFirst();
    }
}
