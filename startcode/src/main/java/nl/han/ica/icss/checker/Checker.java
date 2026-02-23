package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.IfClause;
import nl.han.ica.icss.ast.Operation;
import nl.han.ica.icss.ast.VariableAssignment;
import nl.han.ica.icss.ast.VariableReference;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
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

        if(node instanceof Declaration) {
            checkForTypeCompatibility((Declaration) node);
        }

        if(node instanceof VariableReference) {
            checkForExistenceVariableReference((VariableReference) node);
        }

        if(node instanceof Operation){
            checkOperations((Operation) node);
        }

        if(node instanceof IfClause){
            addScope();
            for(ASTNode child : node.getChildren()) {
                checkNode(child);
            }
            closeScope();
        } else {
            for(ASTNode child : node.getChildren()) {
                checkNode(child);
            }
        }
    }

    private void addVariableAssignment(VariableAssignment variableAssignment) {
        String name = variableAssignment.name.name;
        ExpressionType type = getExpressionType(variableAssignment.expression);
        variableTypes.get(0).put(name, type);
    }

    private void checkForTypeCompatibility(Declaration declaration) {
        ExpressionType valueType = getExpressionType(declaration.expression);
        String property = declaration.property.name;
        boolean isValid = true;

        switch (property) {
            case "color":
                isValid = (valueType == ExpressionType.COLOR);
                break;

            case "width":
            case "height":
                isValid = (valueType == ExpressionType.PIXEL || valueType == ExpressionType.PERCENTAGE);
                break;
        }

        if(!isValid) {
            declaration.setError("Invalid type for property " + property);
        }
    }

    private void checkForExistenceVariableReference(VariableReference variableReference) {
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

    private void checkOperations(Operation node) {
        ExpressionType left = getExpressionType(node.lhs);
        ExpressionType right = getExpressionType(node.rhs);

        if(checkForColorExpressionType(left, right)){
            node.setError("No color allowed in operation");
        }

        if(node instanceof AddOperation || node instanceof SubtractOperation){
            if(!checkForMatchingOperationExpression(left, right)){
                node.setError("Expression Type Mismatch");
            }
        }
        if(node instanceof MultiplyOperation){
            if(checkForScalar(left, right)){
                node.setError("No Scalar in MultiplyOperation");
            }
        }
    }

    private boolean checkForColorExpressionType(ExpressionType left, ExpressionType right) {
        return left == ExpressionType.COLOR || right == ExpressionType.COLOR;
    }

    private boolean checkForScalar(ExpressionType left, ExpressionType right) {
        return left != ExpressionType.SCALAR && right != ExpressionType.SCALAR;
    }

    private boolean checkForMatchingOperationExpression(ExpressionType left, ExpressionType right) {
        return left == right;
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
