package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Evaluator implements Transform {

    private LinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new LinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues.push(new HashMap<>());
        var remove = new LinkedList<ASTNode>();

        for(ASTNode node : ast.root.getChildren()) {
            if(node instanceof VariableAssignment){
                applyVariableAssignment((VariableAssignment)node);
                remove.add(node);
            } else if(node instanceof Stylerule){
                applyStyleRule((Stylerule) node);
            }
        }
        ast.root.getChildren().removeAll(remove);
    }

    private void applyStyleRule(Stylerule stylerule) {
        variableValues.push(new HashMap<>());
        var processed = new LinkedList<ASTNode>();

        for(ASTNode node : stylerule.body) {
            if(node instanceof VariableAssignment){
                applyVariableAssignment((VariableAssignment)node);
            } else if(node instanceof IfClause){
                processed.addAll(applyIfClause((IfClause)node));
            } else if(node instanceof Declaration){
                Declaration declaration = (Declaration) node;
                declaration.expression = evalExpression(declaration.expression );
                processed.add(declaration);
            }
        }
        LinkedList<ASTNode> lastNodes = getLastNodes(processed);
        stylerule.body = new ArrayList<>(lastNodes);
        variableValues.pop();
    }

    private LinkedList<ASTNode> getLastNodes(LinkedList<ASTNode> processed){
        LinkedList<ASTNode> last = new LinkedList<>();
        HashSet<String> seen = new HashSet<>();

        for (int i = processed.size() - 1; i >= 0; i--) {
            ASTNode node = processed.get(i);
            if (node instanceof Declaration) {
                String name = ((Declaration) node).property.name;
                if (seen.add(name)) {
                    last.addFirst(node);
                }
            } else {
                last.addFirst(node);
            }
        }
        return last;
    }

    private ArrayList<ASTNode> applyIfClause(IfClause node) {
        Literal condition = evalExpression(node.conditionalExpression);
        ArrayList<ASTNode> body = null;

        if(condition instanceof BoolLiteral && ((BoolLiteral) condition).value){
            body = node.body;
        } else if(node.elseClause != null) {
            body = node.elseClause.body;
        }
        if(body == null) {
            return new ArrayList<>();
        }

        ArrayList<ASTNode> newBody = new ArrayList<>();

        for (ASTNode element : body) {
            if (element instanceof VariableAssignment) {
                applyVariableAssignment((VariableAssignment) element);
            }
            else if (element instanceof Declaration) {
                Declaration declaration = (Declaration) element;
                declaration.expression = evalExpression(declaration.expression);
                newBody.add(declaration);
            }
            else if (element instanceof IfClause) {
                newBody.addAll(applyIfClause((IfClause) element));
            }
        }
        return newBody;
    }

    private void applyVariableAssignment(VariableAssignment variableAssignment) {
        Literal literal = evalExpression(variableAssignment.expression);
        variableValues.peek().put(variableAssignment.name.name, literal);
    }

    private Literal evalExpression(Expression expression) {
        if(expression instanceof VariableReference) {
            for(HashMap<String, Literal> map : variableValues) {
                if(map.containsKey(((VariableReference) expression).name)) {
                    return map.get(((VariableReference) expression).name);
                }
            }
            return new ScalarLiteral(0);
        }

        if(expression instanceof Literal){
            return (Literal) expression;
        }

        if(expression instanceof AddOperation) {
            AddOperation addOperation = (AddOperation) expression;
            return add(evalExpression(addOperation.lhs), evalExpression(addOperation.rhs));
        }

        if(expression instanceof SubtractOperation){
            SubtractOperation subtractOperation = (SubtractOperation) expression;
            return min(evalExpression(subtractOperation.lhs), evalExpression(subtractOperation.rhs));
        }

        if(expression instanceof MultiplyOperation){
            MultiplyOperation multiplyOperation = (MultiplyOperation) expression;
            return mul(evalExpression(multiplyOperation.lhs), evalExpression(multiplyOperation.rhs));
        }

        return null;
    }

    private Literal add(Literal lhs, Literal rhs) {
        if(lhs instanceof PixelLiteral) {
            return new PixelLiteral((((PixelLiteral) lhs).value) + (((PixelLiteral) rhs).value));
        }
        if(rhs instanceof ScalarLiteral) {
            return new ScalarLiteral(((ScalarLiteral) lhs).value + ((ScalarLiteral) rhs).value);
        }
        if(lhs instanceof PercentageLiteral) {
            return new PercentageLiteral(((PercentageLiteral) lhs).value + ((PercentageLiteral) rhs).value);
        }
        return lhs;
    }

    private Literal min(Literal lhs, Literal rhs) {
        if(lhs instanceof PixelLiteral) {
            return new PixelLiteral(((PixelLiteral) lhs).value - ((PixelLiteral) rhs).value );
        }
        if(rhs instanceof ScalarLiteral) {
            return new ScalarLiteral(((ScalarLiteral) lhs).value - ((ScalarLiteral) rhs).value);
        }
        if(lhs instanceof PercentageLiteral) {
            return new PercentageLiteral(((PercentageLiteral) lhs).value - ((PercentageLiteral) rhs).value);
        }
        return lhs;
    }

    private Literal mul(Literal lhs, Literal rhs) {
        if(lhs instanceof PixelLiteral) {
            return new PixelLiteral(((PixelLiteral) lhs).value * ((PixelLiteral) rhs).value );
        }
        if(rhs instanceof ScalarLiteral) {
            return new ScalarLiteral(((ScalarLiteral) lhs).value * ((ScalarLiteral) rhs).value);
        }
        if(lhs instanceof PercentageLiteral) {
            return new PercentageLiteral(((PercentageLiteral) lhs).value * ((PercentageLiteral) rhs).value);
        }
        return lhs;
    }
}
