package nl.han.ica.icss.ast.operations;

import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.Operation;

public class AddOperation extends Operation {

    @Override
    public String getNodeLabel() {
        return "Add";
    }

    public AddOperation(){}

    public AddOperation(Expression leftOperand, Expression rightOperand) {
        this.lhs = leftOperand;
        this.rhs = rightOperand;
    }
}
