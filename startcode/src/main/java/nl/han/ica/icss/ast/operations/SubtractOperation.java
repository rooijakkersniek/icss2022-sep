package nl.han.ica.icss.ast.operations;

import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.Operation;

public class SubtractOperation extends Operation {

    public SubtractOperation(){}

    public SubtractOperation(Expression leftOperand, Expression rightOperand) {
        this.lhs = leftOperand;
        this.rhs = rightOperand;
    }

    @Override
    public String getNodeLabel() {
        return "Subtract";
    }
}
